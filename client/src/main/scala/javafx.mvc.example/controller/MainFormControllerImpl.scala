package javafx.mvc.example.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.event.{ActionEvent, Event, EventHandler}
import javafx.mvc.example.util.{FXLifeView, LifeViewClosed}
import javafx.scene.Parent
import javafx.scene.control.Tab

class MainFormControllerImpl extends MainFormController {
  override def initialize(location: URL, resources: ResourceBundle): Unit = {

  }

  private def openTab[T <: FXLifeView](name: String, content: Parent, controller: T): Unit = {
    val tab = new Tab(name)
    tab.setContent(content)
    tab.setClosable(true)
    tab.setOnClosed(onTabClose(controller))
    tabPane.getTabs.add(tab)
  }

  private def onTabClose[T <: FXLifeView](controller: T): EventHandler[Event] = {
    new EventHandler[Event] {
      def handle(p1: Event): Unit = {
        val closedTab = p1.getSource.asInstanceOf[Tab]
        closedTab.setContent(null)
        controller.manager.foreach(_ ! LifeViewClosed)
        tabPane.getTabs.remove(closedTab)
        System.gc()
      }
    }
  }

  override def onClickCarSale(event: ActionEvent): Unit = {

  }

  override def onClickEditCarModel(event: ActionEvent): Unit = {

  }

  override def onClickEditCarType(event: ActionEvent): Unit = {

  }
}
