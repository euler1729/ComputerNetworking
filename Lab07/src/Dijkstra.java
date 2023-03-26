import java.util.*;

public class Dijkstra {
    private Network network;

    public Dijkstra(Network network) {
        this.network = network;
    }

    public Object[] getShortestPath(int sourceId) {
        Map<Integer, Integer> distances = new HashMap<>();
        Map<Integer,Integer> parents = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>
                (Comparator.comparing(node -> distances.getOrDefault(node.getId(), 99999)));

        for (Node node : network.getNodes()) {
            distances.put(node.getId(), 99999);
        }
        distances.put(sourceId, 0);
        parents.put(sourceId,-1);
        queue.offer(getNodeById(sourceId));
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            for (Map.Entry<Integer, Integer> neighbor : current.getNeighbors().entrySet()) {
                int neighborId = neighbor.getKey();
                int distance = neighbor.getValue();
                int newDistance = distances.get(current.getId()) + distance;
                if (newDistance < distances.get(neighborId)) {
                    distances.put(neighborId, newDistance);
                    parents.put(neighborId,current.getId());
                    queue.offer(getNodeById(neighborId));
                }
            }
        }
        return new Object[]{distances, parents};
    }

    private Node getNodeById(int nodeId) {
        for (Node node : network.getNodes()) {
            if (node.getId() == nodeId) return node;
        }
        return null;
    }
}
