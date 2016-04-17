import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 4/17/16.
 */
public interface PolicyInfoPlugin {

    public void fetch(ArrayList<Data> data, Config config);
    public void notifyNewNodesCreation(ArrayList<Node> nodes);
    public void notifyRejectionFromOverlord();
    public void notifyDeletionOfNodes(ArrayList<Node> nodes);
    public void notifyCreateResponseReceived(ArrayList<Node> nodes);
    public void notifyRequestToReleaseReceived(ArrayList<Node> nodes);
    public void notifyElapseOfTime(double seconds);
    public String updateStateInfo(String jsonString);

}
