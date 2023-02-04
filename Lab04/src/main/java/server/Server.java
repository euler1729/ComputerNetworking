import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Objects;

public class Server {
    private static final HashMap<String,String> ip_hash = new HashMap<String, String>();
    private static final int port = 1234;
    private static final String[] hosts = {"zoho.com", "gmail.com","google.com", "facebook.com"};
    private static final String[] ip = {"172.28.251.59", "172.217.11.5","172.217.11.14","31.13.71.36"};
    static {
        for(int i=0; i<hosts.length; ++i){
            ip_hash.put(hosts[i],ip[i]);
        }
    }
    public static void main(String[] args) throws IOException {
        //1. Socket to listen on port - 1234
        DatagramSocket serverSocket = new DatagramSocket(port);
        while(true){
            byte[] receive = new byte[65535];
            //2. DatagramPacket to receive data
            DatagramPacket received_packet = new DatagramPacket(receive, receive.length);
            //3. Receiving data in byte buffer
            serverSocket.receive(received_packet);
            //Data sent by Client
            String received_data = new String(received_packet.getData(),0, received_packet.getLength());
            //Client Ip address
            InetAddress ip_client = received_packet.getAddress();
            //Clients' Port
            int port_client = received_packet.getPort();

            if(received_data.equals("quit")){
                System.out.println("Client leaving...");
                serverSocket.close();
                break;
            }
            String ip = null;
            try{
                InetAddress inetAddress = java.net.InetAddress.getByName(received_data);
                ip = inetAddress.toString();
            }catch (Exception exception){
               ip = "Host Not Found";
            }
            //String ip = ip_hash.get(received_data);
            String send_msg = Objects.requireNonNullElse(ip, "Host not found");
            DatagramPacket send_pack = new DatagramPacket(send_msg.getBytes(),
                                        send_msg.getBytes().length,ip_client,port_client);
            serverSocket.send(send_pack);
        }
    }
}
