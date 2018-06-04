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

    private static final String APPLICATION = "..\\LogIn\\application.fxml";
    private static final String STYLESHEET = "css\\java-keywords.css";

    @Override
    public void start(Stage primaryStage) throws Exception {
        showApplicationWindow(primaryStage);
    }

    public static void showApplicationWindow(Stage primaryStage) throws Exception{
        primaryStage.close();
        primaryStage = new Stage();
        Parent root = FXMLLoader.load(new URL(Main.class.getResource(APPLICATION).toString()));
        root.getChildrenUnmodifiable().get(0);

        //Implements java-keywords stylesheet
        String style = new URL(Main.class.getResource(STYLESHEET).toString()).toExternalForm();
        root.getStylesheets().add(style);
        primaryStage.setTitle("And-E-rsTexT");
        primaryStage.setScene(new Scene(root,1080,720));
        primaryStage.hide();
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setOnCloseRequest(e -> System.out.println("Editor closed!"));
        primaryStage.centerOnScreen();
        primaryStage.show();

    }

    public static void main(String[] args){
        launch(args);
    }
}
