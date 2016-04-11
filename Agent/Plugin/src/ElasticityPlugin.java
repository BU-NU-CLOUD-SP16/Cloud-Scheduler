import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public interface ElasticityPlugin {

    public ArrayList<Node> scaleUp();
    public ArrayList<Node> scaleDown();
    public ArrayList<Node> requestResources(String parameters);
    public ArrayList<Node> fetch(ArrayList<Data> data,Config config);
    public ArrayList<Node> getNodes();
    public ArrayList<String> getFrameworkNames();
    public void notifyNewNodeCreation(Node node);
    public ArrayList<Node> receivedReleaseNodeRequest(String string);
    public ArrayList<Node> receivedCreateNodeResponse(String json);
}
