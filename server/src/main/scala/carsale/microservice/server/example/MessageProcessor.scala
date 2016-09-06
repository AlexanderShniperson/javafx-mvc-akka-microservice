package carsale.microservice.server.example

import akka.actor._
import carsale.microservice.api.example.ApiBaseMessage

object MessageProcessor {
  def props() = {
    Props(classOf[MessageProcessor])
  }
}

private class MessageProcessor extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg: ApiBaseMessage =>
  }
}
