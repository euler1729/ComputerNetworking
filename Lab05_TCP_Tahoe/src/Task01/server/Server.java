import java.io.*;
import java.net.*;

public class Server {
    private static final int PORT = 8000;
    private static final int BUFFER_SIZE = 1024;
    private static final int WINDOW_SIZE = 10;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server listening on port " + PORT);

        Socket socket = serverSocket.accept();
        System.out.println("Client connected: " + socket.getRemoteSocketAddress());

        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(new FileOutputStream("file.txt"));

        byte[] buffer = new byte[BUFFER_SIZE];
        int sequenceNumber = 0;
        int ackNumber = 0;
        int windowStart = 0;
        int windowEnd = windowStart + WINDOW_SIZE;

        while (true) {
            // Receive acknowledgment from the client
            try{
                ackNumber = inputStream.readInt();
            }catch (EOFException e){
                System.out.println(e.getMessage());
                break;
            }
            if (ackNumber == -1) {
                break;
            }
            // Check if the acknowledgment falls within the sliding window
            if (ackNumber >= windowStart && ackNumber < windowEnd) {
                // Read data from the input stream
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                // Write data to the output stream
                outputStream.write(buffer, 0, bytesRead);
                // Update sequence number
                sequenceNumber += bytesRead;
            }
            // Check if the sliding window has moved
            if (sequenceNumber >= windowEnd || ackNumber == sequenceNumber - 1) {
                // Flush the output stream and update the sliding window
                outputStream.flush();
                windowStart = sequenceNumber;
                windowEnd = windowStart + WINDOW_SIZE;
                System.out.println("Received data up to sequence number " + sequenceNumber);
            }
            // Send acknowledgment back to the client
            outputStream.writeInt(windowStart);
        }

        // Close streams and sockets
        inputStream.close();
        outputStream.close();
        socket.close();
        serverSocket.close();

        System.out.println("File received successfully");
    }
}
