package carsale.microservice.server.example.crud

import akka.actor._
//import carsale.microservice.api.example.ApiMessages.CarModelApi.{CarModelReply, GetCarModel}
import carsale.microservice.api.example.ApiMessages._

object CarModelCrud {
  def props(): Props = {
    Props(classOf[CarModelCrud])
  }
}

private class CarModelCrud extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg: GetCarModel =>
      val replyTo = sender()
      1.to(10).foreach { x => replyTo ! CarModelReply(x, s"Car model #$x") }
  }
}
