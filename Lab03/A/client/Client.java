import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client{
    private static DataOutputStream out = null;
    private static DataInputStream in = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try(Socket socket = new Socket("192.168.1.9",1234)){
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            while (socket.isConnected()){
                System.out.print("Enter filename to Download: ");
                String send = scanner.nextLine();
                out.writeUTF(send);
                out.flush();
                receiveFile(send);
            }
            in.close();
            out.close();
            socket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void receiveFile(String fileName) throws Exception {
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        long size = in.readLong();
        byte[] buffer = new byte[4 * 1024];
        while (size > 0
                && (bytes = in.read(
                buffer, 0,
                (int)Math.min(buffer.length, size)))
                != -1) {
            // Here we write the file using write method
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }
        // Here we received file
        System.out.println("File is Received");
        fileOutputStream.close();
    }
}
