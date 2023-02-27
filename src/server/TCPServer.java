package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TCPServer {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        try {
            ServerSocket serverSocket = null;
            serverSocket = new ServerSocket(4444);
	    //System.out.println("estoy después de crear el socket");
            Socket clientSocket = null;

            while(true){
                clientSocket = serverSocket.accept();
                //System.out.println("estoy después de aceptar un cliente");
                executor.execute(new ClientManager(clientSocket)); 
            }
        } catch (Exception e) {
            System.err.println("Could not listen on port: 4444.");
            System.exit(1);
        }
    }
}
