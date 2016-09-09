package carsale.microservice.api.example

import akka.actor._

object MessageProxyRemoteActor {
  def props(proxyRouter: ActorRef, to: String): Props = {
    Props(classOf[MessageProxyRemoteActor], proxyRouter, to)
  }
}

private class MessageProxyRemoteActor(proxyRouter: ActorRef, to: String) extends Actor with ActorLogging {

  import akka.actor.ReceiveTimeout
  import concurrent.duration._
  import MessageProxyRouterActor.ApiOutgoingMessage

  context.setReceiveTimeout(20.minutes)

  def receive = {
    case msg: RemoteTerminated =>
      context.parent ! msg
      context stop self

    case ReceiveTimeout =>
      context stop self

    case data: AnyRef => sendOut(data, sender())
  }

  private def sendOut(data: AnyRef, senderRef: ActorRef): Unit = {
    proxyRouter ! ApiOutgoingMessage(data,
      fromActor = senderRef.path.toSerializationFormat,
      toActor = Some(to))
  }
}