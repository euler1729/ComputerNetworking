import java.util.HashMap;
import java.util.Map;

public class Node {
    private int id;
    private Map<Integer, Integer> neighbors;
    public Node(int id){
        this.id = id;
        this.neighbors = new HashMap<>();
    }
    public int getId(){
        return id;
    }
    public void addNeighbor(int neighborId, int distance){
        neighbors.put(neighborId, distance);
    }
    public Map<Integer, Integer> getNeighbors(){
        return neighbors;
    }
}
