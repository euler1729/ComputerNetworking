import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Socket socket = new Socket("localhost", 1111);
        System.out.println("Connected to " + socket.getInetAddress() + ":" + socket.getPort());
        while (true) {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            String message = scanner.nextLine();
            dataOutputStream.writeUTF(message);
            try {
                String response = dataInputStream.readUTF();
                System.out.println("Server: " + response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
