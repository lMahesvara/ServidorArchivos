package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientManager implements Runnable {

    private Socket clientSocket;
    private BufferedReader in;
    private PrintStream salida;
    private FileHandler fileHandler;

    public ClientManager(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        salida = new PrintStream(clientSocket.getOutputStream());
        fileHandler = new FileHandler();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String entrada = in.readLine();
                if(entrada == null) continue;
                System.out.println(entrada);
                
                //Manejar solicitudes
                if(entrada.equalsIgnoreCase("lista")){
                    String msg = "";
                    for(String file: fileHandler.getFilesNames()){
                        msg += file + "\n";
                    }
                    salida.print(msg);
                    continue;
                }
                
                File file = fileHandler.getFile(entrada);
                if(file == null){
                    salida.print("No existe un archivo con ese nombre");
                    continue;
                }
                new UDPSender(file).enviarArchivo();
                
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                salida.close();
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

}
