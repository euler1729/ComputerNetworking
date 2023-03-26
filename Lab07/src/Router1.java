import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Router1 {
    private static final String MULTICAST_ADDRESS = "224.0.0.1";// router 1, 2
    private static final int PORT1 = 3001;
    private static final int router_id = 1;
    private  static Network network = new Network();
    private static int msgId = 1;
    private static Map<Integer,Boolean> msgMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        utils util = new utils(); //For operations
        util.init(network); //initializes the network
        System.out.println("Network state of router-"+router_id+" at the Beginning: ");
        util.getPath(network, router_id);


        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
        MulticastSocket socket = new MulticastSocket(PORT1);
        socket.joinGroup(group);
        try{
            Thread receiveThread = new Thread(()->{
                while (true){
                    try{
                        Node node = util.receivePacket(socket);
                        if(msgMap.get(node.getMessageId())!=null)continue;
                        msgMap.put(node.getMessageId(), true);
                        util.deleteNode(network,node.getId());
                        util.addNode(network,node);
                        System.out.println("\nRouterID: "+node.getId()+" MessageID: "+node.getMessageId());
                        util.getPath(network,router_id);
                        System.out.println("\n\n");
                    }catch (Exception exp){
                        exp.printStackTrace();
                    }
                }
            });
            Thread sendThread = new Thread(()->{
                while(true){
                    try {
                        Thread.sleep(10000);
                        for(Node node: network.getNodes()){
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
                                System.out.println("\nRouterID: "+newNode.getId()+" MessageID: "+newNode.getMessageId());
                                util.getPath(network, router_id);
                                util.sendPacket(group, PORT1, socket, util.getByteStream(newNode));
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            receiveThread.start();
            sendThread.start();
        }catch (Exception exp){
            socket.close();
            exp.printStackTrace();
        }
    }

}
