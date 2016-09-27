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

  log.info(s"[MessageProxyRemoteActor] created for $to")

  def receive = {
    case msg: RemoteTerminated =>
      context.parent ! msg
      context stop self

    case ReceiveTimeout =>
      context stop self

    case data: AnyRef => sendOut(data, sender())
  }

  private def sendOut(data: AnyRef, senderRef: ActorRef): Unit = {
    log.info(s">>> [MessageProxyRemoteActor] SendOut($data)")
    proxyRouter ! ApiOutgoingMessage(data,
      fromActor = senderRef.path.toSerializationFormat,
      toActor = Some(to))
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    log.info(s">>> [MessageProxyRemoteActor] STOP !!!")
    super.postStop()
  }
}