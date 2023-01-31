package servers;
import servers.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

public class ClientHandler extends Thread {
    static DataInputStream in;
    static DataOutputStream out;
    static Socket socket;
    public ClientHandler (Socket socket, DataInputStream in, DataOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        try {
            out.writeUTF("Server connected!!!");
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String received = in.readUTF();

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
                e.printStackTrace();
            }
        }
    }
    private static void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void sendFile(String fileName) throws Exception{
        out.writeUTF("Message from sendFile method.");
        out.flush();
    }
}