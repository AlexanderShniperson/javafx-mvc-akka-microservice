package carsale.microservice.api.example

import akka.actor._
import akka.serialization.Serializer

object MessageProxyRouterActor {

  case class ApiOutgoingMessage(data: AnyRef, fromActor: String, toActor: Option[String])

  def props(connectionHandler: ActorRef, messageProcessor: ActorRef, serializer: Serializer): Props = {
    Props(classOf[ApiMessageProxyRouterActor], connectionHandler, messageProcessor, serializer)
  }
}

private class ApiMessageProxyRouterActor(connectionHandler: ActorRef, messageProcessor: ActorRef, serializer: Serializer)
  extends Actor
    with ActorLogging {

  import MessageProxyRouterActor.ApiOutgoingMessage
  import carsale.microservice.api.example.MessageExtractor._
  import akka.io.Tcp.{Received, Write}
  import scala.util.{Failure, Success, Try}

  private var localRefs = Map.empty[String, ActorRef]
  private val messageExtractor = context.actorOf(MessageExtractor.props())

  override def receive: Receive = {
    case Received(data) =>
      messageExtractor ! MessageExtract(data)

    case MessageExtractReply(data) => deserializeMsg(data).foreach(self ! _)

    /**
      * This part only for Server side, when message coming from Client and recipient is unknown
      */
    case msg@ApiIncomingMessage(bytes, fromActor, None) =>
      deserializeMsg(bytes).foreach(x => messageProcessor.tell(x, getOrCreateProxyRemoteActor(fromActor)))

    /**
      * This part for both Client and Server
      */
    case msg@ApiIncomingMessage(bytes, fromActor, Some(toActor)) =>
      localRefs.get(toActor) match {
        case None => log.error(s"[${getClass.getName}] received IncomingMessage($fromActor, $toActor) but Local actor is not found!")
        case Some(toActorRef) =>
          val remoteProxy = getOrCreateProxyRemoteActor(fromActor)
          deserializeMsg(bytes).foreach(x => toActorRef.tell(x, remoteProxy))
      }

    case msg@ApiOutgoingMessage(data, from, to) =>
      if (from == sender().path.toSerializationFormat && localRefs.get(from).isEmpty) {
        context.watch(sender())
        localRefs = localRefs.updated(from, sender())
      }
      messageExtractor ! MessageBuild(wrap2Incoming(data, from, to))

    case MessageBuildReply(data) =>
      connectionHandler ! Write(data)

    case Terminated(actorRef) =>
      releaseLocal(actorRef)

    case RemoteTerminated(Some(from)) =>
      context.child(getProxyRemoteName(from)).foreach(ref => {
        context.stop(ref)
      })

    case any => log.error(s"[ ClientConnectionHandler ] received unhandled message <$any>")
  }

  def getProxyRemoteName(toActor: String): String = {
    "MPR_" + toActor.hashCode()
  }

  def getOrCreateProxyRemoteActor(toActor: String): ActorRef = {
    val actorName = getProxyRemoteName(toActor)
    context.child(actorName).getOrElse {
      context.actorOf(MessageProxyRemoteActor.props(self, toActor), name = actorName)
    }
  }

  private def releaseLocal(localActor: ActorRef): Unit = {
    localRefs.find(_._2 == localActor).foreach(e => {
      val pathSerialized = e._2.path.toSerializationFormat
      messageExtractor ! MessageBuild(serializeMsg(RemoteTerminated(Some(pathSerialized))).get)
    })
    localRefs = localRefs.filterNot(_._2 == localActor)
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    super.postStop()
    log.info(s"[${getClass.getName}] STOP!!!")
  }

  def serializeMsg(value: AnyRef): Option[Array[Byte]] = {
    Try {
      serializer.toBinary(value)
    } match {
      case Success(s) => Some(s)
      case Failure(f) =>
        println(s"[MessageSerializer] serializeMsg Error ${f.getLocalizedMessage}")
        None
    }
  }

  def deserializeMsg(value: Array[Byte]): Option[AnyRef] = {
    Try {
      serializer.fromBinary(value)
    } match {
      case Success(s) => Some(s)
      case Failure(f) =>
        println(s"[MessageSerializer] deserializeMsg Error ${f.getLocalizedMessage} dataLen(${value.length})")
        None
    }
  }

  def wrap2Incoming(data: AnyRef, from: String, to: Option[String]): Array[Byte] = {
    Try {
      serializeMsg(ApiIncomingMessage(serializeMsg(data).get, from, to)).get
    } match {
      case Success(s) => s
      case Failure(f) =>
        println(s"[MessageSerializer] wrap2Incoming Error ${f.getLocalizedMessage}")
        Array.empty
    }
  }
}
