package LogIn;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileController {

    public static Object readObject(String fileName){
        try{
            FileInputStream fs = new FileInputStream(fileName);
            ObjectInputStream os = new ObjectInputStream(fs);
            Object o = os.readObject();
            os.close();
            return o;
        }
        catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean writeObject(String fileName,Object o){
        try{
            FileOutputStream fs = new FileOutputStream(fileName,false);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(o);
            os.close();
            return true;
        }
        catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public static String readTextFromFile(String fileName){
        try{
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();
            String text = "";
            while(line != null){
                text += line + "\n";
                line = br.readLine();
            }
            fr.close();
            br.close();
            return text;
        }
        catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean writeTextToFile(String fileName,String text){
        try{
            FileWriter fw = new FileWriter(new File(fileName),false);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

            String[] input = text.split("\n");
            for(int i = 0; i < input.length; i++){
                pw.println(input[i]);
            }
            pw.close();
            return true;
        }
        catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean fileExist(String fileName){
        File file = new File(fileName);
        return (file.exists() && !file.isDirectory());
    }

    public static String[] getFiles(String directory){
        File folder = new File(directory);
        if(!folder.isDirectory()){return null;}
        File[] files = folder.listFiles();
        String[] names = new String[files.length];
        for(int i = 0; i < files.length; i++){
            names[i] = files[i].getName();
        }
        return names;
    }

    public static boolean createFile(String fileName){
        try{
            Path path = Paths.get(fileName);
            Files.write(path,new byte[0]);
            return true;
        }
        catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteFile(String fileName){
        try{
            Path path = Paths.get(fileName);
            Files.delete(path);
            return true;
        }
        catch (IOException e){
            return false;
        }
    }

    public static boolean createDirectory(String path){
        File directory = new File(path);

        if(!directory.exists()){
            try{
                return directory.mkdir();
            }
            catch(SecurityException e){
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static boolean deleteDirectory(String path){
        File f = new File(path);
        if(f.isDirectory()){
            for(File c : f.listFiles()){
                deleteDirectory(c.getPath());
            }
        }
        return f.delete();
    }

    public static String streamToString(InputStream in){
        if(in == null){
            return null;
        }

        try{
            String line = null;
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String output = "";
            while((line = br.readLine()) != null){
                output += line + "\n";
            }
            return (output.length() > 2)? output.substring(0,output.length()-1) : output;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean renameFile(String fileName,String replacement){
        File file1 = new File(fileName);
        File file2 = new File(replacement);
        return (file1.renameTo(file2));
    }

    public static boolean isDirectory(String fileName){
        return new File(fileName).isDirectory();
    }
}
