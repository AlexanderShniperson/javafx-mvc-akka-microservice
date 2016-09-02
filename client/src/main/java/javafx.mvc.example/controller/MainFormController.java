package javafx.mvc.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;

public abstract class MainFormController implements Initializable {
    @FXML
    protected TabPane tabPane;

    @FXML
    abstract void onClickCarSale(ActionEvent event);

    @FXML
    abstract void onClickEditCarModel(ActionEvent event);

    @FXML
    abstract void onClickEditCarType(ActionEvent event);
}
