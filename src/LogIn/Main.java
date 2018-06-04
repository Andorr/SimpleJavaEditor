package LogIn;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.geom.Rectangle2D;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;

public class Main extends Application {

    private static final String APPLICATION = "file:\\\\\\C:\\Users\\Eier\\IdeaProjects\\JavaFX_Projects\\src\\LogIn\\application.fxml";
    private static final String STYLESHEET = "file:\\\\\\C:\\Users\\Eier\\IdeaProjects\\JavaFX_Projects\\src\\LogIn\\css\\java-keywords.css";

    @Override
    public void start(Stage primaryStage) throws Exception {
        showLogInWindow(primaryStage);
    }

    public static void showApplicationWindow(Stage primaryStage) throws Exception{
        primaryStage.close();
        primaryStage = new Stage();
        Parent root = FXMLLoader.load(new URL(APPLICATION));
        root.getChildrenUnmodifiable().get(0);

        //Implements java-keywords stylesheet
        String style = new URL(STYLESHEET).toExternalForm();
        root.getStylesheets().add(style);
        primaryStage.setTitle("And-E-rsTexT");
        primaryStage.setScene(new Scene(root,1080,720));
        primaryStage.hide();
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setOnCloseRequest(e -> System.out.println("Editor closed!"));
        primaryStage.centerOnScreen();
        primaryStage.show();

    }

    private void showLogInWindow(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("logIn.fxml"));
        root.getChildrenUnmodifiable().get(0);
        primaryStage.setTitle("Log In");
        primaryStage.setScene(new Scene(root,450,450));
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}
