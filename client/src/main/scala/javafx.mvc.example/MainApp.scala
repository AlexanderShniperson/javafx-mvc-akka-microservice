package javafx.mvc.example

import javafx.application.{Application, Platform}
import javafx.mvc.example.controller._
import javafx.scene.Scene
import javafx.stage.Stage
import util.FXFormLoader

import akka.actor._

object MainApp {

  import akka.actor.ActorSystem
  import com.typesafe.config.ConfigFactory
  import com.typesafe.config.Config

  var launcher: AppLauncher = _
  var config: Config = _
  var system: ActorSystem = _
  var ioConnection = Actor.noSender

  def main(args: Array[String]): Unit = {
    config = ConfigFactory.load().getConfig("CarSaleClient")
    system = ActorSystem("CarSaleClient", config)
    ioConnection = system.actorOf(IOClient.props("localhost", 10105))
    sys.addShutdownHook({
      MainApp.system.terminate()
    })
    launcher = new AppLauncher
    launcher.run(args)
  }

  def runLater(operation: => Unit): Unit = {
    Platform.runLater(new Runnable {
      def run(): Unit = {
        operation
      }
    })
  }
}

class AppLauncher extends Application {
  def run(args: Array[String]) {
    Application.launch(args: _*)
  }

  override def start(primaryStage: Stage): Unit = {
    val (view, _) = FXFormLoader.loadFX[MainFormControllerImpl]("/fxml/MainFormView.fxml")
    primaryStage.setScene(new Scene(view))
    primaryStage.show()
  }
}
