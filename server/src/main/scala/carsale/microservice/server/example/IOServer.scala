package carsale.microservice.server.example


import akka.actor._
import akka.serialization.SerializationExtension
import carsale.microservice.api.example.ApiBaseMessage

object IOServer {
  def props(host: String, port: Int, messageProcessor: ActorRef) = {
    Props(classOf[IOServer], host, port, messageProcessor)
  }
}

private class IOServer(host: String, port: Int, messageProcessor: ActorRef) extends Actor with ActorLogging {

  import akka.io.Tcp.Bind
  import akka.io.{IO, Tcp}
  import akka.io.Tcp._
  import java.net.InetSocketAddress

  val serialization = SerializationExtension(context.system)
  val serializer = serialization.findSerializerFor(new ApiBaseMessage)

  override def receive: Receive = {
    case b@Bound(localAddress) => log.info(s"\\n\\n[ IOServer ] :: bounded to $host:$port")
    case x@CommandFailed(_: Bind) => throw new IllegalStateException(s"orig: $x")
    case Unbound => context become receive
    case Connected(remote, local) => sender() ! connect(local, remote, sender())
    case any => log.warning(s"bounded: Unhandled message received [$any]")
  }

  override def preStart() {
    IO(Tcp) ! Bind(self, new InetSocketAddress(host, port))
  }

  private def connect(local: InetSocketAddress, remote: InetSocketAddress, sender: ActorRef): Register = {
    val handler = context.actorOf(ConnectionHandler.props(sender, remote, messageProcessor, serializer))
    log.info(s"\n\n[ IOServer ] :: connection#${context.children.size} from: $remote\n")
    Register(handler)
  }
}
