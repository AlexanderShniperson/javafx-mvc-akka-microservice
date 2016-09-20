package carsale.microservice.server.example

import akka.actor._
//import carsale.microservice.api.example.ApiMessages.CarModelApi.GetCarModel
//import carsale.microservice.api.example.ApiMessages.CarTypeApi.GetCarType
import carsale.microservice.api.example.ApiMessages._
import carsale.microservice.server.example.crud._

object MessageProcessor {
  def props(): Props = {
    Props(classOf[MessageProcessor])
  }
}

private class MessageProcessor extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg: GetCarModel => context.actorOf(CarModelCrud.props()).forward(msg)
    case msg: GetCarType => context.actorOf(CarTypeCrud.props()).forward(msg)
  }
}
