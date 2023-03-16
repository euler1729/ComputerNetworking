
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server{
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(3000);
        System.out.println("Server running on "+serverSocket.getInetAddress()+":"+serverSocket.getLocalPort());
        while(true){
            try{
                Socket socket = serverSocket.accept();
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());
                Thread thread = new ClientThread(socket, in, out);
                thread.start();
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }
}

class ClientThread extends Thread{
    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    public ClientThread(Socket socket, DataInputStream in, DataOutputStream out) throws IOException {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        String line = "";
        while(!line.equalsIgnoreCase("Over")){
            try{
                line = in.readUTF();
                System.out.println("Client-"+socket.getInetAddress()+"::"+socket.getPort()+": "+line);
                out.writeUTF(line.toUpperCase());
                out.flush();
            }catch (Exception exp){
                exp.printStackTrace();
            }
        }
        try {
            closeConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void closeConnection() throws IOException {
        this.socket.close();
        this.in.close();
        this.out.close();
    }
}
