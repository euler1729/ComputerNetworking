import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Server started and listening on port 1234...");

            // Accept client connection
            Socket socket = serverSocket.accept();
            System.out.println("Client connected: " + socket.getInetAddress().getHostName());

            // Receive the MSS from client
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            int MSS = 1024;
            // Initialize congestion control variables
            int cwnd = MSS;
            int ssthresh = 100000;
            int dupACKcount = 0;

            // Receive file data from client
            InputStream is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream("received_file.pdf");
            byte[] buffer = new byte[MSS];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                // Simulate packet loss by dropping the 3rd packet
                if (dupACKcount == 2) {
                    System.out.println("Simulating packet loss...");
                    dupACKcount = 0;
                    continue;
                }

                // Write data to file and send ACK
                fos.write(buffer, 0, buffer.length);
                dupACKcount++;
                OutputStream os = socket.getOutputStream();
                os.write("ACK\n".getBytes());
                os.flush();

                // Update congestion control variables
                if (cwnd < ssthresh) {
                    cwnd += MSS;
                } else {
                    cwnd += (MSS * MSS) / cwnd;
                }

                // Switch to fast retransmit if packet loss is detected
                if (dupACKcount == 3) {
                    System.out.println("Packet loss detected, switching to fast retransmit...");
                    ssthresh = cwnd / 2;
                    cwnd = MSS;
                    dupACKcount = 0;
                }
            }

            System.out.println("File received successfully.");

            // Close streams and socket
            fos.close();
            is.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
