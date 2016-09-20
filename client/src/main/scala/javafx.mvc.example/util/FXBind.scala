package javafx.mvc.example.util

object FXBind {

  import javafx.beans.property._
  import javafx.beans.value.ObservableValue
  import javafx.collections.FXCollections
  import javafx.scene.control._
  import javafx.scene.control.cell._
  import javafx.scene.text.Text
  import javafx.util.Callback
  import java.lang.{Boolean => JBoolean}
  import collection.JavaConversions._
  import javafx.scene.layout.Region

  def bindStringObservableCVF[R](cl: TableColumn[R, String], f: (R) => StringProperty) {
    cl.setCellValueFactory(new Callback[TableColumn.CellDataFeatures[R, String], ObservableValue[String]] {
      def call(p1: TableColumn.CellDataFeatures[R, String]): ObservableValue[String] =
        f(p1.getValue).asInstanceOf[ObservableValue[String]]
    })
  }

  def bindComboBoxCellFactory[R, L](col: TableColumn[R, L], values: () => List[L]) {
    col.setCellFactory(new Callback[TableColumn[R, L], TableCell[R, L]] {
      def call(p1: TableColumn[R, L]): TableCell[R, L] = {
        new ComboBoxTableCell[R, L](FXCollections.observableList[L](values()))
      }
    })
  }

  def bindCheckBoxCellFactory[R](col: TableColumn[R, JBoolean]) {
    col.setCellFactory(new Callback[TableColumn[R, JBoolean], TableCell[R, JBoolean]] {
      def call(p1: TableColumn[R, JBoolean]): TableCell[R, JBoolean] = {
        new CheckBoxTableCell()
      }
    })
  }

  def bindTextCellFactory[R](column: TableColumn[R, String]): Unit = {
    column.setCellFactory(new Callback[TableColumn[R, String], TableCell[R, String]] {
      def call(param: TableColumn[R, String]): TableCell[R, String] = {
        val cell = new TableCell[R, String]()
        val text = new Text()
        cell.setGraphic(text)
        cell.setPrefHeight(Region.USE_COMPUTED_SIZE)
        cell.setEditable(true)
        text.textProperty().bindBidirectional(cell.itemProperty())
        cell
      }
    })
  }

}
