import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public interface ElasticityPlugin {

    public ArrayList<Node> scaleUp();
    public ArrayList<Node> scaleDown();
    public ArrayList<Node> requestResources(String parameters);
    public void fetch(ArrayList<Data> data);
    public void notifyNewNodeCreation(Node node);

}
