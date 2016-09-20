package javafx.mvc.example.model

import javafx.beans.property._
import carsale.microservice.api.example.ApiBaseMessage
import carsale.microservice.api.example.ApiMessages._

class CarTypeModel(aRecordState: RecordState.RecordState,
                   aId: Long,
                   aName: String) extends BaseModel(aRecordState, aId) {
  val name = new SimpleStringProperty(aName)

  setListenFields(Array(id, name))

  override def toResponse(): ApiBaseMessage = ???

  override def toCreateRequest(): ApiBaseMessage = {
    CarTypeCreate(
      name = name.get()
    )
  }

  override def toUpdateRequest(): ApiBaseMessage = {
    CarTypeUpdate(
      id = id.get(),
      name = name.get()
    )
  }
}
