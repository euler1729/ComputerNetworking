public class Main {
    public static void main(String[] args) {
        Node node1 = new Node(1);
        Node node2 = new Node(2);
        Node node3 = new Node(3);
        Node node4 = new Node(4);

        node1.addNeighbor(2, 10);
        node1.addNeighbor(3, 15);
        node2.addNeighbor(1, 10);
        node2.addNeighbor(3, 35);
        node2.addNeighbor(4, 25);
        node3.addNeighbor(1, 15);
        node3.addNeighbor(2, 35);
        node3.addNeighbor(4, 30);
        node4.addNeighbor(2, 25);
        node4.addNeighbor(3, 30);

        Network network = new Network();
        network.addNode(node1);
        network.addNode(node2);
        network.addNode(node3);
        network.addNode(node4);

        LinkState linkState = new LinkState(network);
        linkState.run();
    }
}
