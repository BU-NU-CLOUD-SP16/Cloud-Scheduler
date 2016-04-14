import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 4/14/16.
 */
public class AgingPolicyPlugin implements PolicyPlugin {


    private static final String CLUSTER_ID_JSON = "id";
    private static final String CLUSTER_PRIORITY_JSON = "priority";
    private static final String CLUSTER_MINIMUM_NODES_JSON = "minNodes";
    private static final String CLUSTER_PORT_JSON = "port";
    private static final String CLUSTER_NODES_JSON = "nodes";
    private static final String CLUSTER_STATUS_JSON = "status";
    private static final String NODE_NAME_JSON = "nodes";
    private static final String CLUSTER_AGENT_IP_JSON = "ip";

    private AgentList registeredAgents;
    private ArrayList<Node> nodes;

    @Override
    public void registerAgent(String json) {
        Gson gson = new Gson();
        JsonObject object = gson.fromJson(json, JsonObject.class);

        int ceAgentID = object.get(CLUSTER_ID_JSON).getAsInt();
        double priority = object.get(CLUSTER_PRIORITY_JSON).getAsDouble();
        int minFixedNodes = object.get(CLUSTER_MINIMUM_NODES_JSON).getAsInt();
        int port = object.get(CLUSTER_PORT_JSON).getAsInt();
        int status = object.get(CLUSTER_STATUS_JSON).getAsInt();
        String ceAgentIP = object.get(CLUSTER_AGENT_IP_JSON).getAsString();

        JsonArray jsonArray = object.get(CLUSTER_NODES_JSON).getAsJsonArray();

        ArrayList<Node> nodes = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            Node node = findNodeWithName(jsonElement.getAsJsonObject().get(NODE_NAME_JSON).getAsString());
            if (node != null)
                nodes.add(node);
        }


        Agent ceAgent = new Agent();
        ceAgent.setId(ceAgentID);
        ceAgent.setPriority(priority);
        ceAgent.setIp(ceAgentIP);
        ceAgent.setPort(port);
        ceAgent.setMinFixedNodes(minFixedNodes);
        ceAgent.setNodeList(nodes);
        ceAgent.setStatus(status);
        registeredAgents.add(ceAgent);
    }

    @Override
    public void requestNode(String json) {

    }
}
