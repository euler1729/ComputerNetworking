import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket = null;
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;
    private Scanner scanner = null;

    public Client(String ip, int port) throws IOException {
        try{
            socket = new Socket(ip, port);
            System.out.println("Connected to "+ip+":"+port);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            scanner = new Scanner(System.in);
        }catch (Exception exp){
            exp.printStackTrace();
            return;
        }
        String line = "";
        while(!line.equals("Over")){
            try{
                line = scanner.nextLine();
                dataOutputStream.writeUTF(line);
                String response = dataInputStream.readUTF();
                System.out.println("Server: "+response);
            }catch (Exception exp){
                exp.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost",3000);
        try{
            client.socket.close();
            client.dataInputStream.close();
            client.dataOutputStream.close();
            client.scanner.close();
            client.scanner.close();
        }catch (Exception exp){
            exp.printStackTrace();
        }
    }
}
