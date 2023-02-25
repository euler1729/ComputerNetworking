import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class Server {
    private static final int MSS = 1024; // maximum segment size
    private static final int BUFFER_SIZE = 65536; // buffer size
    private static final int PORT = 5001; // port number
    private static final int TIMEOUT = 5000; // timeout value in milliseconds

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server listening on PORT: "+PORT);
        Socket socket = serverSocket.accept();

        byte[] fileData = new byte[BUFFER_SIZE];
        FileOutputStream fileOutputStream = new FileOutputStream("received_file.pdf");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

        int bytesRead;
        int expectedSeqNum = 1;
        boolean[] acksSent = new boolean[100000];
        while ((bytesRead = socket.getInputStream().read(fileData)) != -1) {
            int seqNum = extractSeqNum(fileData);
            if (seqNum == expectedSeqNum) {
                // Expected packet received, send acknowledgment
                byte[] ackBuffer = new byte[4];
                ByteBuffer.wrap(ackBuffer, 0, 4).putInt(expectedSeqNum);
                socket.getOutputStream().write(ackBuffer);

                // Write data to file
                bufferedOutputStream.write(fileData, 4, bytesRead - 4);

                // Update sequence number
                expectedSeqNum++;
                acksSent[seqNum] = true;
            } else {
                // Out-of-order packet received, ignore and send duplicate acknowledgment
                if (!acksSent[seqNum]) {
                    byte[] ackBuffer = new byte[4];
                    ByteBuffer.wrap(ackBuffer, 0, 4).putInt(seqNum);
                    socket.getOutputStream().write(ackBuffer);
                    acksSent[seqNum] = true;
                }
            }
        }

        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        fileOutputStream.close();

        socket.close();
        serverSocket.close();
    }

    private static int extractSeqNum(byte[] buffer) {
        return ByteBuffer.wrap(buffer, 0, 4).getInt();
    }
}
