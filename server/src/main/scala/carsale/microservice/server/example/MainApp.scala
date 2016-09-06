package carsale.microservice.server.example

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.{Config, ConfigFactory}

object MainApp {
  var config: Config = _
  var system: ActorSystem = _

  def main(args: Array[String]): Unit = {
    config = ConfigFactory.load().getConfig("CarSaleServer")
    system = ActorSystem("CarSaleServer", config)
    sys.addShutdownHook({
      MainApp.system.terminate()
    })
    system.actorOf(IOServer.props(config.getString("io.hostName"), config.getInt("io.hostPort"), system.actorOf(MessageProcessor.props())))
  }
}
