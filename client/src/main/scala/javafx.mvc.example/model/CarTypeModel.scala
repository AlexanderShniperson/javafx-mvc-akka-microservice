package javafx.mvc.example.model

import javafx.beans.property._
import akka.microservice.api.example.ApiBaseMessage

class CarTypeModel(aRecordState: RecordState.RecordState,
                   aId: Long,
                   aName: String) extends BaseModel(aRecordState, aId) {
  val name = new SimpleStringProperty(aName)

  setListenFields(Array(id, name))

  override def toResponse(): ApiBaseMessage = ???

  override def toCreateRequest(): ApiBaseMessage = ???

  override def toUpdateRequest(): ApiBaseMessage = ???
}
