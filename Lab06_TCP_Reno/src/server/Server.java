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
            FileOutputStream fos = new FileOutputStream("a.txt");
            byte[] buffer = new byte[MSS];
            int bytesRead;
            int ack = 0;
            while ((bytesRead = is.read(buffer = new byte[cwnd])) != -1) {
                // Simulate packet loss by dropping the 3rd packet
                if (dupACKcount == 2) {
                    System.out.println("Simulating packet loss...");
                    dupACKcount = 0;
                    continue;
                }
                // Write data to file and send ACK
                fos.write(buffer, 0, buffer.length);
//                dupACKcount++;
                OutputStream os = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);
                ack += buffer.length;
                System.out.println("Next ack: "+ack);
                dos.writeInt(ack);
                dos.flush();
                os.flush();
                // Update congestion control variables
                if (cwnd < ssthresh) {
                    cwnd += MSS;
                } else {
                    cwnd += (MSS * MSS) / cwnd;
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
