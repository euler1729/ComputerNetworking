//package client;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Client {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
//        System.out.print("Enter DNS Implementation type: ");
//        String type = "Itr";
//        type = scanner.nextLine();
//        System.out.print("Enter port: ");
//        int port = scanner.nextInt();

        try {
            while (true) {
                System.out.print("Enter domain name: ");
                String url = scanner.nextLine();
//                url = scanner.nextLine();
                long start = System.currentTimeMillis();
                parseIp(handleReq(createQuery(url,(short) 1),localIP(),1202));
                long finish = System.currentTimeMillis();
                System.out.println("Time taken in recursive: " +(finish-start)+"ms");
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        scanner.close();
    }
    //gives local ip
    private static String localIP() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet6Address) continue;
                    ip = addr.getHostAddress();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }
    //creates request byte array
    public static byte[] createQuery(String url, short type) throws IOException {
        short flg = Short.parseShort("0000000100000000", 2);
        ByteBuffer byteBuffer = ByteBuffer.allocate(2).putShort(flg);
        byte[] flag = byteBuffer.array();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeShort((short) new Random().nextInt((1 << 15) - 1));
        dataOutputStream.write(flag);
        short QD = 1;
        dataOutputStream.writeShort(QD);
        short AN = 0;
        dataOutputStream.writeShort(AN);
        short NS = 0;
        dataOutputStream.writeShort(NS);
        short AR = 0;
        dataOutputStream.writeShort(AR);
        //splitting domain name by parts
        String[] labels = url.split("\\.");
        for (String label : labels) {
            byte[] part = label.getBytes(StandardCharsets.UTF_8);
            dataOutputStream.writeByte(part.length);
            dataOutputStream.write(part);
        }
        dataOutputStream.writeByte(0);//ending of domain name
        dataOutputStream.writeShort(type);// Type 0x01 = A (Host Request)
        dataOutputStream.writeShort(1);// Class 0x01 = IN
        byte[] query = byteArrayOutputStream.toByteArray();
        return query;
    }
    //sends and receives request
    public static byte[] handleReq(byte[] query, String ip, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
//        System.out.println("query: "+ Arrays.toString(query));
        DatagramPacket request = new DatagramPacket(query, query.length,
                InetAddress.getByName(ip), port);
        socket.send(request);
        byte[] resMsg = new byte[512];
        DatagramPacket response = new DatagramPacket(resMsg, resMsg.length);
        socket.receive(response);
        socket.close();
//        System.out.println("response: "+Arrays.toString(response.getData()));
        return response.getData();
    }
    //parses ip resolution from server response
    public static void parseIp(byte[] response) throws IOException {
        DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(response));
        System.out.println("req id: " + dataStream.readShort());
        short flg = dataStream.readByte();
        System.out.println("QR " + ((flg & 0b10000000) >>> 7));
        System.out.println("OP-code " + ((flg & 0b01111000) >>> 3));
        System.out.println("AA " + ((flg & 0b00000100) >>> 2));
        System.out.println("TC " + ((flg & 0b00000010) >>> 1));
        System.out.println("RD " + (flg & 0b00000001));
        flg = dataStream.readByte();
        System.out.println("RA " + ((flg & 0b10000000) >>> 7));
        System.out.println("Z " + ((flg & 0b01110000) >>> 4));
        System.out.println("RC " + (flg & 0b00001111));
        short QD = dataStream.readShort();
        System.out.println("QD: " + QD);
        short AN = dataStream.readShort();
        System.out.println("AnsRRs: " + AN);
        short NS = dataStream.readShort();
        System.out.println("AuthRRs: " + NS);
        short AR = dataStream.readShort();
        System.out.println("AddiRRs: " + AR);
        String qnm = "";
        int len;
        byte[] rec;
        while ((len = dataStream.readByte()) > 0) {
            rec = new byte[len];
            for (int i = 0; i < len; i++) rec[i] = dataStream.readByte();
            qnm += new String(rec, StandardCharsets.UTF_8) + ".";
        }
        qnm = qnm.substring(0, qnm.length() - 1);
        short type = dataStream.readShort();
        short cls = dataStream.readShort();
        System.out.println("RECORD: " + qnm);
        System.out.println("RECORD Type: " + type);
        System.out.println("CLASS: " + cls);
        String ipf;
        ByteArrayOutputStream label = new ByteArrayOutputStream();
        Map<String, String> resIp = new HashMap<>();
        byte fbyte = dataStream.readByte();
        int ftbyte = (fbyte & 0b11000000) >>> 6;
        for (int i = 0; i < AN; i++) {
            if (ftbyte == 3) {
                byte curr = dataStream.readByte();
                byte[] arr = Arrays.copyOfRange(response, curr, response.length);
                DataInputStream dataInputStream1 = new DataInputStream(new ByteArrayInputStream(arr));
                ArrayList<Integer> rdt = new ArrayList<>();
                ArrayList<String> domns = new ArrayList<>();
                boolean stp = false;
                while (!stp) {
                    byte nxbyte = dataInputStream1.readByte();
                    if (nxbyte != 0) {
                        byte[] curLabel = new byte[nxbyte];
                        for (int k = 0; k < nxbyte; k++) curLabel[k] = dataInputStream1.readByte();
                        label.write(curLabel);
                    }
                    else {
                        stp = true;
                        short TYPE = dataStream.readShort();
                        short CLASS = dataStream.readShort();
                        int TTL = dataStream.readInt();
                        int RDL = dataStream.readShort();
                        for (int j = 0; j < RDL; j++) {
                            int nx = dataStream.readByte() & 255;
                            rdt.add(nx);
                        }
                        System.out.println("TYPE: " + TYPE);
                        System.out.println("CLASS: " + CLASS);
                        System.out.println("TTL: " + TTL);
                        System.out.println("RD LEN: " + RDL);
                    }
                    domns.add(label.toString(StandardCharsets.UTF_8));
                    label.reset();
                }
                StringBuilder ip = new StringBuilder();
                StringBuilder domainSb = new StringBuilder();
                for (Integer ipPart : rdt) ip.append(ipPart).append(".");
                for (String domainPart : domns) if (!domainPart.equals("")) domainSb.append(domainPart).append(".");
                String domainFinal = domainSb.toString();
                ipf = ip.toString();
                resIp.put(ipf.substring(0, ipf.length() - 1), qnm);
            }
            fbyte = dataStream.readByte();
            ftbyte = (fbyte & 0b11000000) >>> 6;
        }
        for (int i = 0; i < NS; i++) {
            if (ftbyte == 3) {
                byte curr = dataStream.readByte();
                boolean stp = false;
                byte[] arr = Arrays.copyOfRange(response, curr, response.length);
                DataInputStream dataInputStream1 = new DataInputStream(new ByteArrayInputStream(arr));
                ArrayList<Integer> rdat = new ArrayList<>();
                ArrayList<String> domns = new ArrayList<>();
                while (!stp) {
                    byte nextByte = dataInputStream1.readByte();
                    if (nextByte != 0) {
                        byte[] curLabel = new byte[nextByte];
                        for (int j = 0; j < nextByte; j++) curLabel[j] = dataInputStream1.readByte();
                        label.write(curLabel);
                    }
                    else {
                        stp = true;
                        short TYPE = dataStream.readShort();
                        short CLASS = dataStream.readShort();
                        int TTL = dataStream.readInt();
                        int RDL = dataStream.readShort();
                        for (int s = 0; s < RDL; s++) {
                            int nx = dataStream.readByte() & 255;// and with 255 to
                            rdat.add(nx);
                        }
                        System.out.println("TYPE: " + TYPE);
                        System.out.println("CLASS: " + CLASS);
                        System.out.println("TTL: " + TTL);
                        System.out.println("RD LEN: " + RDL);
                    }
                    domns.add(label.toString(StandardCharsets.UTF_8));
                    label.reset();
                }
                StringBuilder ips = new StringBuilder();
                StringBuilder domainSb = new StringBuilder();
                for (Integer ipPart : rdat) ips.append(ipPart).append(".");
                for (String domainPart : domns) if (!domainPart.equals("")) domainSb.append(domainPart).append(".");
                String domainFinal = domainSb.toString();
                ipf = ips.toString();
                resIp.put(ipf.substring(0, ipf.length() - 1), domainFinal.substring(0, domainFinal.length() - 1));
            }
            fbyte = dataStream.readByte();
            ftbyte = (fbyte & 0b11000000) >>> 6;
        }
        resIp.forEach((k, v) -> System.out.println(k + ": " + v));
    }
}