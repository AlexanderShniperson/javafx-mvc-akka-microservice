package carsale.microservice.api.example

import akka.actor._
import akka.io.Tcp.{Received, Write}
import akka.serialization.Serializer
import akka.util.ByteString

import scala.util.{Failure, Success, Try}

object MessageProxyRouterActor {

  case class ApiOutgoingMessage(data: AnyRef, fromActor: String, toActor: Option[String])

  def props(connectionHandler: ActorRef, messageProcessor: ActorRef, serializer: Serializer): Props = {
    Props(classOf[ApiMessageProxyRouterActor], connectionHandler, messageProcessor, serializer)
  }
}

private class ApiMessageProxyRouterActor(connectionHandler: ActorRef, messageProcessor: ActorRef, serializer: Serializer)
  extends Actor
    with ActorLogging
    with MessageExtractor
    with MessageSerializer {

  import MessageProxyRouterActor.ApiOutgoingMessage

  private var localRefs = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case Received(data) =>
      implicit val ser = serializer
      extractMessage(data.toArray).foreach(r => deserializeMsg(r).foreach(self ! _))

    /**
      * This part only for Server side, when message coming from Client and recipient is unknown
      */
    case msg@ApiIncomingMessage(bytes, fromActor, None) =>
      log.info(s">>> [ApiMessageProxyRouterActor] ApiIncomingMessage($fromActor, None) #1")
      implicit val ser = serializer
      deserializeMsg(bytes).foreach(x => messageProcessor.tell(x, getOrCreateProxyRemoteActor(fromActor)))

    /**
      * This part for both Client and Server
      */
    case msg@ApiIncomingMessage(bytes, fromActor, Some(toActor)) =>
      log.info(s">>> [ApiMessageProxyRouterActor] ApiIncomingMessage($fromActor, $toActor) #2")
      implicit val ser = serializer
      localRefs.get(toActor) match {
        case None => log.error(s"[${getClass.getName}] received IncomingMessage($fromActor, $toActor) but Local actor is not found!")
        case Some(toActorRef) =>
          val remoteProxy = getOrCreateProxyRemoteActor(fromActor)
          deserializeMsg(bytes).foreach(x => toActorRef.tell(x, remoteProxy))
      }

    case msg@ApiOutgoingMessage(data, from, to) =>
      log.info(s">>> [ApiMessageProxyRouterActor] ApiOutgoingMessage($from, $to) #1")
      implicit val ser = serializer
      if (from == sender().path.toSerializationFormat && localRefs.get(from).isEmpty) {
        context.watch(sender())
        localRefs = localRefs.updated(from, sender())
      }
      //       connectionHandler ! Write(serializeMsg(data, from, to))
      connectionHandler ! Write(buildMessage(wrap2Incoming(data, from, to)))

    case Terminated(actorRef) =>
      log.info(s">>> [ApiMessageProxyRouterActor] Terminated($actorRef)")
      releaseLocal(actorRef)

    case RemoteTerminated(Some(from)) =>
      log.info(s">>> [ApiMessageProxyRouterActor] TerminatedRemote($from)")
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
    implicit val ser = serializer
    localRefs.find(_._2 == localActor).foreach(e => {
      val pathSerialized = e._2.path.toSerializationFormat
      connectionHandler ! Write(buildMessage(serializeMsg(RemoteTerminated(Some(pathSerialized))).get))
    })
    localRefs = localRefs.filterNot(_._2 == localActor)
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    super.postStop()
    log.info(s"[${getClass.getName}] STOP!!!")
  }
}
