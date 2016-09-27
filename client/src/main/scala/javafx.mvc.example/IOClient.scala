package javafx.mvc.example

import java.net.InetSocketAddress

import akka.actor._
import akka.serialization._
import carsale.microservice.api.example.{MessageExtractor, MessageSerializer}

import scala.concurrent.Future

object IOClient {
  def props(serverHost: String, serverPort: Int) =
    Props(classOf[IOClient], serverHost, serverPort)
}

class IOClient(serverHost: String, serverPort: Int)
  extends Actor
    with ActorLogging {

  import akka.actor._
  import akka.io.Tcp._
  import akka.io.{IO, Tcp}
  import carsale.microservice.api.example.{ApiBaseMessage, MessageProxyRouterActor}
  import carsale.microservice.api.example.MessageProxyRouterActor.ApiOutgoingMessage

  val serialization = SerializationExtension(context.system)
  val serializer = serialization.findSerializerFor(new ApiBaseMessage)
  private val maxTimeoutCount = 12
  private var timeoutCount = maxTimeoutCount

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    retryConnect(false)
  }

  override def receive = {
    case cf@CommandFailed(_: Connect) ⇒
      log.warning(s"\n[IOClient] $cf from: ${sender()}")
      retryConnect()

    case x: Connect =>
      log.warning(s"\n[IOClient] $x from: ${sender()}")
      retryConnect()

    case c@Connected(remote, local) ⇒
      log.info(s"\n [IOClient] Connected($remote, $local)\n")
      val connection = sender()
      connection ! Register(self)
      val proxyRouter = context.actorOf(MessageProxyRouterActor.props(connection, Actor.noSender, serializer), "cPR_" + (math.random * 10000))
      context become connected(connection, proxyRouter)

    case ReceiveTimeout =>
      retryConnect()

    case unhandled => log.error(s"[ IOClient ] receive Received unhandled: $unhandled")
  }

  private def connected(connection: ActorRef, proxyRouter: ActorRef): Receive = {
    case msg: Received =>
      log.info(s"[ IOClient ] ReceivedData(${msg.data.length})")
      proxyRouter ! msg
    //      timeoutCount = maxTimeoutCount

    case msg: ApiBaseMessage =>
      log.info(s"[ IOClient ] MessageToServer($msg)")
      proxyRouter.forward(ApiOutgoingMessage(msg, sender().path.toSerializationFormat, None))

    //    case ReceiveTimeout ⇒
    //      timeoutCount -= 1
    //      if (timeoutCount <= 0) {
    //        context.stop(self)
    //        log.warning(s"IOClient connected Received KeepAlive ReceiveTimeout")
    //      }

    case cc: ConnectionClosed =>
      log.info(s"IOClient ConnectionClosed: $cc")
      context become receive

    case unhandled => log.error(s"[ IOClient ] connected Received unhandled:$unhandled from ${sender()}")
  }

  private final def retryConnect(isNeedSleep: Boolean = true): Unit = {
    val conn = self
    import context.dispatcher
    implicit val as = context.system
    //Future {
    if (isNeedSleep) Thread.sleep(1000)
    IO(Tcp).tell(Connect(remoteAddress = new InetSocketAddress(serverHost, serverPort)), conn)
    //}
  }
}