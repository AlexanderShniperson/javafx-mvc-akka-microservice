package javafx.mvc.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.mvc.example.model.CarTypeModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public abstract class CarTypeController implements Initializable {
    @FXML
    protected TableView<CarTypeModel> tvItems;

    @FXML
    protected TableColumn<CarTypeModel, String> colTypeName;
}