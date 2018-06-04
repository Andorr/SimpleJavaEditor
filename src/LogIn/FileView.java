package LogIn;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileView extends TreeView<String> {

    private final URL FOLDER_ICON_PATH;
    private final URL FILE_ICON_PATH;
    private static final int ICON_SIZE = 20;

    private FileItem root;

    public FileView(String directory){
        System.out.println(directory);
        System.out.println(FileController.isDirectory(directory));
        //Setup
        FOLDER_ICON_PATH = this.getClass().getResource("images/Folder.png");
        FILE_ICON_PATH = this.getClass().getResource("images/FileIcon.png");
        this.setStyle("-fx-control-inner-background: #515151");

        //Initialize root
        Path path = Paths.get(directory);
        root = new FileItem(directory,path.getFileName().toString());
        root.setGraphic(FOLDER_ICON_PATH);
        this.setRoot(root);
        updateTree(root);
    }

    public void update(){
        root.getChildren().remove(0,root.getChildren().size());
        updateTree(root);
    }

    private void updateTree(FileItem r){
        String[] files = FileController.getFiles(r.getFullPath());
        ObservableList<TreeItem<String>> items = r.getChildren();
        if(files == null){ return; }
        r.setExpanded(r == root);
        for(int i = 0; i < files.length; i++){
            FileItem item = new FileItem(r.getFullPath() + "\\" + files[i],files[i]);
            items.add(item);
            if(FileController.isDirectory(r.getFullPath() + "\\" + files[i])){
                item.setGraphic(FOLDER_ICON_PATH);
                updateTree(item);
            }
            else{
                item.setGraphic(FILE_ICON_PATH);
            }
        }
    }

    public boolean changeDirectory(String directory){
        if(!FileController.isDirectory(directory)){
            return false;
        }

        //Sets up new directory
        this.setRoot(null); //Removes all the current children
        Path path = Paths.get(directory);
        root = new FileItem(directory,path.getFileName().toString()); //Sets up a new root
        root.setGraphic(FOLDER_ICON_PATH);
        updateTree(root);
        this.setRoot(root);
        this.setVisible(true);
        return true;
    }

    public boolean deleteItem(String path){
        return removeItem(path,root);
    }

    private boolean removeItem(String path,FileItem item){
        ObservableList<TreeItem<String>> items = item.getChildren();
        for(int i = 0; i < items.size(); i++){
            FileItem fileItem = (FileItem)items.get(i);
            if(fileItem.getFullPath().equals(path)){
                items.remove(i);
                return true;
            }
            else if(fileItem.getChildren().size() > 0){
                boolean status = removeItem(path,fileItem);
                if(status){return true;}
            }
        }
        return false;
    }

    public boolean isSelectedDirectory(){
        FileItem item = (FileItem)getSelectionModel().getSelectedItem();
        if(item == null){return false;}
        String dir = item.getFullPath();
        return FileController.isDirectory(dir);
    }

    public String getCurSelectedFileName(boolean isFullPath){
        FileItem item = (FileItem)getSelectionModel().getSelectedItem();
        if(item == null){return null;} //No item selected
        return (isFullPath)? item.getFullPath() : item.getValue();
    }

    public String getCurSelectedFolder(){
        if(isSelectedDirectory()){return getCurSelectedFileName(true);}
        FileItem item = (FileItem)getSelectionModel().getSelectedItem();
        if(item != null){ item = (FileItem)item.getParent(); }
        return (item == null)? null : item.getFullPath();
    }

    private class FileItem extends TreeItem<String>{
        private String fullPath;
        public FileItem(String fullPath,String path){
            super(path);
            this.fullPath = fullPath;
        }

        public String getFullPath(){return fullPath;}

        public void setGraphic(URL path){
            ImageView image = new ImageView(path.toString());
            image.setFitWidth(ICON_SIZE);
            image.setFitHeight(ICON_SIZE);
            this.setGraphic(image);
        }
    }

}
