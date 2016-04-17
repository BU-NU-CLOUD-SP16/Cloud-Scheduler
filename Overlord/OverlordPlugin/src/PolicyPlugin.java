import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chemistry_sourabh on 4/14/16.
 */
public interface PolicyPlugin {

    public void setup(Config config);
    public void registerAgent(String ip,String json);
    public String requestNode(String json);
    public void updateState(HashMap<String,String> clusterStates,ArrayList<Node> nodesInCloud);
    public HashMap<String,String> getAgentHostnames();
}
