import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Router4 {
    private static final String MULTICAST_ADDRESS2 = "224.0.0.3";
    private static final String MULTICAST_ADDRESS5 = "224.0.0.6";
    private static final int PORT2 = 3003;
    private static final int PORT5 = 3006;
    private static final int router_id = 4;
    private  static Network network = new Network();
    private static int msgId = 40000;
    private static Map<Integer,Boolean> msgMap = new HashMap<>();
    public static void main(String[] args) throws IOException {
        utils util = new utils(); //For operations
        util.init(network); //initializes the network
        System.out.println("Network state of router-"+router_id+" at the Beginning: ");
        util.getPath(network, router_id);

        InetAddress group2 = InetAddress.getByName(MULTICAST_ADDRESS2);
        InetAddress group5 = InetAddress.getByName(MULTICAST_ADDRESS5);
        MulticastSocket socket2 = new MulticastSocket(PORT2);
        MulticastSocket socket5 = new MulticastSocket(PORT5);
        socket2.joinGroup(group2);
        socket5.joinGroup(group5);

        try{
            Thread recv2 = new Thread(()->{
                while(true){
                    try{
                        Node node = util.receivePacket(socket2);
                        if(msgMap.get(node.getMessageId())!=null)continue;
                        msgMap.put(node.getMessageId(),true);
                        util.deleteNode(network, node.getId());
                        util.addNode(network, node);
                        System.out.println("\nRouterID: "+node.getId()+" MessageID: "+node.getMessageId());
//                        System.out.println("Network state of messageID:"+node.getMessageId());
//                        util.networkState(network);
                        util.getPath(network, router_id);
                        util.sendPacket(group5, PORT5, socket5,util.getByteStream(node));
                    }catch (Exception exp){
                        exp.printStackTrace();
                        break;
                    }
                }
            });
            Thread recv5 = new Thread(()->{
                while(true){
                    try{
                        Node node = util.receivePacket(socket5);
                        if(msgMap.get(node.getMessageId())!=null)continue;
                        msgMap.put(node.getMessageId(),true);
                        util.deleteNode(network, node.getId());
                        util.addNode(network, node);
                        System.out.println("\nRouterID: "+node.getId()+" MessageID: "+node.getMessageId());
//                        System.out.println("Network state of messageID:"+node.getMessageId());
//                        util.networkState(network);
                        util.getPath(network, router_id);
                        util.sendPacket(group2, PORT2, socket2,util.getByteStream(node));
                    }catch (Exception exp){
                        exp.printStackTrace();
                        break;
                    }
                }
            });

            Thread sendThread = new Thread(()->{
                while (true){
                    try {
                        Thread.sleep(10000);
                        for(Node node: network.getNodes()){
                            System.out.println(node.getId());
                            if(node.getId()==router_id){
                                Node newNode = node;
//                                System.out.println("Before: ");
//                                util.networkState(network);
                                for(Map.Entry<Integer,Integer>mp: node.getNeighbors().entrySet()){
                                    newNode.addNeighbor(mp.getKey(),util.getRandom());
                                }
                                newNode.setMessageId(msgId);
                                msgMap.put(msgId, true);
                                msgId++;
                                util.deleteNode(network, node.getId());
                                util.addNode(network, newNode);
//                                System.out.println("After: ");
//                                util.networkState(network);
                                System.out.println("\nRouterID: "+node.getId()+" MessageID: "+node.getMessageId());
                                util.getPath(network, router_id);
                                util.sendPacket(group2, PORT2, socket2, util.getByteStream(newNode));
                                util.sendPacket(group5, PORT5, socket5, util.getByteStream(newNode));
                                break;
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        break;
                    }
                }
            });
            recv2.start();
            recv5.start();
            sendThread.start();
        }catch (Exception exp){
            socket2.close();
            socket5.close();
            exp.printStackTrace();
        }
    }
}
