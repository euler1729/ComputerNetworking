import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Scanner;

public class Client {
    // Socket for the client
    private Socket socket;
    // BufferedReader and BufferedWriter for reading and writing data
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    // String to store the client's username
    private String username;

    // Constructor to initialize the client
    public Client(Socket socket, String username){
        try{
            // Initialize the socket, BufferedReader, BufferedWriter, and username
            this.socket = socket;
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader((new InputStreamReader(socket.getInputStream())));
            this.username = username;
        } catch (IOException e){
            e.printStackTrace();
            // Close all resources if an exception is thrown
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // Method to send messages
    public void sendMessage(){
        try{
            // Send the client's username to the server
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner((System.in));
            // Loop until the socket is connected
            while(socket.isConnected()){
                // Read a message from the user
                String messageToSend = scanner.nextLine();
                // Send the message to the server
                bufferedWriter.write(username +": "+ messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            // Close all resources if an exception is thrown
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // Method to listen for messages
    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromGroupChat;
                // Loop until the socket is connected
                while(socket.isConnected()){
                    try {
                        // Read a message from the server
                        messageFromGroupChat = bufferedReader.readLine();
                        // Print the message to the console
                        System.out.println(messageFromGroupChat);
                    } catch (IOException e) {
                        // Close all resources if an exception is thrown
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    // Method to close all resources
    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Main method
    public static void main (String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        String serverip = "10.33.3.23";
        int  serverport = 1234;
        Socket socket = new Socket(serverip, serverport);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
    }
//    private String getIP(){
//        String ip=null;
//        try {
//            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//            while (interfaces.hasMoreElements()) {
//                NetworkInterface networkInterface = interfaces.nextElement();
//                // filters out 127.0.0.1 and inactive interfaces
//                if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;
//                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
//                while(addresses.hasMoreElements()) {
//                    InetAddress addr = addresses.nextElement();
//                    if (addr instanceof Inet6Address) continue;
//                    ip = addr.getHostAddress();
//                }
//            }
//        } catch (SocketException e) {
//            System.out.println(e);
//        }
//        return ip;
//    }
}
