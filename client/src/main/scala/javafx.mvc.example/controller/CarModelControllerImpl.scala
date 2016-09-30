package javafx.mvc.example.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.collections.{FXCollections, ObservableList}
import javafx.mvc.example.MainApp
import javafx.mvc.example.model.CarModelModel
import javafx.mvc.example.util.{FXBind, FXLifeView}

import akka.actor._
import carsale.microservice.api.example.ApiMessages._

class CarModelManager(ctrl: CarModelControllerImpl) extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg: GetCarModel => MainApp.ioConnection ! msg
    case msg: CarModelReply =>
      ctrl.addItem(CarModelModel.fromReply(msg))
  }
}

class CarModelControllerImpl extends CarModelController with FXLifeView {
  private val items: ObservableList[CarModelModel] = FXCollections.observableArrayList[CarModelModel]()

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    FXBind.bindStringObservableCVF[CarModelModel](colModelName, _.name)
    tvItems.setItems(items)

    manager = Option(MainApp.system.actorOf(Props(classOf[CarModelManager], this)))
    manager.foreach(_ ! GetCarModel(None))
  }

  def addItem(value: CarModelModel): Unit = {
    items.add(value)
  }
}
