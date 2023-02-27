package server;

import java.io.File;

public class FileHandler {
    
    String PATH = "C:\\Users\\erick\\Documents\\prueba";
    File file = new File(PATH);

    public String[] getFilesNames() {
        return file.list();
    }

    public File getFile(String fileName) {
        file = new File(
        PATH+ "\\" + fileName);
        if (file.isFile() && file.getName().equals(fileName)) {
            return file;
        }
        return null;
    }
}
