import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        String hostName = "localhost";
        int portNumber = 1234;
        int windowSize = 1; // initial window size
        int congestionThreshold = 16; // initial congestion threshold
        int duplicateAcks = 0; // number of duplicate acknowledgements received
        boolean slowStart = true; // flag to indicate if TCP is in slow start phase
        boolean congestionAvoidance = false; // flag to indicate if TCP is in congestion avoidance phase

        try (
                Socket socket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            String inputLine;
            int ackNumber = 0;

            while (ackNumber >= 0) {
                out.println(windowSize);
                System.out.println("Sending data with window size: " + windowSize);

                inputLine = in.readLine();
                ackNumber = Integer.parseInt(inputLine.trim());
                System.out.println("Received SEQ: " + ackNumber);

                if (slowStart) {
                    windowSize++;
                    if (windowSize >= congestionThreshold) {
                        slowStart = false;
                        congestionAvoidance = true;
                    }
                } else if (congestionAvoidance) {
                    windowSize += 1 / windowSize;
                }

                if (ackNumber > duplicateAcks) {
                    duplicateAcks = 0;
                } else if (++duplicateAcks == 3) {
                    congestionThreshold = windowSize / 2;
                    windowSize = 1;
                    slowStart = true;
                    congestionAvoidance = false;
                    System.out.println("Fast retransmit detected, reducing congestion window to: " + congestionThreshold);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }
}
