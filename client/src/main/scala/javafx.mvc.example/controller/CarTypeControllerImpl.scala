package javafx.mvc.example.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.collections.{FXCollections, ObservableList}
import javafx.mvc.example.MainApp
import javafx.mvc.example.model.{CarTypeModel, RecordState}
import javafx.mvc.example.util.{FXBind, FXLifeView}

import akka.actor._
import carsale.microservice.api.example.ApiMessages._

class CarTypeManager(ctrl: CarTypeControllerImpl) extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg: GetCarType =>
      MainApp.ioConnection ! msg
    case msg: CarTypeReply =>
      ctrl.addItem(new CarTypeModel(RecordState.None, msg.id, msg.name))
  }
}

class CarTypeControllerImpl extends CarTypeController with FXLifeView {
  private val items: ObservableList[CarTypeModel] = FXCollections.observableArrayList[CarTypeModel]()

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    FXBind.bindStringObservableCVF[CarTypeModel](colTypeName, _.name)
    tvItems.setItems(items)

    manager = Option(MainApp.system.actorOf(Props(classOf[CarTypeManager], this)))
    manager.foreach(_ ! GetCarType(None))
  }

  def addItem(value: CarTypeModel): Unit = {
    items.add(value)
  }
}
