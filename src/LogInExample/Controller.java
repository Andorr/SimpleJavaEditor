package LogInExample;

import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public void closeApplication(MouseEvent event){
        System.exit(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
