package javafx.mvc.example.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.collections.{FXCollections, ObservableList}
import javafx.mvc.example.MainApp
import javafx.mvc.example.model.CarTypeModel
import javafx.mvc.example.util.FXLifeView
import akka.actor._
import carsale.microservice.api.example.ApiMessages.CarTypeApi._

class CarTypeManager(ctrl: CarTypeControllerImpl) extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg: GetCarType => MainApp.ioConnection ! msg
    case msg: CarTypeReply => MainApp.runLater({
      ctrl.items.add(CarTypeModel.fromReply(msg))
    })
  }
}

class CarTypeControllerImpl extends CarTypeController with FXLifeView {
  val items: ObservableList[CarTypeModel] = FXCollections.emptyObservableList()

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    manager = Option(MainApp.system.actorOf(Props(classOf[CarTypeManager], this)))
    manager.foreach(_ ! GetCarType(None))
  }
}
