import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static int i = 1;
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1111);
        System.out.println("Server running on "+
                serverSocket.getInetAddress()+":"+serverSocket.getLocalPort());
        System.out.println();
        while (true) {
            Socket socket = serverSocket.accept();
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("Client "+i+" connected!");
            try {
                String message = dataInputStream.readUTF();
                System.out.println(message);
                dataOutputStream.writeUTF(message.toUpperCase());
                ++i;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}