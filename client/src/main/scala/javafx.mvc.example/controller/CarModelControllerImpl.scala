package javafx.mvc.example.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.collections.{FXCollections, ObservableList}
import javafx.mvc.example.MainApp
import javafx.mvc.example.model.CarModelModel
import javafx.mvc.example.util.FXLifeView
import akka.actor._
import carsale.microservice.api.example.ApiMessages._

class CarModelManager(ctrl: CarModelControllerImpl) extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg: GetCarModel => MainApp.ioConnection ! msg
    case msg: CarModelReply => MainApp.runLater({
      ctrl.items.add(CarModelModel.fromReply(msg))
    })
  }
}

class CarModelControllerImpl extends CarModelController with FXLifeView {
  val items: ObservableList[CarModelModel] = FXCollections.observableArrayList[CarModelModel]()

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    manager = Option(MainApp.system.actorOf(Props(classOf[CarModelManager], this)))
    tvItems.setItems(items)
    manager.foreach(_ ! GetCarModel(None))
  }
}
