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

  override def receive: Receive = {
    /**
      * This part only for Server side, when message coming from Client and recipient is unknown
      */
    case msg@ApiIncomingMessage(bytes, fromActor, None) =>
      messageProcessor.tell(serializer.fromBinary(bytes), getOrCreateProxyRemoteActor(fromActor))

    /**
      * This part for both Client and Server
      */
    case msg@ApiIncomingMessage(bytes, fromActor, Some(toActor)) =>
      context.child(getProxyRemoteName(toActor)) match {
        case None => log.warning(s"[${getClass.getName}] received IncomingMessage($fromActor, $toActor) but to actor is not found!")
        case Some(toActorRef) =>
          val remote = getOrCreateProxyRemoteActor(fromActor)
          toActorRef.tell(serializer.fromBinary(bytes), remote)
      }

    case ApiOutgoingMessage(data, from, to) =>
      connectionHandler ! Write(ByteString.fromArray(serializer.toBinary(ApiIncomingMessage(serializer.toBinary(data), from, to))))

    case any => log.warning(s"[ ClientConnectionHandler ] received unhandled message <$any>")
  }

  def getProxyRemoteName(toActor: String): String = {
    "MPA_" + toActor.hashCode()
  }

  def getOrCreateProxyRemoteActor(toActor: String): ActorRef = {
    val actorName = getProxyRemoteName(toActor)
    context.child(actorName).getOrElse {
      context.actorOf(MessageProxyRemoteActor.props(self, toActor), name = actorName)
    }
  }
}
