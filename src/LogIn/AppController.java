package LogIn;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ResourceBundle;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

public class AppController implements Initializable {

    private Stage primaryStage;
    private String directory;
    private ArrayList<String> tabPaths = new ArrayList<>();

    private String curFile;
    private String curDir;

    //Components
    public FileView fileView;
    public TabPane tabPanel;
    public Label statusLabel;
    public Label mainClassLabel;
    public TextArea compilerTextArea;
    public VBox leftVBox;

    private ContextMenu cm;

    private Thread runThread;
    private Process process;
    private boolean wasTerminated = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String fileName = "Application.java";
        File file = new File(fileName);
        directory = file.getAbsolutePath().substring(0,file.getAbsolutePath().length()-fileName.length()) + "src\\LogIn\\files\\";

        System.out.println(directory);
        initializeFileView();
        compilerTextArea.setText("");
        cm = createContextMenu();


    }

    private void createNewTab(String fileName,String dir){
        Tab newTab = new Tab();
        newTab.setText(fileName);
        String text = FileController.readTextFromFile(dir + "\\" + fileName);

        //Sets up a code area
        CodeArea ca = new CodeArea();
        ca.setParagraphGraphicFactory(LineNumberFactory.get(ca)); //Adds LineNumbers
        //Adds java-color patterns
        ca.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(change -> {
                if(ca.getText().length() == 0){return;}
                ca.setStyleSpans(0, JavaKeywords.computeHighlighting(ca.getText()));
        });

        ca.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.TAB){
                addSOUT(ca);
            }
        });

        //Apply text to the codeArea
        if(text.length() == 0){
            ca.appendText(templateCode(fileName)); //Applies template code
        }
        else if(text != null){
            ca.appendText(text); //Applies the text from the file
        }
        System.out.println(text.length());
        newTab.setContent(ca);
        newTab.setOnCloseRequest(e ->{
            int index = tabPanel.getSelectionModel().getSelectedIndex();
            tabPaths.remove(index);
        });
        tabPaths.add(dir);
        tabPanel.getTabs().add(newTab);
    }

    private void openTab(){
        if(fileView.isSelectedDirectory()){
            return;
        }
        String selected = fileView.getCurSelectedFileName(false);
        String dir = fileView.getCurSelectedFolder();
        int tabIndex = tabExist(selected,dir);
        if(tabIndex < 0){
            createNewTab(selected,dir);
            tabPanel.getSelectionModel().selectLast();
        }
        else{
            tabPanel.getSelectionModel().select(tabIndex);
        }

        fileView.getScene().setOnKeyPressed(new KeyController());
    }

    private void closeTab(String fileName,String dir){
        ObservableList<Tab> items = tabPanel.getTabs();
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).getText().equals(fileName) && tabPaths.get(i).equals(dir)){
                items.remove(i);
                tabPaths.remove(i);
                break;
            }
        }
    }

    private void saveFile(String fileName){
        CodeArea ca = getCurTabTextArea();
        if(ca == null){return;}

        int tabIndex = tabPanel.getSelectionModel().getSelectedIndex();
        String path = tabPaths.get(tabIndex) + "\\" + fileName;
        String text = ca.getText();

        boolean status = FileController.writeTextToFile(path,text);
        System.out.println(fileName + " - File Saved: " + status);

        if(!status){
            Panels.showMessage("Error","File was not saved!");
        }
        else{
            statusLabel.setText(fileName + " - File saved! - " + Calendar.getInstance().getTime().toString());
        }
    }

    private void saveAsFile(String path){
        CodeArea ca = getCurTabTextArea();
        if(ca == null){return;}

        String text = ca.getText();

        boolean status = FileController.writeTextToFile(path,text);
        System.out.println(path + " - File Saved: " + status);

        if(!status){
            Panels.showMessage("Error","File was not saved!");
        }
        else{
            statusLabel.setText(path + " - File saved! - " + Calendar.getInstance().getTime().toString());

            Path p = Paths.get(path);
            int tabIndex = tabExist(p.getFileName().toString(),p.getParent().toString());
            if(tabIndex != -1){
                CodeArea tabCA = (CodeArea)tabPanel.getTabs().get(tabIndex).getContent();
                tabCA.deleteText(0,tabCA.getText().length());
                tabCA.appendText(FileController.readTextFromFile(path));
            }
        }
    }

    private boolean tryCompile(String fileName,String dir){
        addCompilingText("Starting to compile " + fileName);
        boolean status = compile(dir);
        if(status){
            setStatusText(fileName + " - Compiled successfully! -");
        }
        else{
            setStatusText(fileName + " - Compilation failed! - ");
        }
        return status;
    }

    private void tryRun(String fileName,String dir){
        //Compiling code first
        boolean isCompiled = tryCompile(fileName,dir);
        if(!isCompiled){return;} //If compilation failed, return

        //Resetting compiling text and runs
        addCompilingText(null);
        addCompilingText("Running " + directory + "/" + fileName);

        if(runThread != null && runThread.isAlive()){
            runThread.interrupt();
        }

        runThread = new Thread(() -> {
            boolean status = run(fileName,dir);
            if(status){
                Platform.runLater(() -> {
                    setStatusText(fileName + " - Compiled & ran successfully! - ");
                    changeMainClass(fileName,dir);
                });
            }
        });
        runThread.start();
    }

    private void deleteFile(String fileName,String dir){
        //Get confirmation
        boolean confirmation = Panels.confirmationPanel("Confirmation!","Are you sure you want to delete " + fileName + "?");
        if(!confirmation){return;}

        //Delete file
        boolean status = FileController.deleteFile(dir + "/" + fileName);
        if(status){
            closeTab(fileName,dir);
            setStatusText(fileName + " was deleted successfully!");
            System.out.println(dir);
            fileView.deleteItem(dir + "\\" + fileName);
        }
        else{
            Panels.showMessage("Error occurred!","Was not able to remove " + fileName + ".");
        }
    }

    public void deleteDirectory(String dir){
        //Get confirmation
        boolean confirmation = Panels.confirmationPanel("Confirmation!","Are you sure you want to delete the folder ?");
        if(!confirmation){return;}

        boolean status = FileController.deleteDirectory(dir);
        if(status){
            for(int i = tabPaths.size()- 1; i >= 0; i--){
                if(tabPaths.get(i).contains(dir)){
                    String fileName = tabPanel.getTabs().get(i).getText();
                    closeTab(fileName,tabPaths.get(i));
                }
            }
            setStatusText("Directory was deleted successfully");
            fileView.deleteItem(dir);
        }
        else{
            System.out.println(dir);
            Panels.showMessage("Error occurred!","Was not able to remove the folder.");
        }
    }

    private void tryRenameFile(String fileName,String dir){
        String newFileName = Panels.readInput("Rename file","Enter a new name for " + fileName + ".");
        if(newFileName == null){return;}
        if(!fileView.isSelectedDirectory()){
            newFileName += ".java";
        }

        boolean status = FileController.renameFile(dir + "/" + fileName,dir + "/" + newFileName);
        if(status){
            fileView.update();
            //Checks if the changed file's tab is open
            int index = tabExist(fileName,dir);
            if(index != -1){
                tabPanel.getTabs().get(index).setText(newFileName); //Changes the header of the renamed file's tab
            }

            //If renamed file was the curFile
            if(curFile.equals(fileName) && curDir.equals(dir)){
                changeMainClass(newFileName,dir);
            }
        }
        else{
            Panels.showMessage("Error","Could not rename file " + fileName + ".");
        }
    }

    //-------------Button Functions-----------------
    public void newFile(){
        String fileName = Panels.readInput("Creating File","Type in a name for the new file...");
        String curFolder = fileView.getCurSelectedFolder();
        String dir = (fileView.isSelectedDirectory())? fileView.getCurSelectedFileName(true) : (curFolder != null)? curFolder : directory;
        if(FileController.fileExist(dir + "/" + fileName + ".java")){
            Panels.showMessage("Information message","File with that name already exists!");
            fileName = null;
        }
        if(fileName == null){
            return;
        }

        fileName = fileName + ".java";
        System.out.println("FileName: " + fileName);
        boolean status = FileController.createFile(dir + "/" + fileName);
        if(status){
            System.out.println("File Created: " + status);
            createNewTab(fileName,dir);
            fileView.update();
        }

    }

    public void newDirectory(){
        String dir = Panels.readInput("Create new directory!","What is the name of the new directory?");
        if(dir == null){return;}
        String folder = (fileView.isSelectedDirectory())? fileView.getCurSelectedFileName(true) : directory;
        boolean status = FileController.createDirectory(folder + "\\" + dir);
        System.out.println("Directory Created: " + status);

        if(status){
            fileView.update();
        }
    }

    public void saveFile(){
        String fileName = getCurTabFileName();
        saveFile(fileName);
    }

    public void saveAsFile(){
        String fileName = Panels.chooseFile("Choose A File To Save",directory);
        if(fileName != null){
            saveAsFile(fileName);
        }
    }

    public void tryCompile(){
        String fileName = getCurTabFileName();
        String dir = tabPaths.get(getCurTabIndex());
        if(fileName == null){return;}
        tryCompile(fileName,dir);
    }

    public void tryRun(){
        String fileName = getCurTabFileName();
        String dir = tabPaths.get(getCurTabIndex());
        if(fileName == null){return;}
        tryRun(fileName,dir);
    }

    public void deleteFile(){
        String dir = fileView.getCurSelectedFolder();
        if(fileView.isSelectedDirectory()){
            //Checking if the directory to be deleted is the root-directory
            if(dir.equals(directory)){
                Panels.showMessage("Operation failed!","It is not possible to delete the root folder!");
                return;
            }
            //Deleting the directory
            deleteDirectory(dir);
        }
        else{
            String fileName = fileView.getCurSelectedFileName(false);
            deleteFile(fileName,dir);
        }
    }

    public void changeRootDirectory(){
        String directoryPath = Panels.chooseDirectory("Choose A Directory",directory);
        if(directoryPath == null){ return; }

        boolean status = fileView.changeDirectory(directoryPath);
        System.out.println("Directory Changed: " + status);
        directory = directoryPath;
        closeAllTabs();
        setStageTitle(directoryPath);
    }

    public void tryQuitProcess(){
        Process status = quitProcess();
        if(status != null){
            wasTerminated = true;
        }
    }

    //-------------Helper Functions-----------------
    private void initializeFileView(){
        fileView = new FileView(directory);
        fileView.setPrefHeight(1000);
        leftVBox.getChildren().add(fileView);
        fileView.setOnMouseClicked((e) -> {
            if(isDoubleClick(e)){
                openTab();
            }
            else if(e.getButton() == MouseButton.SECONDARY){
                cm.show(fileView.getSelectionModel().getSelectedItem().getGraphic(),e.getScreenX(),e.getScreenY());
            }
        });
    }

    private int tabExist(String fileName,String dir){
        for(int i = 0; i < tabPanel.getTabs().size(); i++){
            if(tabPanel.getTabs().get(i).getText().equals(fileName) && tabPaths.get(i).equals(dir)){
                return i;
            }
        }
        return -1;
    }

    private boolean isDoubleClick(MouseEvent e){
        return (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY);
    }

    private String getCurTabFileName(){
        Tab t = tabPanel.getSelectionModel().getSelectedItem();
        return (t == null)? null : t.getText();
    }

    private CodeArea getCurTabTextArea(){
        Tab t = tabPanel.getSelectionModel().getSelectedItem();
        return (t == null)? null : (CodeArea)t.getContent();
    }

    private int getCurTabIndex(){
        return tabPanel.getSelectionModel().getSelectedIndex();
    }

    private boolean compile(String dir){
        try{
            String classPath = String.format("-classpath %s/lib/", directory);
            Process process = Runtime.getRuntime().exec("javac " + dir + "\\*.java " + classPath);
            String error = FileController.streamToString(process.getErrorStream());
            addCompilingText(error + "\nProcess finished with exit code " + process.exitValue());
            return error.equals("");
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean run(String fileName,String dir){
        try{
            quitProcess();
            wasTerminated = false;

      /*      ProcessBuilder b = new ProcessBuilder("java", "-cp", dir + "; " + fileName.substring(0, fileName.length()-5));
            b.redirectError(ProcessBuilder.Redirect);*/

            System.out.println("Hello world :D");
            process = Runtime.getRuntime().exec("java -cp " + dir + "; " + fileName.substring(0,fileName.length()-5));
            System.out.println("Hello world :D 2");

            //Print output and errors
            Thread t = new Thread(() -> {
                try {
                    final InputStream stream = process.getInputStream();
                    final BufferedReader reader = new BufferedReader(
                            new InputStreamReader(stream)
                    );
                    String line = null;
                    while((line = reader.readLine()) != null) {
                        final String l = line;
                        addCompilingText(l);
                    }
                    reader.close();
                } catch(Exception e) {}

                String input = FileController.streamToString(process.getInputStream());
                String error = FileController.streamToString(process.getErrorStream());
                Platform.runLater(() ->{
                    if(wasTerminated){
                        addCompilingText("The process was terminated with exit code: " + process.exitValue());
                        wasTerminated = false;
                    }
                    else{
                        if(input != null) {
                            addCompilingText(input);
                            addCompilingText("\n");
                        }
                        if(error != null) {
                            addCompilingText(error);
                            addCompilingText("\n");
                        }

                        addCompilingText("Process finished with exit code " + process.exitValue());
                    }
                });
            });
            t.start();

            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private Process quitProcess(){
        if(process != null && process.isAlive()){
            return process.destroyForcibly();
        }
        return null;
    }

    private void addCompilingText(String text){
        //If text is null, clear all the text
        if(text == null){compilerTextArea.setText(""); return;}
        //If there is not text to add, return
        if(text.length() == 0){return;}
        //Appends the texts
        double scrollTop = compilerTextArea.getScrollTop();
        compilerTextArea.appendText(text + "\n");
        compilerTextArea.setScrollTop(scrollTop);
}

    private void setStatusText(String text){
        statusLabel.setText(text + " " + Calendar.getInstance().getTime().toString());
    }

    private ContextMenu createContextMenu(){
        ContextMenu cm = new ContextMenu();
        MenuItem item1 = new MenuItem("New File...");
        MenuItem item2 = new MenuItem("New Directory...");
        MenuItem item3 = new MenuItem("Rename...");
        MenuItem item4 = new MenuItem("Delete...");
        MenuItem item5 = new MenuItem("Compile");
        MenuItem item6 = new MenuItem("Run");
        MenuItem item7 = new MenuItem("Close");
        cm.getItems().addAll(item1,item2,item3,item4,item5,item6,item7);

        for(int i = 0; i < cm.getItems().size(); i++){
            MenuItem item = cm.getItems().get(i);
            item.setStyle("-fx-text-fill: white");
        }
        cm.setOnAction(e -> {
            String action = ((MenuItem)e.getTarget()).getText();
            runAction(action);
        });
        return cm;
    }

    private void runAction(String action){
        String fileName = fileView.getCurSelectedFileName(false);
        String dir = fileView.getCurSelectedFolder();
        if(!action.equals("New File...") && fileName == null){
            return;
        }
        System.out.println("Action: " + action);
        switch (action){
            case "New File...":
                newFile();
                break;
            case "New Directory...":
                newDirectory();
                break;
            case "Rename...":
                tryRenameFile(fileName,dir);
                break;
            case "Delete...":
                deleteFile();
                break;
            case "Compile":
                tryCompile(fileName,dir);
                break;
            case "Run":
                tryRun(fileName,dir);
                break;
            case "Close":
                cm.hide();
                break;
        }
    }

    private void setStageTitle(String text){
        if(primaryStage == null){
            primaryStage = (Stage)tabPanel.getScene().getWindow();
        }
        primaryStage.setTitle("The Ultimate Java-TextEditor - " + text);
    }

    private String templateCode(String fileName){
        String className = fileName.substring(0,fileName.length()-5);
        String sampleCode = "public class " + className + "{\n" +
                "\n" +
                "}";
        return sampleCode;
    }

    private void closeAllTabs(){
        ObservableList<Tab> tabs = tabPanel.getTabs();
        for(int i = tabs.size() - 1; i >= 0; i--){
            tabs.remove(i);
            tabPaths.remove(i);
        }
    }

    private void changeMainClass(String fileName,String dir){
        mainClassLabel.setText(fileName.substring(0,fileName.length()-5));
        mainClassLabel.setStyle("-fx-text-fill: #FFFFFF");
        curFile = fileName;
        curDir = dir;
    }

    private void addSOUT(CodeArea ca){

        if(ca.isFocused()){
            String line = ca.getText(ca.getCurrentParagraph());
            if(line.contains("sout")){
                for(int i = 0; i < 5; i++){
                    ca.deletePreviousChar();
                }


                ca.insertText(ca.getCurrentParagraph(), ca.getCaretColumn(),"System.out.println()");
                ca.moveTo(ca.getCaretPosition()-1);
            }
        }
    }

    private class KeyController implements EventHandler<KeyEvent>{

        @Override
        public void handle(KeyEvent event) {
            //Save - CTRL & S
            if(event.isControlDown() && event.getCode() == KeyCode.S){
                saveFile();
            }
            //Compile - LEFTSHIFT & F9
            else if(event.isShiftDown() && event.getCode() == KeyCode.F9){
                tryCompile();
            }
            //Run - LEFTSHIFT & F10
            else if(event.isShiftDown() && event.getCode() == KeyCode.F10){
                if(curFile == null && curDir == null){
                    tryRun();
                }
                else{
                    tryRun(curFile,curDir);
                }
            }
            //Delete file - DELETE
            else if(event.getCode() == KeyCode.DELETE){
                deleteFile();
            }
        }
    }

}
