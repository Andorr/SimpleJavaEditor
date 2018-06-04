package LogIn;

import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

import java.io.File;
import java.util.Optional;

public class Panels {

    public static String readInput(String title,String message){
        TextInputDialog w = new TextInputDialog();
        w.setTitle(title);
        w.setContentText(message);
        w.initModality(Modality.APPLICATION_MODAL);
        Optional<String> result = w.showAndWait();
        return (result.isPresent())? result.get() : null;
    }

    public static void showMessage(String title,String message){
        Alert window = new Alert(Alert.AlertType.INFORMATION);
        window.setTitle(title);
        window.setHeaderText(null);
        window.setContentText(message);
        window.initModality(Modality.APPLICATION_MODAL);
        window.showAndWait();
    }

    public static boolean confirmationPanel(String title, String message){
        Alert window = new Alert(Alert.AlertType.CONFIRMATION);
        window.setTitle(title);
        window.setHeaderText(null);
        window.setContentText(message);
        window.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = window.showAndWait();
        return (result.get() == ButtonType.OK);
    }

    public static String chooseDirectory(String title,String startDirectory){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(new File(startDirectory));
        chooser.setTitle(title);

        File directory = chooser.showDialog(null);
        return (directory == null)? null : directory.getPath();
    }

    public static String chooseFile(String title,String startDirectory){
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(startDirectory));
        chooser.setTitle(title);

        File file = chooser.showSaveDialog(null);
        return (file == null)? null : file.getPath();
    }
}
