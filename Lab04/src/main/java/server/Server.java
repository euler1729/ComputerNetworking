import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {

    private static final int port = 1234;
    public static void main(String[] args) throws IOException {
        //1. Socket to listen on port - 1234
        DatagramSocket datagramSocket = new DatagramSocket(port);
        while(true){
            byte[] receive = new byte[65535];
            //2. DatagramPacket to receive data
            DatagramPacket received_packet = new DatagramPacket(receive, receive.length);
            //3. Receiving data in byte buffer
            datagramSocket.receive(received_packet);
            System.out.println("Client: "+
                    new String(received_packet.getData(),0, received_packet.getLength()));
            if(ToString(receive).toString().equals("quit")){
                System.out.println("Client leaving...");
                datagramSocket.close();
                break;
            }
            datagramSocket.send(received_packet);
        }
    }
    //Converts byte array to string
    public static StringBuilder ToString(byte[] b){
        if(b==null) return null;
        StringBuilder str = new StringBuilder();
        int i = 0;
        while(b[i]!=0){
            str.append((char)b[i++]);
        }
        return str;
    }
}
