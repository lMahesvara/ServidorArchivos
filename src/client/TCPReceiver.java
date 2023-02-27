package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TCPReceiver implements Runnable{
    private Socket socket;
    private BufferedReader entrada;

    public TCPReceiver(Socket socket) throws IOException{
        this.socket = socket;
        this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    
    @Override
    public void run() {
        try {
            while(socket.isConnected()){
                //System.out.println("Esperando respuesta del servidor");
                System.out.println(entrada.readLine());
            }
        } catch (Exception e) {
        }
    }
    
}
