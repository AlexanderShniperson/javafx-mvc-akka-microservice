package carsale.microservice.api.example

@SerialVersionUID(1L)
class ApiBaseMessage extends java.io.Serializable

@SerialVersionUID(1L)
case class ApiIncomingMessage(data: Array[Byte], fromActor: String, toActor: Option[String]) extends ApiBaseMessage

@SerialVersionUID(1L)
case class RemoteTerminated(from: Option[String]) extends ApiBaseMessage

object ApiMessages {

  object CarModelApi {

    /**
      * Request car model, if 'id' is defined then returns only one 'CarModelReply' else returns many 'CarModelReply'
      *
      * @param id A car model id at database
      */
    case class GetCarModel(id: Option[Long] = None) extends ApiBaseMessage

    /**
      * Response message represents car model record
      *
      * @param id   id of record
      * @param name Name of model
      */
    case class CarModelReply(id: Long, name: String) extends ApiBaseMessage

    /**
      * Request to create car model
      *
      * @param name A name of model
      */
    case class CarModelCreate(name: String) extends ApiBaseMessage

    /**
      * Response of created car model
      *
      * @param data Created object
      */
    case class CarModelCreateReply(data: CarModelReply) extends ApiBaseMessage

    /**
      * Request to update car model
      *
      * @param id   id of record that need to be changed
      * @param name updated car model name
      */
    case class CarModelUpdate(id: Long, name: String) extends ApiBaseMessage

    /**
      * Response of updated car model
      *
      * @param data updated object
      */
    case class CarModelUpdateReply(data: CarModelReply) extends ApiBaseMessage

    /**
      * Request to remove car model
      *
      * @param id id of record
      */
    case class CarModelRemove(id: Long) extends ApiBaseMessage

    /**
      * Response of removed car model
      *
      * @param id id of record
      */
    case class CarModelRemoveReply(id: Long) extends ApiBaseMessage

  }

  object CarTypeApi {

    /**
      * Request car type, if 'id' is defined then returns only one 'CarTypeReply' else returns many 'CarTypeReply'
      *
      * @param id A car type id at database
      */
    case class GetCarType(id: Option[Long] = None) extends ApiBaseMessage

    /**
      * Response message represents car type record
      *
      * @param id   id of record
      * @param name Name of type
      */
    case class CarTypeReply(id: Long, name: String) extends ApiBaseMessage

    /**
      * Request to create car type
      *
      * @param name A name of type
      */
    case class CarTypeCreate(name: String) extends ApiBaseMessage

    /**
      * Response of created car type
      *
      * @param data Created object
      */
    case class CarTypeCreateReply(data: CarTypeReply) extends ApiBaseMessage

    /**
      * Request to update car type
      *
      * @param id   id of record that need to be changed
      * @param name updated car type name
      */
    case class CarTypeUpdate(id: Long, name: String) extends ApiBaseMessage

    /**
      * Response of updated car type
      *
      * @param data updated object
      */
    case class CarTypeUpdateReply(data: CarTypeReply) extends ApiBaseMessage

    /**
      * Request to remove car type
      *
      * @param id id of record
      */
    case class CarTypeRemove(id: Long) extends ApiBaseMessage

    /**
      * Response of removed car type
      *
      * @param id id of record
      */
    case class CarTypeRemoveReply(id: Long) extends ApiBaseMessage

  }

  object CarSaleApi {

    case class GetCarSale(id: Option[Long] = None) extends ApiBaseMessage

    case class CarSaleReply(id: Long, ownerName: String, carModel: CarModelApi.CarModelReply, carType: CarTypeApi.CarTypeReply, yearMade: Int, mileage: Int, price: BigDecimal) extends ApiBaseMessage

    case class CarSaleCreate(ownerName: String, carModel: CarModelApi.CarModelReply, carType: CarTypeApi.CarTypeReply, yearMade: Int, mileage: Int, price: BigDecimal) extends ApiBaseMessage

    case class CarSaleCreateReply(data: CarSaleReply) extends ApiBaseMessage

    case class CarSaleUpdate(id: Long, ownerName: String, carModel: CarModelApi.CarModelReply, carType: CarTypeApi.CarTypeReply, yearMade: Int, mileage: Int, price: BigDecimal) extends ApiBaseMessage

    case class CarSaleUpdateReply(data: CarSaleReply) extends ApiBaseMessage

    case class CarSaleRemove(id: Long) extends ApiBaseMessage

    case class CarSaleRemoveReply(id: Long) extends ApiBaseMessage

  }

}
