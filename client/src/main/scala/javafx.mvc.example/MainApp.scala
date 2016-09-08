package javafx.mvc.example

import javafx.application.Application
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
    launcher = new AppLauncher
    launcher.run(args)
    config = ConfigFactory.load().getConfig("CarSaleClient")
    system = ActorSystem("CarSaleClient", config)
    sys.addShutdownHook({
      MainApp.system.terminate()
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
    MainApp.system.actorOf(IOClient.props("localhost", 10105))
  }
}
