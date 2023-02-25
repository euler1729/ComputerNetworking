import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

public class Server {
    private static final int MSS = 1024; // maximum segment size
    private static final int BUFFER_SIZE = 65536; // buffer size
    private static final int PORT = 5000; // port number
    private static final int TIMEOUT = 5000; // timeout value in milliseconds

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Socket clientSocket = serverSocket.accept();

        InputStream in = clientSocket.getInputStream();
        OutputStream out = clientSocket.getOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int rwnd = BUFFER_SIZE;
        int lastSeqNum = 0;

        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            in.read(buffer, 0, buffer.length);
            int seqNum = ByteBuffer.wrap(buffer, 0, 4).getInt();

            if (seqNum == lastSeqNum + 1) {
                lastSeqNum = seqNum;
                rwnd -= MSS;
                if (rwnd < MSS) {
                    rwnd = 0;
                }
                out.write(ByteBuffer.allocate(4).putInt(seqNum).array());
                out.flush();
            } else {
                out.write(ByteBuffer.allocate(4).putInt(lastSeqNum).array());
                out.flush();
            }
        }
    }
}
