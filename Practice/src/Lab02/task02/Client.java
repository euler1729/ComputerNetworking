import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    Socket socket = null;
    DataInputStream in = null;
    DataOutputStream out = null;
    Scanner scanner = null;

    public Client(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        System.out.println("Connected to "+ip+"::"+port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        scanner = new Scanner(System.in);
        String line = "";
        while(!line.equalsIgnoreCase("over")){
            try{
                line = scanner.nextLine();
                out.writeUTF(line);
                String response = in.readUTF();
                System.out.println("Server: "+response);
            }catch (Exception exp){
                exp.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost",3000);
        try {
            client.socket.close();
            client.in.close();
            client.out.close();
            client.scanner.close();
        }catch (Exception exp){
            exp.printStackTrace();
        }
    }
}
