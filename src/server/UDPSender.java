/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package server;

/**
 *
 * @author tonyd
 */
import java.io.File;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPSender {

    private final int PACKET_SIZE = 60000;
    private final int TIMEOUT_MS = 5000;
    private final int MAX_TRIES = 3;
    private int EXTRA_BYTES = 4;
    private File file;

    public UDPSender(File file) {
        this.file = file;
    }

    public void enviarArchivo(){
        try {
            String hostName = "localhost";
            int port = 12345;
            
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(0);
            InetAddress address = InetAddress.getByName(hostName);
            
            //String filePath = "D:\\Documents\\videos\\entrevista_229254.mp3";
            byte[] messageBytes  = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            System.out.println(messageBytes.length);
            
            int totalPackets = (int) Math.ceil((double) messageBytes.length / PACKET_SIZE);
            
            byte[] buffer = new byte[PACKET_SIZE + EXTRA_BYTES];
            
            byte[] totalPacketsBytes = getBytesFromValue(totalPackets);
            
            buffer[0] = totalPacketsBytes[0];
            buffer[1] = totalPacketsBytes[1];
            
            DatagramPacket packet = new DatagramPacket(buffer, 2, InetAddress.getByName(hostName), port);
            socket.send(packet);
            
            int packetNumber = 0;
            int offset = 0;
            
            while (offset < messageBytes.length) {
                
                packetNumber++;
                int length = Math.min(PACKET_SIZE, messageBytes.length - offset);
                byte[] packetSize = getBytesFromValue(packetNumber);
                
                byte[] lengthBytes = getBytesFromValue(length);
                
                buffer[0] = packetSize[0];
                buffer[1] = packetSize[1];
                buffer[2] = lengthBytes[0];
                buffer[3] = lengthBytes[1];
                
                while (true) {
                    
                    //Enviar paquete
                    System.arraycopy(messageBytes, offset, buffer, 4, length);
                    packet = new DatagramPacket(buffer, (length + 4), InetAddress.getByName(hostName), port);
                    socket.send(packet);
                    
                    // Esperar la confirmación del receptor
                    byte[] bufferEsperado = new byte[PACKET_SIZE];
                    DatagramPacket confirmationPacket = new DatagramPacket(bufferEsperado, bufferEsperado.length);
                    socket.receive(confirmationPacket);
                    
                    // Verificar si se recibió la confirmación correcta
                    if (isConfirmationPacketCorrect(confirmationPacket.getData(), packetNumber)) {
                        System.out.println("Packet " + packetNumber + " sent successfully");
                        break;
                    } else {
                        System.out.println("Packet " + packetNumber + " not received correctly, retrying...");
                    }
                    
                    // Si se han intentado MAX_TRIES veces y no se ha recibido confirmación, asumir que se ha perdido el paquete
                    if (packetNumber % MAX_TRIES == 0) {
                        try {
                            socket.setSoTimeout(TIMEOUT_MS);
                            socket.receive(confirmationPacket);
                        } catch (SocketTimeoutException e) {
                            System.out.println("Packet " + packetNumber + " lost, retrying...");
                        }
                    }
                    
                }
                offset += length;
            }

            System.out.println("Se han enviado " + totalPackets + " paquetes.");
            
            // Esperar la respuesta del servidor
            socket.setSoTimeout(0);
            byte[] ackBuffer = new byte[PACKET_SIZE];
            DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
            try {
                socket.receive(ackPacket);
                String ackMessage = new String(ackPacket.getData(), 0, ackPacket.getLength());
                System.out.println("Confirmación recibida: " + ackMessage);
            } catch (SocketTimeoutException e) {
                System.out.println("No se recibió confirmación del servidor.");
            }
            
            socket.close();
        } catch (Exception ex) {
            Logger.getLogger(UDPSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static boolean isConfirmationPacketCorrect(byte[] data, int expectedPacketNumber) {
        int packetNumber = getValueFromBytes(data[0],data[1]);
        //System.out.printf("Numero: %d ---- esperado: %d%n",packetNumber,expectedPacketNumber);
        return packetNumber == expectedPacketNumber;
    }
    
    private static byte[] getBytesFromValue(int value){
        byte[] bytes = new byte[2]; // array de bytes para almacenar los resultados
            // dividir valor en dos bytes
            bytes[0] = (byte) ((value >> 8) & 0xFF); // byte más significativo
            bytes[1] = (byte) (value & 0xFF); // byte menos significativo
            return bytes;
    }

    private static int getValueFromBytes(byte byte1, byte byte2){
        return ((byte1 & 0xFF) << 8) | (byte2 & 0xFF);
    }

}
