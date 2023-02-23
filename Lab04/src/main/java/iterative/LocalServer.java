
import java.net.*;
import java.util.Arrays;
import java.util.Enumeration;

public class LocalServer {
    static String localIP = getLocalIP();
    static int myPort = 1231;
    static int rootPort = 1233;
    static int tldPort = 1236;
    static int authPort = 1237;
    public static void main(String[] args) {
        DatagramSocket socket = null;
        try {
            //server running on port 1233
            socket = new DatagramSocket(myPort);
            System.out.println("Local server running on " + localIP + ":"+myPort);
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
                DatagramPacket rootPack  = new DatagramPacket(
                        reqBuf,
                        reqBuf.length,
                        InetAddress.getByName(localIP),
                        rootPort
                );
                socket.send(rootPack);
                byte[] responseRoot = new byte[512];
                DatagramPacket resRootPack = new DatagramPacket(responseRoot, responseRoot.length);
                socket.receive(resRootPack);

                DatagramPacket tldPack = new DatagramPacket(
                        reqBuf,
                        reqBuf.length,
                        InetAddress.getByName(localIP),
                        tldPort
                );
                socket.send(tldPack);

                byte[] tldRes = new byte[512];
                DatagramPacket tldResPack = new DatagramPacket(tldRes, tldRes.length);
                socket.receive(tldResPack);

                DatagramPacket authPack = new DatagramPacket(
                        reqBuf,
                        reqBuf.length,
                        InetAddress.getByName(localIP),
                        authPort
                );
                socket.send(authPack);

                byte[] authRes = new byte[512];
                DatagramPacket authResPack = new DatagramPacket(tldRes, tldRes.length);
                socket.receive(authResPack);

                DatagramPacket clientPack = new DatagramPacket(
                        authRes,
                        authRes.length,
                        requestPacket.getAddress(),
                        requestPacket.getPort()
                );
                socket.send(clientPack);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }

}
