public class Main {
    public static void main(String[] args) {
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

        Network network = new Network();
        network.addNode(node1);
        network.addNode(node2);
        network.addNode(node3);
        network.addNode(node4);
        network.addNode(node5);

        Thread thread = new Thread(new LinkState(network));
        thread.start();
    }
}
