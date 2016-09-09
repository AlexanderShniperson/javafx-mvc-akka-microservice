package carsale.microservice.api.example

import akka.actor._
import akka.io.Tcp.Write
import akka.serialization.Serializer
import akka.util.ByteString

object MessageProxyRouterActor {

  case class ApiOutgoingMessage(data: AnyRef, fromActor: String, toActor: Option[String])

  def props(connectionHandler: ActorRef, messageProcessor: ActorRef, serializer: Serializer): Props = {
    Props(classOf[ApiMessageProxyRouterActor], connectionHandler, messageProcessor, serializer)
  }
}

private class ApiMessageProxyRouterActor(connectionHandler: ActorRef, messageProcessor: ActorRef, serializer: Serializer) extends Actor with ActorLogging {

  import MessageProxyRouterActor.ApiOutgoingMessage

  private var localRefs = Map.empty[Int, ActorRef]

  override def receive: Receive = {
    /**
      * This part only for Server side, when message coming from Client and recipient is unknown
      */
    case msg@ApiIncomingMessage(bytes, fromActor, None) =>
      messageProcessor.tell(deserializeMsg(bytes), getOrCreateProxyRemoteActor(fromActor))

    /**
      * This part for both Client and Server
      */
    case msg@ApiIncomingMessage(bytes, fromActor, Some(toActor)) =>
      localRefs.get(toActor.hashCode) match {
        case None => log.warning(s"[${getClass.getName}] received IncomingMessage($fromActor, $toActor) but Local actor is not found!")
        case Some(toActorRef) =>
          val remoteProxy = getOrCreateProxyRemoteActor(fromActor)
          toActorRef.tell(deserializeMsg(bytes), remoteProxy)
      }

    case ApiOutgoingMessage(data, from, to) =>
      if (from == sender().path.toSerializationFormat && localRefs.get(from.hashCode).isEmpty) {
        context.watch(sender())
        localRefs = localRefs.updated(from.hashCode, sender())
      }
      connectionHandler ! Write(serializeMsg(data, from, to))

    case Terminated(actorRef) =>
      releaseLocal(actorRef)

    case RemoteTerminated(Some(from)) =>
      context.child(getProxyRemoteName(from)).foreach(ref => {
        context.stop(ref)
      })

    case any => log.warning(s"[ ClientConnectionHandler ] received unhandled message <$any>")
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

  def serializeMsg(data: AnyRef, from: String, to: Option[String]): ByteString = {
    ByteString.fromArray(serializer.toBinary(ApiIncomingMessage(serializer.toBinary(data), from, to)))
  }

  def deserializeMsg(data: Array[Byte]): AnyRef = {
    serializer.fromBinary(data)
  }

  private def releaseLocal(localActor: ActorRef) {
    localRefs.find(_._2 == localActor).foreach(e => {
      val pathSerialized = e._2.path.toSerializationFormat
      connectionHandler ! Write(ByteString.fromArray(serializer.toBinary(RemoteTerminated(Some(pathSerialized)))))
    })
    localRefs = localRefs.filterNot(_._2 == localActor)
  }
}
