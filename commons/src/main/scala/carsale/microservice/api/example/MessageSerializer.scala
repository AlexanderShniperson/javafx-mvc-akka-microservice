package carsale.microservice.api.example

import akka.serialization.Serializer
import scala.util.{Failure, Success, Try}

trait MessageSerializer {
  def serializeMsg(value: AnyRef)(implicit serializer: Serializer): Option[Array[Byte]] = {
    Try {
      serializer.toBinary(value)
    } match {
      case Success(s) => Some(s)
      case Failure(f) =>
        println(s"[MessageSerializer] serializeMsg Error ${f.getLocalizedMessage}")
        None
    }
  }

  def deserializeMsg(value: Array[Byte])(implicit serializer: Serializer): Option[AnyRef] = {
    Try {
      serializer.fromBinary(value)
    } match {
      case Success(s) => Some(s)
      case Failure(f) =>
        println(s"[MessageSerializer] deserializeMsg Error ${f.getLocalizedMessage}")
        None
    }
  }

  def wrap2Incoming(data: AnyRef, from: String, to: Option[String])(implicit serializer: Serializer): Array[Byte] = {
    Try {
      serializeMsg(ApiIncomingMessage(serializeMsg(data).get, from, to)).get
    } match {
      case Success(s) => s
      case Failure(f) => Array.empty
    }
  }
}
