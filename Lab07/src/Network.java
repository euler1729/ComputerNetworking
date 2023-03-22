import java.util.ArrayList;
import java.util.List;

public class Network {
    private List<Node> nodes;
    public Network(){
        nodes = new ArrayList<>();
    }
    public void addNode(Node node){
        nodes.add(node);
    }
    public List<Node> getNodes(){
        return nodes;
    }
}