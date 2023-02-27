package client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPReceiver implements Runnable{

    @Override
    public void run(){
        try {
            int port = 12345;
            int PACKET_SIZE = 60000;
            int EXTRA_BYTES = 4;
            
            DatagramSocket socket = new DatagramSocket(port);
            
            byte[] temporalBuffer = new byte[PACKET_SIZE + EXTRA_BYTES];
            byte[] buffer = new byte[0];
            
            DatagramPacket packet = null;//new DatagramPacket(buffer, buffer.length);
            
            int length = 0;
            int receivedPackets = 0;
            int expectedPackets = -1;
            boolean[] receivedPacketNumbers = null;
            
            while (true) {
                temporalBuffer = new byte[PACKET_SIZE + EXTRA_BYTES];
                packet = new DatagramPacket(temporalBuffer, temporalBuffer.length);
                socket.receive(packet);
                
                if (expectedPackets == -1) {
                    expectedPackets = getValueFromBytes(temporalBuffer[0], temporalBuffer[1]);
                    receivedPacketNumbers = new boolean[expectedPackets];
                    System.out.println("Esperando recibir " + expectedPackets + " paquetes.");
                    continue;
                }
                length = getValueFromBytes(temporalBuffer[2], temporalBuffer[3]);
                //Remover byte para numero de paquete
                //Remover byte para tamaño de paquete y ajustar al tamaño
                byte[] data = Arrays.copyOfRange(temporalBuffer, EXTRA_BYTES, length+EXTRA_BYTES);
                
                buffer = joinByteArray(buffer, data);
                
                //System.out.println("El texto es: " + new String(buffer));
                
                int packetNumber = getValueFromBytes(temporalBuffer[0], temporalBuffer[1]);
                //System.out.println(expectedPackets);
                
                System.out.println(packetNumber);
                
                if (packetNumber >= 1 && packetNumber <= expectedPackets && !receivedPacketNumbers[packetNumber - 1]) {
                    
                    receivedPackets++;
                    receivedPacketNumbers[packetNumber - 1] = true;
                    //System.out.println("Paquete " + packetNumber + " recibido.");
                    
                    // Enviar la confirmación al remitente
                    byte[] confirmationData = getBytesFromValue(packetNumber);
                    DatagramPacket confirmationPacket = new DatagramPacket(confirmationData, confirmationData.length, packet.getAddress(), packet.getPort());
                    socket.send(confirmationPacket);
                    System.out.println("Packet " + getPacketNumber(packet.getData()) + " received");
                }
                
                if (receivedPackets == expectedPackets) {
                    System.out.println("Se han recibido todos los paquetes.");
                    System.out.println(new String(buffer));
                    String confirmation = "OK";
                    byte[] confirmationBytes = confirmation.getBytes();
                    DatagramPacket ackPacket = new DatagramPacket(confirmationBytes, confirmationBytes.length, packet.getAddress(), packet.getPort());
                    socket.send(ackPacket);
                    Path path = Paths.get("C:\\Users\\erick\\Documents\\prueba\\transferidos\\"+ ClientServer.fileName);
                    Files.write(path, buffer);
                    
                    break;
                }
            }
            
            socket.close();
        } catch (Exception ex) {
            Logger.getLogger(UDPReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static byte[] joinByteArray(byte[] byte1, byte[] byte2) {

        return ByteBuffer.allocate(byte1.length + byte2.length)
                .put(byte1)
                .put(byte2)
                .array();

    }
    
    private static int getPacketNumber(byte[] data) {
        return getValueFromBytes(data[0],data[1]);
    }
    
    private static int getValueFromBytes(byte byte1, byte byte2){
        return ((byte1 & 0xFF) << 8) | (byte2 & 0xFF);
    }

    private static byte[] getBytesFromValue(int value){
        byte[] bytes = new byte[2]; // array de bytes para almacenar los resultados
        // dividir valor en dos bytes
        bytes[0] = (byte) ((value >> 8) & 0xFF); // byte más significativo
        bytes[1] = (byte) (value & 0xFF); // byte menos significativo
        return bytes;
    }
}
