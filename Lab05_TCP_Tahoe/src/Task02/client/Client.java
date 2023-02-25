import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Client {
    private static final int MSS = 1024; // maximum segment size
    private static final int BUFFER_SIZE = 65536; // buffer size
    private static final int PORT = 5001; // port number
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
        Socket socket = new Socket(address, PORT);

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

        OutputStream outputStream = socket.getOutputStream();
        try {
            while (true) {
                // Send packets
                int packetCount = 0;
                while (packetCount < cwnd / MSS && lastSeqNum < numPackets) {
                    int dataSize = Math.min(MSS, fileData.length - lastSeqNum * MSS);
                    byte[] buffer = new byte[dataSize + 4];
                    ByteBuffer.wrap(buffer, 0, 4).putInt(lastSeqNum + 1);
                    System.arraycopy(fileData, lastSeqNum * MSS, buffer, 4, dataSize);
                    outputStream.write(buffer);
                    lastSeqNum++;
                    packetCount++;
                }

                // Receive acknowledgments
                byte[] ackBuffer = new byte[4];
                InputStream inputStream = socket.getInputStream();
                int ackIndex;
                while ((ackIndex = inputStream.read(ackBuffer, 0, 4)) != -1) {
                    int ackSeqNum = ByteBuffer.wrap(ackBuffer, 0, 4).getInt();
                    if (ackSeqNum >= expectedSeqNum && ackSeqNum <= lastSeqNum) {
                        ackIndex = ackSeqNum - 1;
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
                                    for (int i = ackSeqNum - 1; i < lastSeqNum; i++) {
                                        acksReceived[i] = false;
                                    }
                                    duplicateAcks = 0;
                                    break;
                                }
                            }
                        }
                        if (acksReceived[numPackets - 1]) {
                            // All packets have been acknowledged
                            endTime = System.nanoTime();
                            long duration = (endTime - startTime) / 1000000; // in milliseconds
                            System.out.println("File sent successfully in " + duration + "ms");
                        } else {
                            // Timeout occurred while waiting for acknowledgments
                            ssthresh = cwnd / 2;
                            cwnd = MSS;
                            expectedSeqNum = 1;
                            lastSeqNum = 0;
                            duplicateAcks = 0;
                            System.out.println("Timeout occurred. Resending packets...");
                        }
                    }
                }
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }
        socket.close();
        outputStream.close();
        fileInputStream.close();
        bufferedInputStream.close();
    }
}
