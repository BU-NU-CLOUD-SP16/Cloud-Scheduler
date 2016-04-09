import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 3/25/16.
 */
public class OverlordCommunicator {


    private String overlordIp;
    private int overlordPort;
    private String agentId;


    public OverlordCommunicator(String overlordIp, int overlordPort) {
        this.overlordIp = overlordIp;
        this.overlordPort = overlordPort;
    }

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
