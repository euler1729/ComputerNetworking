//package server;
import java.net.*;
import java.util.Arrays;
import java.util.Enumeration;

public class RootServer {
    static String localIP = getLocalIP();
    static int myPort = 1202;
    static int tldPort = 1201;
    public static void main(String[] args) {
        DatagramSocket socket = null;
        try {
            //server running on port 1233
            socket = new DatagramSocket(myPort);
            System.out.println("ROOT server running on " + localIP + ":"+myPort);
            while (true) {
                byte[] reqBuf = new byte[4096];
                DatagramPacket reqPack = new DatagramPacket(reqBuf, reqBuf.length);
                socket.receive(reqPack);
                Thread thread = new Thread(new ClientHandler(socket, reqPack));
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        assert socket != null;
        socket.close();
    }

    /**
     *
     * @return local ip
     */
    private static String getLocalIP() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> intface = NetworkInterface.getNetworkInterfaces();
            while (intface.hasMoreElements()) {
                NetworkInterface face = intface.nextElement();
                if (face.isLoopback() || !face.isUp()) continue;
                Enumeration<InetAddress> adr = face.getInetAddresses();
                while (adr.hasMoreElements()) {
                    InetAddress inetAddress = adr.nextElement();
                    if (inetAddress instanceof Inet6Address) continue;
                    ip = inetAddress.getHostAddress();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return ip;
    }

    public static class ClientHandler implements Runnable {
        private final DatagramSocket socket;
        private final DatagramPacket requestPacket;
        public ClientHandler(DatagramSocket socket, DatagramPacket reqPack) {
            this.socket = socket;
            this.requestPacket = reqPack;
        }
        @Override
        public void run() {
            try {
                byte[] reqBuf = requestPacket.getData();
//                System.out.println(Arrays.toString(reqBuf));
                DatagramPacket tldPack  = new DatagramPacket(
                        reqBuf,
                        reqBuf.length,
                        InetAddress.getByName(localIP),
                        tldPort
                );
                socket.send(tldPack);
                byte[] responseTLD = new byte[512];
                DatagramPacket resTldPack = new DatagramPacket(responseTLD, responseTLD.length);
                socket.receive(resTldPack);
//                System.out.println("tld: "+Arrays.toString(responseTLD));
                DatagramPacket resPack = new DatagramPacket(
                        resTldPack.getData(),
                        resTldPack.getData().length,
                        requestPacket.getAddress(),
                        requestPacket.getPort());
                socket.send(resPack);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }

}
