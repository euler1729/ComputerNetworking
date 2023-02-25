import java.io.*;
import java.net.*;

public class Client {
    private static final int PORT = 8000;
    private static final int BUFFER_SIZE = 1024;
    private static final int WINDOW_SIZE = 10;

    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", PORT);
        System.out.println("Connected to server");

        DataInputStream inputStream = new DataInputStream(new FileInputStream("text.txt"));
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

        byte[] buffer = new byte[BUFFER_SIZE];
        int sequenceNumber = 0;
        int ackNumber = 0;
        int windowStart = 0;
        int windowEnd = windowStart + WINDOW_SIZE;

        while (true) {
            outputStream.writeInt(ackNumber);

            if (ackNumber >= sequenceNumber) {
                sequenceNumber = ackNumber;
            }

            if (sequenceNumber >= windowStart && sequenceNumber < windowEnd) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    outputStream.writeInt(-1);
                    break;
                }
                outputStream.write(buffer, 0, bytesRead);
                sequenceNumber += bytesRead;
            }

            if (sequenceNumber >= windowEnd) {
                System.out.println("Window full, waiting for ack...");
                while (ackNumber < sequenceNumber) {
                    int ack = socket.getInputStream().read();
                    if (ack >= ackNumber) {
                        ackNumber = ack;
                        windowStart = ackNumber;
                        windowEnd = windowStart + WINDOW_SIZE;
                        System.out.println(ack);
                    }
                }
                System.out.println("Received ack: " + ackNumber);
            }
        }

        inputStream.close();
        outputStream.close();
        socket.close();
        System.out.println("File sent successfully");
    }
}
