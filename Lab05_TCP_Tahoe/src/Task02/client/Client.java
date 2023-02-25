import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        String serverName = "localhost";
        int port = 1234;

        // connect to the server and receive the MSS
        Socket clientSocket = new Socket(serverName, port);
        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        int mss = 1024;
//        System.out.println("Received MSS: " + mss);

        // initialize variables
        int cwnd = mss;
        int ssthresh = Integer.MAX_VALUE;
        int dupAckCount = 0;

        // divide the file to be transferred into segments of MSS size or smaller
        File file = new File("book.pdf");
        FileInputStream fileIn = new FileInputStream(file);
        byte[] fileData = new byte[(int) file.length()];
        fileIn.read(fileData);
        int numSegments = (int) Math.ceil((double) fileData.length / mss);
        System.out.println("Number of segments: " + numSegments);

        // send each segment to the server and wait for an ACK
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        System.out.println("Total file size: "+numSegments*mss+"byte s");
        out.write(numSegments*mss);
        out.flush();
        int ackCount = 0;
        int currentSegment = 0;
        long startTime = System.currentTimeMillis();
        while (ackCount < numSegments) {
            // send segment
            int segmentSize = Math.min(mss, fileData.length - currentSegment * mss);
            out.write(fileData, currentSegment * mss, cwnd);
            out.flush();
            System.out.println("Sent segment " + (currentSegment + 1) + " of " + numSegments);

            // wait for ACK
            int timeout = 5000; // 5 seconds
            clientSocket.setSoTimeout(timeout);
            try {
                int ack = in.readInt();
                System.out.println("Received ACK: " + ack);
                ackCount++;
                dupAckCount = 0;
                if (cwnd < ssthresh) {
                    cwnd += mss; // increase cwnd in slow start
                } else {
                    cwnd += (mss * mss) / cwnd; // increase cwnd in congestion avoidance
                }
            } catch (SocketTimeoutException e) {
                // timeout, packet loss detected
                System.out.println("Packet loss detected");
                ssthresh = cwnd / 2;
                cwnd = mss;
                dupAckCount = 0;
                currentSegment -= ackCount;
                ackCount = 0;
            }
            currentSegment++;
        }

        // close connection
        in.close();
        out.close();
        clientSocket.close();

        // calculate throughput and transmission time
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double throughput = (double) fileData.length / totalTime * 1000 / 1024; // Mbps
        System.out.println("Transmission time: " + totalTime + " ms");
        System.out.println("Throughput: " + throughput + " Mbps");
    }
}
