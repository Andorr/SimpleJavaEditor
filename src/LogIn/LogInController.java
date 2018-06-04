package LogIn;

import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.ResourceBundle;

public class LogInController implements Initializable {

    public AnchorPane window;
    public Label exitButton;
    public Label logInErrorText;
    public Label signUp_UserNameError;
    public Label signUp_PasswordError;
    public Pane logInPanel;
    public Pane signUpPanel;
    public Pane loadingPanel;
    public Pane welcomePanel;
    public TextField logIn_UserName;
    public TextField logIn_Password;
    public TextField signUp_UserName;
    public TextField signUp_Password;
    public TextField signUp_ConfirmPassword;
    private boolean toggleExitButton = false;

    private double initialX;
    private double initialY;

    private final String DIRECTORY = "C:\\Users\\Eier\\IdeaProjects\\JavaFX_Projects\\src\\LogIn\\users\\";

    public void closeApplication(MouseEvent event){
        toggleExitButton();
        System.exit(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addDraggableNode(window);

        logIn_Password.setOnKeyPressed((e) -> onEnterPressed(e,true));
        logIn_UserName.setOnKeyPressed((e) -> onEnterPressed(e,true));
        signUp_UserName.setOnKeyPressed((e) -> onEnterPressed(e,false));
        signUp_Password.setOnKeyPressed((e) -> onEnterPressed(e,false));
        signUp_ConfirmPassword.setOnKeyPressed((e) -> onEnterPressed(e,false));
    }

    public void toggleExitButton(){
        Paint color = (!toggleExitButton)? Paint.valueOf("gray") : Paint.valueOf("white");
        exitButton.setTextFill(color);
        toggleExitButton = !toggleExitButton;
    }

    public void togglePanels(){
        boolean isLogIn = logInPanel.isVisible();
        logInPanel.setVisible(!isLogIn);
        signUpPanel.setVisible(isLogIn);
        welcomePanel.setVisible(false);

        //Disables error message-labels
        signUp_UserNameError.setVisible(false);
        signUp_PasswordError.setVisible(false);
        logInErrorText.setVisible(false);
    }

    public void tryLogIn(){
        logInPanel.setVisible(false);
        logInErrorText.setVisible(false);
        loadingPanel.setVisible(true);

        String userName = logIn_UserName.getText();
        String password = hashString(logIn_Password.getText());
        UserID id = (UserID)FileController.readObject(DIRECTORY + userName + ".dat");

        //Check if the userName exists
        if(id == null) {
            System.out.println("Log In Failed");
            logInErrorText.setVisible(true);
            logInPanel.setVisible(true);
            loadingPanel.setVisible(false);
            return;
        }

        System.out.println("Username: " + userName + ", Password: " + password);
        System.out.println("Username: " + id.userName + ", Password: " + id.password);

        //Checks if userName and password is correct
        boolean status = id.equals(userName,password);

        //If credentials are correct - log in
        if(status){
            System.out.println("Logged In!");
            sleep(2000);
            try{
                Stage s = (Stage)window.getScene().getWindow();
                System.out.println("Changing scene");
                Main.showApplicationWindow(s);
            }
           catch (Exception e){
                e.printStackTrace();
           }
        }
        //If not
        else{
            System.out.println("Incorrect credentials");
            logInErrorText.setVisible(true);
            logInPanel.setVisible(true);
            loadingPanel.setVisible(false);
        }
    }

    public void tryRegister(){
        String userName = signUp_UserName.getText();
        String password = signUp_Password.getText();
        String confirmedPassword = signUp_ConfirmPassword.getText();

        //Disable/Enable components
        signUp_UserNameError.setVisible(false);
        signUp_PasswordError.setVisible(false);

        //Checks if userName exists
        if(FileController.fileExist(DIRECTORY + userName + ".dat")){
            System.out.println("Username already exists");
            signUp_UserNameError.setVisible(true);
            return;
        }

        //Checks if the password is written correctly
        if(!password.equals(confirmedPassword)){
            System.out.println("Typed passwords are not equal");
            signUp_PasswordError.setVisible(true);
            return;
        }

        //Register user
        UserID id = new UserID(userName,hashString(password));
        boolean status = FileController.writeObject(DIRECTORY + userName + ".dat",id);

        //If the registration was successful
        if(status){
            System.out.println(userName + " is now registered!");
            signUpPanel.setVisible(false);
            welcomePanel.setVisible(true);
        }
        else{
            System.out.println("Was not able to register!");
        }
    }

    //-------Helper Functions---------
    private void addDraggableNode(final Node node) {

        node.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    initialX = me.getSceneX();
                    initialY = me.getSceneY();
                }
            }
        });

        node.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    node.getScene().getWindow().setX(me.getScreenX() - initialX);
                    node.getScene().getWindow().setY(me.getScreenY() - initialY);
                }
            }
        });
    }

    private void onEnterPressed(KeyEvent e,boolean enableLogIn){
            if(e.getCode() == KeyCode.ENTER){
                if(enableLogIn){tryLogIn();}
                else{tryRegister();}
            }
    }

    private String hashString(String input){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        }
        catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return input;
    }

    private static void sleep(long time){
        try{
            Thread.sleep(time);
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}
