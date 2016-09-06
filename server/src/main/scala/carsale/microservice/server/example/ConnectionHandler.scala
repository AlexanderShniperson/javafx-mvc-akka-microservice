package carsale.microservice.server.example

import akka.actor._
import java.net.InetSocketAddress

import akka.serialization._
import akka.util.ByteString
import carsale.microservice.api.example._

import scala.annotation.tailrec

object ConnectionHandler {
  def props(connection: ActorRef, remote: InetSocketAddress, messageProcessor: ActorRef, serializer: Serializer): Props =
    Props(classOf[ConnectionHandler], connection, remote, messageProcessor, serializer)
}

private class ConnectionHandler(connection: ActorRef, remote: InetSocketAddress, messageProcessor: ActorRef, serializer: Serializer) extends Actor with ActorLogging {

  import akka.io.Tcp._

  override def receive: Receive = {
    case Received(data) ⇒
      serializer.fromBinary(data.toArray) match {
        case msg@ApiTransportMessage(bytes, fromActor, toActor) =>
          (fromActor, toActor) match {
            case (Some(from), None) => context.actorOf(MessageProxyActor.props(connection), name = getProxyActorName(from.hashCode))
            case (Some(from), Some(to)) =>
              (context.child(getProxyActorName(from.hashCode)), context.child(getProxyActorName(to.hashCode))) match {
                case (None, Some(findLocal)) => findLocal ! serializer.fromBinary(bytes)
                case (Some(findRemote), None) => connection ! Write(ByteString.fromArray(serializer.toBinary(msg)))
                case any => log.warning(s"[ ClientConnectionHandler ] received unchecked ApiTransportMessage #1 from/to <$any>")
              }
            case any => log.warning(s"[ ClientConnectionHandler ] received unchecked ApiTransportMessage #2 from/to <$any>")
          }
        case any => log.warning(s"[ ClientConnectionHandler ] received unplanned message <$any>")
      }

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

  def getProxyActorName(magicNumber: Int): String = {
    "MPA_" + magicNumber
  }

  private final def closeConnection() = {
    connection ! Close
    context stop self
  }
}
