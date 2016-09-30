package carsale.microservice.api.example

import akka.actor._
import akka.util.ByteString

object MessageExtractor {

  case class MessageBuild(data: Array[Byte])

  case class MessageBuildReply(data: ByteString)

  case class MessageExtract(data: ByteString)

  case class MessageExtractReply(data: Array[Byte])

  def props(): Props = {
    Props(classOf[MessageExtractor])
  }
}

private class MessageExtractor extends Actor {

  import MessageExtractor._

  private final val byteOrder = java.nio.ByteOrder.LITTLE_ENDIAN
  private final val headerSize = 4
  private var buffer = ByteString.empty

  case object HasBytes

  override def receive: Receive = {
    case MessageBuild(data) =>
      context.parent ! MessageBuildReply(buildMessage(data))

    case MessageExtract(data) =>
      buffer ++= data
      extractMessage()

    case HasBytes =>
      extractMessage()
  }

  def buildMessage(value: Array[Byte]): ByteString = {
    val result = ByteString.newBuilder
    result.putLongPart(value.length, headerSize)(byteOrder)
    result.putBytes(value)
    result.result()
  }

  def extractMessage(): Unit = {
    if (buffer.length >= headerSize) {
      val messageLength = buffer.iterator.getLongPart(headerSize)(byteOrder).toInt
      buffer = buffer.drop(headerSize)
      if (messageLength > 0 && buffer.nonEmpty) {
        val canTakeBytes = messageLength.min(buffer.length)
        context.parent ! MessageExtractReply(buffer.take(canTakeBytes).toArray)
        buffer = buffer.drop(canTakeBytes)
      }
      if (buffer.nonEmpty)
        self ! HasBytes
    }
  }
}
