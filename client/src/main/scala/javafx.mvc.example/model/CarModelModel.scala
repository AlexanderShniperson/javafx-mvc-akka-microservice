package javafx.mvc.example.model

import javafx.beans.property._

import carsale.microservice.api.example.ApiBaseMessage
import carsale.microservice.api.example.ApiMessages._

object CarModelModel {
  def fromReply(reply: CarModelReply): CarModelModel = {
    new CarModelModel(RecordState.None, reply.id, reply.name)
  }
}

class CarModelModel(aRecordState: RecordState.RecordState,
                    aId: Long,
                    aName: String) extends BaseModel(aRecordState, aId) {
  val name = new SimpleStringProperty(aName)

  setListenFields(Array(id, name))

  override def toResponse(): ApiBaseMessage = ???

  override def toCreateRequest(): ApiBaseMessage = {
    CarModelCreate(
      name = name.get()
    )
  }

  override def toUpdateRequest(): ApiBaseMessage = {
    CarModelUpdate(
      id = id.get(),
      name = name.get()
    )
  }
}
