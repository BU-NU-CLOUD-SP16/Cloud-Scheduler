import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 3/4/16.
 */
public interface ClusterScalerPlugin {

    public Node createNewNode(Node node);
    public boolean deleteNode(Node node);
    public void setup(Config config, ArrayList<Node> nodes);
}
