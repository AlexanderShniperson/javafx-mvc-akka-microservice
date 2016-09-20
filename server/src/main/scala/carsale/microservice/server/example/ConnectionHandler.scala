package carsale.microservice.server.example

import akka.actor._
import java.net.InetSocketAddress
import akka.serialization._
import carsale.microservice.api.example._

object ConnectionHandler {
  def props(connection: ActorRef, remote: InetSocketAddress, messageProcessor: ActorRef, serializer: Serializer): Props =
    Props(classOf[ConnectionHandler], connection, remote, messageProcessor, serializer)
}

private class ConnectionHandler(connection: ActorRef, remote: InetSocketAddress, messageProcessor: ActorRef, serializer: Serializer) extends Actor with ActorLogging {

  import akka.io.Tcp._

  val proxyRouter = context.actorOf(MessageProxyRouterActor.props(connection, messageProcessor, serializer))

  override def receive: Receive = {
    case Received(data) ⇒
      proxyRouter ! serializer.fromBinary(data.toArray)

    case ReceiveTimeout ⇒
      log.info(s"ConnectionHandler: Closing connection by receive timeout [$remote]")
      closeConnection()

    case Close ⇒
      log.info(s"ConnectionHandler: Closing connection by close request [$remote]")
      closeConnection()

    case cc: ConnectionClosed ⇒
      log.info(s"ConnectionHandler: Closing connection by $cc request [$remote]")
      closeConnection()

    case unhandled ⇒
      log.error(s"unhandled event received <$unhandled>")
  }

  private final def closeConnection() = {
    connection ! Close
    context stop self
  }
}
