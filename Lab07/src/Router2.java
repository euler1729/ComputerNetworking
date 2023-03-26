import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Router2{
    private static final String MULTICAST_ADDRESS1 = "224.0.0.1"; // router 1,2
    private static final String MULTICAST_ADDRESS3 = "224.0.0.2"; // router 2,3
    private static final String MULTICAST_ADDRESS4 = "224.0.0.3"; // router 2,4
    private static final int PORT1 = 3001;
    private static final int PORT3 = 3002;
    private static final int PORT4 = 3003;
    private static final int router_id = 2;
    private  static Network network = new Network();
    private static int msgId = 20000;
    private static Map<Integer,Boolean> msgMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        utils util = new utils(); //For operations
        util.init(network); //initializes the network
        System.out.println("Network state of router-"+router_id+" at the Beginning: ");
        util.getPath(network, router_id);

        //initializing connections
        InetAddress group1 = InetAddress.getByName(MULTICAST_ADDRESS1);
        InetAddress group3 = InetAddress.getByName(MULTICAST_ADDRESS3);
        InetAddress group4 = InetAddress.getByName(MULTICAST_ADDRESS4);
        MulticastSocket socket1 = new MulticastSocket(PORT1);
        MulticastSocket socket3 = new MulticastSocket(PORT3);
        MulticastSocket socket4 = new MulticastSocket(PORT4);
        socket1.joinGroup(group1);
        socket3.joinGroup(group3);
        socket4.joinGroup(group4);

        try{
            Thread recv1 = new Thread(()->{
                while(true){
                    try{
                        Node node = util.receivePacket(socket1);
                        if(msgMap.get(node.getMessageId())!=null)continue;
                        msgMap.put(node.getMessageId(),true);
                        util.deleteNode(network, node.getId());
                        util.addNode(network, node);
                        System.out.println("\nRouterID: "+node.getId()+" MessageID: "+node.getMessageId());
//                        System.out.println("Network state of messageID:"+node.getMessageId());
//                        util.networkState(network);
                        util.getPath(network, router_id);
                        util.sendPacket(group3, PORT3, socket3,util.getByteStream(node));
                        util.sendPacket(group4, PORT4, socket4,util.getByteStream(node));
                    }catch (Exception exp){
                        exp.printStackTrace();
                        break;
                    }
                }
            });
            Thread recv2 = new Thread(()->{
                while(true){
                    try{
                        Node node = util.receivePacket(socket3);
                        if(msgMap.get(node.getMessageId())!=null)continue;
                        msgMap.put(node.getMessageId(),true);
                        util.deleteNode(network, node.getId());
                        util.addNode(network, node);
                        System.out.println("\nRouterID: "+node.getId()+" MessageID: "+node.getMessageId());
//                        System.out.println("Network state of messageID:"+node.getMessageId());
//                        util.networkState(network);
                        util.getPath(network, router_id);
                        util.sendPacket(group1, PORT1, socket1,util.getByteStream(node));
                        util.sendPacket(group4, PORT4, socket4,util.getByteStream(node));
                    }catch (Exception exp){
                        exp.printStackTrace();
                        break;
                    }
                }
            });
            Thread recv3 = new Thread(()->{
                while(true){
                    try{
                        Node node = util.receivePacket(socket4);
                        if(msgMap.get(node.getMessageId())!=null)continue;
                        msgMap.put(node.getMessageId(),true);
                        util.deleteNode(network, node.getId());
                        util.addNode(network, node);
                        System.out.println("\nRouterID: "+node.getId()+" MessageID: "+node.getMessageId());
//                        System.out.println("Network state of messageID:"+node.getMessageId());
//                        util.networkState(network);
                        util.getPath(network, router_id);
                        util.sendPacket(group1, PORT1, socket1,util.getByteStream(node));
                        util.sendPacket(group3, PORT3, socket3,util.getByteStream(node));
                    }catch (Exception exp){
                        exp.printStackTrace();
                        break;
                    }
                }
            });

            Thread sendThread = new Thread(()->{
                while(true){
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
                                util.sendPacket(group1, PORT1, socket1, util.getByteStream(newNode));
                                util.sendPacket(group3, PORT3, socket3, util.getByteStream(newNode));
                                util.sendPacket(group4, PORT4, socket4, util.getByteStream(newNode));
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            recv1.start();
            recv2.start();
            recv3.start();
            sendThread.start();
        }catch (Exception exp){
            socket1.close();
            socket3.close();
            socket4.close();
            exp.printStackTrace();
        }
    }
}