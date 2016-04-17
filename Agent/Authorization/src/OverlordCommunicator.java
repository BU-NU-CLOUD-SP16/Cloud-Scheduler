import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.ArrayList;

/**
 * <h1>OverlordCommunicator</h1>
 * This class opens up all methods
 * to communicate to the Overlord.
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-03-25
 */
public class OverlordCommunicator {


    private String overlordIp;
    private int overlordPort;
    private String agentId;


    /**
     * <h1>OverlordCommunicator</h1>
     * Constructor
     * @param overlordIp
     * @param overlordPort
     */
    public OverlordCommunicator(String overlordIp, int overlordPort) {
        this.overlordIp = overlordIp;
        this.overlordPort = overlordPort;
    }

    /**
     * <h1>register</h1>
     * @param state ClusterState
     * the Cluster Registers with the Overlord.
     */
    public void register(ClusterState state)
    {

        this.agentId = state.getId();

        JsonObject object = new JsonObject();
        object.addProperty("id",state.getId());
        object.addProperty("priority",state.getPriority());
        object.addProperty("minNodes",state.getMinNodes());
        object.addProperty("port",state.getPort());
        JsonArray array = new JsonArray();

        ArrayList<Node> slaves = state.getNodesInCluster();

        for (Node node : slaves)
        {
            OpenStackNode openStackNode = (OpenStackNode) node;
            JsonObject object1 = new JsonObject();
            object1.addProperty("name",openStackNode.getHostname());
            array.add(object1);
        }

        object.add("nodes",array);

        try {
            Unirest.post("http://"+overlordIp+":"+overlordPort+"/registerCEAgent").body(object.toString()).asString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    /**
     * <h1>createNode</h1>
     * Creates a node based on Overlords response.
     * @param number
     * @return String
     */
    public String createNode(int number)
    {
        JsonObject object = new JsonObject();
        object.addProperty("ceAgentID",agentId);
        object.addProperty("number",number);

        try {
           HttpResponse<String> response = Unirest.post("http://"+overlordIp+":"+overlordPort+"/requestNode").body(object.toString()).asString();
           return ""+response.getStatus();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return null;
    }
}
