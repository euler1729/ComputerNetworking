import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.Random;
import java.io.Serializable;

public class utils{
    public Node receivePacket(MulticastSocket socket) throws IOException, ClassNotFoundException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        ObjectInputStream objectStream = new ObjectInputStream(stream);
        return (Node) objectStream.readObject();
    }
    public void sendPacket(InetAddress group, int port, MulticastSocket socket, byte[] buffer) throws IOException {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(packet);
    }
    public byte[] getByteStream(Node node) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);
        objectOutputStream.writeObject(node);
        return byteStream.toByteArray();
    }
    public void getPath(Network network, int router_id){
        Dijkstra dijkstra = new Dijkstra(network);
        Object[] shortestPath= dijkstra.getShortestPath(router_id);
        Map<Integer,Integer> cost = (Map<Integer, Integer>) shortestPath[0];
        Map<Integer,Integer> path = (Map<Integer, Integer>) shortestPath[1];

        System.out.println("Shortest paths:");
        for(int i=1; i<=5; ++i){
            printPath(path, i);
            System.out.println(i+" cost: "+cost.get(i));
        }
    }
    public void printPath(Map<Integer,Integer>map, int child){
        int parent = map.get(child);
        if(parent==-1) return;
        printPath(map, parent);
        System.out.print(parent+"->");
    }
    public void deleteNode(Network network, int id){
        network.getNodes().removeIf(element -> element.getId() == id);
    }
    public void addNode(Network network, Node node){
        network.addNode(node);
    }
    public void init(Network network){
        Node node1 = new Node(1);
        Node node2 = new Node(2);
        Node node3 = new Node(3);
        Node node4 = new Node(4);
        Node node5 = new Node(5);

        node1.addNeighbor(2,1);
        node2.addNeighbor(1,1);
        node2.addNeighbor(3,2);
        node2.addNeighbor(4,3);
        node3.addNeighbor(2,2);
        node3.addNeighbor(5,2);
        node4.addNeighbor(2,3);
        node4.addNeighbor(5,1);
        node5.addNeighbor(3,2);
        node5.addNeighbor(4,1);

        network.addNode(node1);
        network.addNode(node2);
        network.addNode(node3);
        network.addNode(node4);
        network.addNode(node5);
    }
    public void networkState(Network network){
        for(Node node: network.getNodes()){
            System.out.println("Router ID:"+node.getId()+" msgID:"+node.getMessageId());
            for(Map.Entry<Integer,Integer> mp: node.getNeighbors().entrySet()){
                System.out.println(mp.getKey()+" "+mp.getValue());
            }
        }
    }
    public int getRandom(){
        Random random = new Random();
        return random.nextInt(5)+1;
    }
}
