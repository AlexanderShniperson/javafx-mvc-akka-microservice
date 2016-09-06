package javafx.mvc.example.model

import javafx.beans.property._
import javafx.beans.value.{ChangeListener, ObservableValue}

import carsale.microservice.api.example.ApiBaseMessage

object RecordState extends Enumeration {
  type RecordState = Value
  val None, New, Changed, Removed = Value
}

abstract class BaseModel(aRecordState: RecordState.RecordState, aId: Long) {
  val id: ObjectProperty[Long] = new SimpleObjectProperty(aId)
  private val listenFields = collection.mutable.ArrayBuffer.empty[ObservableValue[_]]
  private val recordState: ObjectProperty[RecordState.RecordState] = new SimpleObjectProperty[RecordState.RecordState](aRecordState)
  val isHasChanges = new ReadOnlyBooleanWrapper(false)

  isHasChanges.bind(recordState.isNotEqualTo(RecordState.None))

  val listener = new ChangeListener[Any] {
    def changed(p1: ObservableValue[_ <: Any], p2: Any, p3: Any) {
      setRecordState(RecordState.Changed)
    }
  }

  def getRecordState = recordState.get

  def setListenFields(fields: Array[ObservableValue[_]]): Unit = {
    detachChangeListener()
    listenFields.clear()
    listenFields ++= fields
    attachChangeListener()
  }

  def setRecordState(value: RecordState.RecordState): RecordState.RecordState = {
    recordState.set(recordState.get() match {
      case RecordState.None ⇒ value
      case RecordState.New | RecordState.Changed ⇒ if (Array(RecordState.Removed, RecordState.None).contains(value)) value else recordState.get()
      case _ ⇒ recordState.get()
    })
    recordState.get()
  }

  def detachChangeListener(): Unit = {
    listenFields.foreach(r => {
      r.removeListener(listener)
      r match {
        case op: ObjectProperty[_] =>
          op.get() match {
            case bm: BaseModel =>
              bm.isHasChanges.removeListener(listener)
            case _ =>
          }
        case _ =>
      }
    })
  }

  def attachChangeListener(): Unit = {
    listenFields.foreach(r => {
      r.addListener(listener)
      r match {
        case op: ObjectProperty[_] =>
          op.get() match {
            case bm: BaseModel =>
              bm.isHasChanges.addListener(listener)
            case _ =>
          }
        case _ =>
      }
    })
  }

  def toResponse(): ApiBaseMessage

  def toCreateRequest(): ApiBaseMessage

  def toUpdateRequest(): ApiBaseMessage

}
