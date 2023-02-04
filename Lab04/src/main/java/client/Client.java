import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final int port = 1234;
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        //1. Socket to carry data
        DatagramSocket datagramSocket = new DatagramSocket();
        InetAddress ip = InetAddress.getLocalHost();
        while(true){
            String in = scanner.nextLine();
            byte [] buffer = in.getBytes();//Converts String to byte array
            //2. Create DatagramPacket to send data
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
            //3. calling send method of DatagramSocket to send data
            datagramSocket.send(packet);

            byte[]receivedByte = new byte[65535];
            DatagramPacket receivedPacket = new DatagramPacket(receivedByte, receivedByte.length);
            datagramSocket.receive(receivedPacket);
            String receivedString = new String(receivedPacket.getData(),0,receivedPacket.getLength());
            System.out.println("Server: "+receivedString);

            if(in.equals("quit")){
                datagramSocket.close();
                break;
            }
        }
    }
}
