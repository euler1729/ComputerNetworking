import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final int port = 1234;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        //1. Socket to carry data
        DatagramSocket serverSocket = new DatagramSocket();
        InetAddress server_ip = InetAddress.getLocalHost();
        if (args.length > 0) {
            server_ip = InetAddress.getByName(args[0]);
        }
        while (true) {
            try {
                System.out.print("Enter the Hostname: ");
                String in = scanner.nextLine();
                byte[] buffer = in.getBytes();
                //2. Create DatagramPacket to send data
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, server_ip, port);
                //3. calling send method of DatagramSocket to send data
                serverSocket.send(packet);
                serverSocket.setSoTimeout(5000);
                if (in.equals("quit")) {
                    serverSocket.close();
                    break;
                }

                byte[] receivedByte = new byte[65535];
                DatagramPacket receivedPacket = new DatagramPacket(receivedByte, receivedByte.length);
                serverSocket.receive(receivedPacket);
                String receivedString = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                System.out.println("IP Address: " + receivedString);
            } catch (Exception e) {
                System.out.println("Server request time out. Please send request once again...");
            }

        }
    }
}
