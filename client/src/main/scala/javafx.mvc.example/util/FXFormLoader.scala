package javafx.mvc.example.util

import javafx.fxml.FXMLLoader
import javafx.scene.Parent

object FXFormLoader {

  /**
    * JavaFX form loader
    *
    * @param uri Path to fxml file
    * @tparam T generic type of controller
    * @return Tuple2 of View and Controller
    */
  def loadFX[T](uri: String): (Parent, T) = {
    val url = getClass.getResource(uri)
    val loader = new FXMLLoader(url)
    val root = loader.load().asInstanceOf[Parent]
    val controller = loader.getController[T]
    (root, controller)
  }
}