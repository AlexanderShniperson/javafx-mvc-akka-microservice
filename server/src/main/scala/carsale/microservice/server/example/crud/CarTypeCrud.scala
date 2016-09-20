package carsale.microservice.server.example.crud

import akka.actor._
//import carsale.microservice.api.example.ApiMessages.CarTypeApi.{CarTypeReply, GetCarType}
import carsale.microservice.api.example.ApiMessages._

object CarTypeCrud {
  def props(): Props = {
    Props(classOf[CarTypeCrud])
  }
}

private class CarTypeCrud extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg: GetCarType =>
      val replyTo = sender()
      1.to(10).foreach { x => replyTo ! CarTypeReply(x, s"Car type #$x") }
  }
}
