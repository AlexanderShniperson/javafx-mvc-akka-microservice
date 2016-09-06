package carsale.microservice.api.example

import akka.actor._

object MessageProxyActor {
  def props(connectionHandler: ActorRef) = {
    Props(classOf[ApiMessageProxyActor], connectionHandler)
  }
}

private class ApiMessageProxyActor(connectionHandler: ActorRef) extends Actor {
  private var localEndPointRef = ActorRef.noSender
  private var remoteEndPointRef = ""

  override def receive: Receive = {
    case _ =>
  }
}
