package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientServer {
    public static String fileName;
    
    public static void main(String[] args) {
        Socket socket = null;
        PrintStream salida=null;
        BufferedReader entrada=null,teclado=null;
        
        try {
            socket = new Socket("localhost", 4444);
            new Thread(new TCPReceiver(socket)).start();
            new Thread(new UDPReceiver()).start();
            
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            teclado = new BufferedReader(new InputStreamReader(System.in));
            salida = new PrintStream(socket.getOutputStream());
            while(true){
               fileName = teclado.readLine();
                salida.println(fileName);
            }
            
            
        } catch (Exception e) {
        }finally
        {
            try {
                entrada.close();
                salida.close();
                teclado.close();   
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
}
