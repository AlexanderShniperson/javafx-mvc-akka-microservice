package javafx.mvc.example.model

import javafx.beans.property._

import akka.microservice.api.example.ApiMessages.CarModelApi.CarModelReply
import akka.microservice.api.example.ApiMessages.CarTypeApi.CarTypeReply
import carsale.microservice.api.example.ApiBaseMessage

class CarSaleModel(aRecordState: RecordState.RecordState,
                   aId: Long,
                   aOwnerName: String,
                   aCarModel: CarModelModel,
                   aCarType: CarTypeModel,
                   aYearMade: Int,
                   aMileage: Int,
                   aPrice: BigDecimal) extends BaseModel(aRecordState, aId) {
  val ownerName = new SimpleStringProperty(aOwnerName)
  val carModel = new SimpleObjectProperty[CarModelModel](aCarModel)
  val carType = new SimpleObjectProperty[CarTypeModel](aCarType)
  val yearMade = new SimpleIntegerProperty(aYearMade)
  val mileage = new SimpleIntegerProperty(aMileage)
  val price = new SimpleDoubleProperty(aPrice.toDouble)

  setListenFields(Array(id, ownerName, carModel, carType, yearMade, mileage, price))

  override def toResponse(): ApiBaseMessage = ???

  override def toCreateRequest(): ApiBaseMessage = ???

  override def toUpdateRequest(): ApiBaseMessage = ???
}
