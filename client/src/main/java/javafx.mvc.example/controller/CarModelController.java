package javafx.mvc.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.mvc.example.model.CarModelModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public abstract class CarModelController implements Initializable {
    @FXML
    protected TableView<CarModelModel> tvItems;

    @FXML
    protected TableColumn<CarModelModel, String> colModelName;
}
