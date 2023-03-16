import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;

    public Server(int port) throws IOException {
        try{
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on "+serverSocket.getInetAddress()+":"+serverSocket.getLocalPort());
            System.out.println("Waiting for client...");
        }catch (Exception exp){
            exp.printStackTrace();
            return;
        }
        while(true){
            socket = serverSocket.accept();
            System.out.println("Device "+socket.getInetAddress()+":"+socket.getPort()+" connected!");
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            String inputLine = "";
            while(!inputLine.equalsIgnoreCase("OVER")){
                try{
                    inputLine = dataInputStream.readUTF();
                    System.out.println("Client: "+inputLine);
                    dataOutputStream.writeUTF(inputLine.toUpperCase());
                    dataOutputStream.flush();
                }catch (Exception exp){
                    exp.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(3000);
        try{
            server.serverSocket.close();
            server.socket.close();
            server.dataOutputStream.close();
            server.dataInputStream.close();
        }catch (Exception exp){
            exp.printStackTrace();
        }
    }
}
