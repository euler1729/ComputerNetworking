import java.util.*;

public class Dijkstra {
    private Network network;
    public Dijkstra(Network network){
        this.network = network;
    }
    public Map<Integer, Integer> getShortestPath(int sourceId){
        Map<Integer, Integer> distances = new HashMap<>();
        Set<Integer> visited = new HashSet<>();
        PriorityQueue<Node> queue = new PriorityQueue<>
                (Comparator.comparing(node->distances.getOrDefault(node.getId(), Integer.MAX_VALUE)));

        for(Node node: network.getNodes()){
            distances.put(node.getId(), Integer.MAX_VALUE);
        }
        distances.put(sourceId, 0);
        queue.offer(getNodeById(sourceId));
        while(!queue.isEmpty()){
            Node current = queue.poll();
            visited.add(current.getId());
            for(Map.Entry<Integer, Integer> neighbor: current.getNeighbors().entrySet()){
                int neighborId = neighbor.getKey();
                int distance = neighbor.getValue();
                if(!visited.contains(neighborId)){
                    int newDistance = distances.get(current.getId())+distance;
                    if(newDistance<distances.get(neighborId)){
                        distances.put(neighborId,newDistance);
                        queue.offer(getNodeById(neighborId));
                    }
                }
            }
        }
        return distances;
    }
    private Node getNodeById(int nodeId){
        for(Node node:network.getNodes()){
            if(node.getId()==nodeId) return node;
        }
        return null;
    }
}
