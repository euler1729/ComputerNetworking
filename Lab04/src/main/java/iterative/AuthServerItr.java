
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;

public class AuthServerItr {
    static int myport = 1237;
    public static void main(String[] args) {
        DatagramSocket socket = null;
        try {
            //server running on port 1237
            socket = new DatagramSocket(myport);
            System.out.println("server running on " + getLocalIP() + ":"+myport);
            while (true) {
                byte[] reqBuf = new byte[512];
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
        public static ArrayList<Record> store;
        private final DatagramSocket socket;
        private final DatagramPacket requestPacket;
        public ClientHandler(DatagramSocket socket, DatagramPacket reqPack) {
            this.socket = socket;
            this.requestPacket = reqPack;
        }
        @Override
        public void run() {
            try {
//                System.out.println("req: "+Arrays.toString(requestPacket.getData()));
                byte[] resBuf = handleReq(requestPacket.getData());
                DatagramPacket resPack = new DatagramPacket(
                        resBuf,
                        resBuf.length,
                        requestPacket.getAddress(),
                        requestPacket.getPort());
                socket.send(resPack);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }

        public static byte[] handleReq(byte[] request) throws Exception {
            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(request));
            short rid = inputStream.readShort();
            inputStream.readByte();
            inputStream.readByte();
            inputStream.readShort();
            inputStream.readShort();
            inputStream.readShort();
            inputStream.readShort();

            System.out.println("req id: " + rid);
            String qName = "";
            int len;
            byte[] segment;
            while ((len = inputStream.readByte()) > 0) {
                segment = new byte[len];
                for (int i = 0; i < len; i++) segment[i] = inputStream.readByte();
                qName += new String(segment, StandardCharsets.UTF_8) + ".";
            }
            //erasing trailing part
            qName = qName.substring(0, qName.length() - 1);
            System.out.println("url: " + qName);
            int k;
            store = Record.read("records.txt");
            ArrayList<Integer> ipm = new ArrayList<Integer>();
            for(k=0; k<store.size(); k++)
                if(store.get(k).getName().equals(qName))ipm.add(k);

            Record.write(store,"records.txt");
            short qType = inputStream.readShort();
            short qClass = inputStream.readShort();

            //Starting writing output
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
            outputStream.writeShort(rid); // request id
            outputStream.writeShort(0x8180); // flags
            outputStream.writeShort(1); // number of question
            outputStream.writeShort(ipm.size());// ans RRs
            outputStream.writeShort(0); // auth RRs
            outputStream.writeShort(0); // additional RRs

            String[] domSegment = qName.split("\\.");

            int qLen = 0;
            for (int i = 0; i < domSegment.length; i++) {
                byte[] domainBytes = domSegment[i].getBytes(StandardCharsets.UTF_8);
                outputStream.writeByte(domainBytes.length);
                outputStream.write(domainBytes);
                qLen += domainBytes.length + 1;
            }
            outputStream.writeByte(0);
            qLen++;
            outputStream.writeShort(qType);// Type 0x01 = A (Host Request)
            outputStream.writeShort(qClass);// Class 0x01 = IN
            int cnt = 0;
            while (cnt<ipm.size()){
                int nmPos = 12 + qLen + 4; // header+query length(12bytes), type+class(4bytes)
                int ofset = nmPos - 12; // start of message (12 bytes)
                outputStream.writeShort(0xc000 | ofset); // first two bits - 11
                outputStream.writeShort(qType); // TYPE
                outputStream.writeShort(qClass); // CLASS
                outputStream.writeInt(store.get(ipm.get(cnt)).getTTL()); // TTL
                InetAddress inet = InetAddress.getByName(store.get(ipm.get(cnt)).getValue());
                byte[] ipa = inet.getAddress();
                outputStream.writeShort(ipa.length);
                outputStream.write(ipa);
                cnt++;
            }
            return byteArrayOutputStream.toByteArray();
        }
    }
    public static class Record {
        private short ttl;
        private short type;
        private String value;
        private String name;
        public Record(String name, String value, short type, short ttl) {
            this.name = name;
            this.value = value;
            this.type = type;
            this.ttl = ttl;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
        public String getType() {
            return switch (this.type) {
                case 1 -> "A";
                case 2 -> "AAAA";
                case 3 -> "NS";
                case 4 -> "CNAME";
                case 5 -> "MX";
                default -> "null";
            };
        }
        public void setType(short type) {
            this.type = type;
        }
        public short getTTL() {
            return this.ttl;
        }
        public void setTtl(short ttl) {
            this.ttl = ttl;
        }
        public static void write(ArrayList<Record> records, String fileName){
            try (PrintWriter printWriter = new PrintWriter(new FileWriter(fileName))) {
                printWriter.println("Name\tValue\tType\tTTL");
                int k=0;
                for (Record single : records) {
                    if(k!=0) {
                        printWriter.printf("%s\t%s\t%s\t%d%n",
                                single.getName(),
                                single.getValue(),
                                single.getType(),
                                single.getTTL());
                    }
                    k++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public static ArrayList<Record> read(String fileName) throws IOException {
            new BufferedReader(new FileReader(fileName));
            BufferedReader bufferedReader;
            String newLine;
            bufferedReader = new BufferedReader(new FileReader(fileName));
            ArrayList<Record> records = new ArrayList<>();
            while ((newLine = bufferedReader.readLine()) != null) {
                String[] parts = newLine.split("\\s+");
                String name = parts[0];
                String value = parts[1];
                short type = switch (parts[2]) {
                    case "A" -> 1;
                    case "AAAA" -> 2;
                    case "NS" -> 3;
                    case "CNAME" -> 4;
                    case "MX" -> 5;
                    default -> 6;
                };
                short timeToLeave = 60;
                records.add(new Record(name, value, type, timeToLeave));
            }
            bufferedReader.close();
            return records;
        }
    }
}
