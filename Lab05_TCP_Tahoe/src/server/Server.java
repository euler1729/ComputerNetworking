//package server;

import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException {
        int portNumber = 1234;

        try (
                ServerSocket serverSocket = new ServerSocket(portNumber);
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String inputLine;
            int seqNumber = 0;

            while ((inputLine = in.readLine()) != null) {
                int ackNumber = Integer.parseInt(inputLine.trim());
                System.out.println("Received ACK: " + ackNumber);

                seqNumber = ackNumber;
                Thread.sleep(500);
                out.println(seqNumber);
                System.out.println("Sending SEQ: " + seqNumber);
            }
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
