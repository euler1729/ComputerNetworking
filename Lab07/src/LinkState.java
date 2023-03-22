import java.util.*;

public class LinkState implements Runnable{
    private Network network;
    public LinkState(Network network){
        this.network = network;
    }
    @Override
    public void run(){
        //Broadcast network topology to all nodes
        for(Node node: network.getNodes()){
            broadcastTopology(node);
        }
        for(Node node: network.getNodes()){
            Dijkstra dijkstra = new Dijkstra(network);
            Map<Integer, Integer> shortestPath = dijkstra.getShortestPath(node.getId());
            System.out.println("Shortest Path from node "+node.getId()+": "+shortestPath);
        }
    }
    private void broadcastTopology(Node source){
        Map<Integer, Integer> distances = new HashMap<>();
        for(Node node: network.getNodes()){
            if(node.getId()==source.getId()){
                distances.put(node.getId(), 0);
            }else{
                distances.put(node.getId(),Integer.MAX_VALUE);
            }
        }
        Set<Integer> visited = new HashSet<>();
        visited.add(source.getId());
        Queue<Node> queue = new LinkedList<>();
        queue.offer(source);
        while (!queue.isEmpty()){
            Node current = queue.poll();
            for(Map.Entry<Integer, Integer> neighbor: current.getNeighbors().entrySet()){
                int id = neighbor.getKey();
                int dis = neighbor.getValue();
                if(!visited.contains(id)){
                    int newDis = distances.get(current.getId())+dis;
                    if(newDis<distances.get(id)){
                        distances.put(id, newDis);
                    }
                    queue.offer(getNodeById(id));
                }
            }
        }
        for(Node node: network.getNodes()){
            if(node.getId()!=source.getId()){
                Map<Integer, Integer> newNeighbor = new HashMap<>();
                for(Map.Entry<Integer, Integer> neigh: node.getNeighbors().entrySet()){
                    int id = neigh.getKey();
                    if(visited.contains(id)){
                        newNeighbor.put(id, distances.get(id));
                    }
                }
                node.getNeighbors().clear();
                node.getNeighbors().putAll(newNeighbor);
            }
        }
    }
    private Node getNodeById(int nodeId){
        for(Node node: network.getNodes()){
            if(node.getId()==nodeId){
                return node;
            }
        }
        return null;
    }
}
