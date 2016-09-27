package carsale.microservice.api.example

import java.nio.{ByteBuffer, ByteOrder}
import akka.util.ByteString

trait MessageExtractor {
  private final val byteOrder = ByteOrder.LITTLE_ENDIAN
  private final val headerSize = 4

  def buildMessage(value: Array[Byte]): ByteString = {
    val length = value.length + headerSize
    val result = ByteString.newBuilder
    result.putLongPart(length, headerSize)(byteOrder)
    result.putBytes(value)
    val res = result.result()
    println(s"[MessageExtractor] buildMessage origMessage(${value.length}) calc($length) final(${res.length})")
    res
  }

  def extractMessage(value: Array[Byte]): List[Array[Byte]] = {
    var result = List.empty[Array[Byte]]
    var buffer = value
    var messageLength = 0
    var i = 0
    while (buffer.length > 0) {
      messageLength = ByteBuffer.wrap(buffer.take(headerSize)).order(byteOrder).getInt
      buffer = buffer.drop(headerSize)
      if (messageLength > 0 && buffer.length > 0) {
        val canTakeBytes = messageLength.min(buffer.length)
        result ::= buffer.take(canTakeBytes)
        println(s"[MessageExtractor] extractMessage length($messageLength) afterBuff(${buffer.length - canTakeBytes}) curMsg(${canTakeBytes})")
        buffer = buffer.drop(canTakeBytes)
      }
      i+=1
      messageLength = 0
      println(s">>> Iter($i) bufLen(${buffer.length})")
    }
    result
  }

}
