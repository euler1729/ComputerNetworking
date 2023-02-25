import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Client_UDP {
    private static final int MSS = 1024; // maximum segment size
    private static final int BUFFER_SIZE = 65536; // buffer size
    private static final int PORT = 5000; // port number
    private static final int TIMEOUT = 5000; // timeout value in milliseconds
    private static final double ALPHA = 0.125; // EWMA alpha value
    private static final double BETA = 0.25; // EWMA beta value

    private static int ssthresh = BUFFER_SIZE; // slow start threshold
    private static int cwnd = MSS; // congestion window size
    private static int lastSeqNum = 0;
    private static int duplicateAcks = 0;
    private static int expectedSeqNum = 1;

    public static void main(String[] args) throws IOException, InterruptedException {
        InetAddress address = InetAddress.getByName("localhost");
        DatagramSocket socket = new DatagramSocket();

        File file = new File("book.pdf");
        byte[] fileData = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        bufferedInputStream.read(fileData, 0, fileData.length);

        int numPackets = (int) Math.ceil((double) fileData.length / MSS);
        boolean[] acksReceived = new boolean[numPackets];
        Arrays.fill(acksReceived, false);

        long startTime = System.nanoTime();
        long endTime;

        while (true) {
            // Send packets
            int packetCount = 0;
            while (packetCount < cwnd / MSS && lastSeqNum < numPackets) {
                int dataSize = Math.min(MSS, fileData.length - lastSeqNum * MSS);
                byte[] buffer = new byte[dataSize + 4];
                ByteBuffer.wrap(buffer, 0, 4).putInt(lastSeqNum + 1);
                System.arraycopy(fileData, lastSeqNum * MSS, buffer, 4, dataSize);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PORT);
                socket.send(packet);
                lastSeqNum++;
                packetCount++;
            }

            // Receive acknowledgments
            byte[] ackBuffer = new byte[4];
            DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
            socket.setSoTimeout(TIMEOUT);
            boolean timeoutOccurred = false;
            while (packetCount > 0 && !timeoutOccurred) {
                try {
                    socket.receive(ackPacket);
                    int ackSeqNum = ByteBuffer.wrap(ackPacket.getData(), 0, 4).getInt();
                    if (ackSeqNum >= expectedSeqNum && ackSeqNum <= lastSeqNum) {
                        int ackIndex = ackSeqNum - 1;
                        if (!acksReceived[ackIndex]) {
                            acksReceived[ackIndex] = true;
                            packetCount--;
                            if (ackSeqNum == expectedSeqNum) {
                                // Update congestion window
                                expectedSeqNum++;
                                duplicateAcks = 0;
                                if (cwnd < ssthresh) {
                                    // Slow start
                                    cwnd += MSS;
                                } else {
                                    // Congestion avoidance
                                    cwnd += (MSS * MSS / cwnd);
                                }
                            } else {
                                // Duplicate acknowledgment
                                duplicateAcks++;
                                if (duplicateAcks == 3) {
                                    // Fast retransmit
                                    ssthresh = cwnd / 2;
                                    cwnd = ssthresh + 3 * MSS;
                                    lastSeqNum = ackSeqNum - 1;
                                    packetCount = 0;
                                }
                            }
                        }
                    }

                } catch (SocketTimeoutException e) {
                    timeoutOccurred = true;
                }
                // Check if all packets have been acknowledged
                boolean allRecvd = true;
                for(boolean val:acksReceived){
                    if(!val){
                        allRecvd = false;
                        break;
                    }
                }
                if (allRecvd) {
                    endTime = System.nanoTime();
                    double totalTime = (endTime - startTime) / 1_000_000.0;
                    double fileSize = file.length() / (1024.0 * 1024.0);
                    double throughput = fileSize / (totalTime / 1000.0);
                    System.out.println("File sent successfully!");
                    System.out.printf("File size: %.2f MB\n", fileSize);
                    System.out.printf("Time taken: %.2f ms\n", totalTime);
                    System.out.printf("Throughput: %.2f MB/s\n", throughput);
                }
                // Update congestion window after timeout
                if (timeoutOccurred) {
                    ssthresh = cwnd / 2;
                    cwnd = MSS;
                    lastSeqNum = expectedSeqNum - 1;
                    Arrays.fill(acksReceived, false);
                    System.out.println("Timeout occurred, resending packets...");
                }
            }
        }
    }
}