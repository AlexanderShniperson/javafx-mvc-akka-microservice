package javafx.mvc.example.model

import javafx.beans.property._
import carsale.microservice.api.example.ApiBaseMessage
import carsale.microservice.api.example.ApiMessages.CarSaleApi.{CarSaleCreate, CarSaleUpdate}

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

  override def toCreateRequest(): ApiBaseMessage = {
    CarSaleCreate(
      ownerName = ownerName.get(),
      carModelId = carModel.get().id.get(),
      carTypeId = carType.get().id.get(),
      yearMade = yearMade.get(),
      mileage = mileage.get(),
      price = price.get())
  }

  override def toUpdateRequest(): ApiBaseMessage = {
    CarSaleUpdate(
      id = id.get(),
      ownerName = ownerName.get(),
      carModelId = carModel.get().id.get(),
      carTypeId = carType.get().id.get(),
      yearMade = yearMade.get(),
      mileage = mileage.get(),
      price = price.get())
  }
}
