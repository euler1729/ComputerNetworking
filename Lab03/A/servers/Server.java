import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;
import java.net.Socket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.net.NetworkInterface;
import java.net.Inet6Address;
import java.util.Enumeration;
import java.net.SocketException;


public class Server{
    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Server is running on "+getIP()+":1234");
        while (true){
            try{
                Socket socket = serverSocket.accept();
                System.out.println("A Client just joined...");
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                Thread thread = new ClientHandler(socket, in, out);
                thread.start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static String getIP(){
        String ip=null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // *EDIT*
                    if (addr instanceof Inet6Address) continue;

                    ip = addr.getHostAddress();
                    System.out.println(iface.getDisplayName() + " " + ip);
                }
            }
        } catch (SocketException e) {
            System.out.println(e);
        }
        return ip;
    }
}
class ClientHandler extends Thread {
    static DataInputStream in;
    static DataOutputStream out;
    static Socket socket;
    public ClientHandler (Socket socket, DataInputStream in, DataOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String received = in.readUTF();
                System.out.println("Client: "+received);
                switch (received) {
                    case "quit":
                        closeConnection();
                        break;
                    default:
                        sendFile(received);
                        break;
                }
            } catch (Exception e) {
                closeConnection();
//                e.printStackTrace();
            }
        }
    }
    private static void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }
    private static void sendFile(String fileName) throws Exception{
        out.flush();
        int bytes = 0;
        // Open the File where he located in your pc
        File file = new File(fileName);
        FileInputStream fileInputStream
                = new FileInputStream(file);

        // Here we send the File to Server
        out.writeLong(file.length());
        // Here we  break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer))
                != -1) {
            // Send the file to Server Socket
            out.write(buffer, 0, bytes);
            out.flush();
        }
        // close the file here
        fileInputStream.close();
    }
}