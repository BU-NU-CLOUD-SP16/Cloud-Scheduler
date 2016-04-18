import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.ArrayList;

/**
 * <h1>OverlordCommunicator</h1>
 * Overlord Communicator contacts the agents
 * in each clusters and based on the priority
 * asks agents to exchange nodes or gives
 * permission to create a new node from the
 * openstack.
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-03-16
 */
public class OverlordCommunicator {

    private String username;
    private String password;

    /**
     * <h1>getUsername</h1>
     * @return String
     */
    public String getUsername() {
        return username;
    }

    /**
     * <h1>setUsername</h1>
     * @param username String
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * <h1>getPassword</h1>
     * @return String
     */
    public String getPassword() {
        return password;
    }

    /**
     * <h1>setPassword</h1>
     * @param password String
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * <h1>register</h1>
     * registers a node with one of the clusters.
     * @param id String
     * @param port String
     * @param slaves String
     */
    public void register(String id,String port,ArrayList<Node> slaves)
    {

        JsonObject object = new JsonObject();
        object.addProperty("ceAgentID",id);
        object.addProperty("port",port);
        JsonArray array = new JsonArray();

        for (Node node : slaves)
        {
            OpenStackNode openStackNode = (OpenStackNode) node;
            JsonObject object1 = new JsonObject();
            object1.addProperty("name",openStackNode.getHostname());
            array.add(object1);
        }

        object.add("nodes",array);

        try {
            Unirest.post("http://localhost:6000/registerCEAgent").body(object.toString()).asString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    /**
     * <h1>list</h1>
     * @param id String
     * @return String
     * the http response.
     */
    public String list(String id)
    {
        JsonObject object = new JsonObject();
        object.addProperty("ceAgentID",id);

        try {
          HttpResponse<String> stringHttpResponse =  Unirest.post("http://localhost:6000/getNodeList").body(object.toString()).asString();
            return stringHttpResponse.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <h1>list</h1>
     * @param id String
     * @param name String
     * @return String
     * the HTTP response
     */
    public String list(String id,String name)
    {
        JsonObject object = new JsonObject();
        object.addProperty("ceAgentID",id);
        object.addProperty("name",name);

        try {
            HttpResponse<String> stringHttpResponse =  Unirest.post("http://localhost:6000/getNodeList").body(object.toString()).asString();
            return stringHttpResponse.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <h1>createNode</h1>
     * Creates a new node for the cluster
     * @param id String
     * @param flavor String
     * @return String
     * the response is returned.
     */
    public String createNode(String id,String flavor)
    {
        JsonObject object = new JsonObject();
        object.addProperty("ceAgentID",id);

        try {
           HttpResponse<String> response = Unirest.post("http://localhost:6000/requestNode").body(object.toString()).asString();
           return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <h1>deleteNode</h1>
     * Deletes given node from a cluster.
     * @param id String
     * @param nodeId String
     */
    public void deleteNode(String id,String nodeId)
    {
        JsonObject object = new JsonObject();
        object.addProperty("ceAgentID",id);
        object.addProperty("nodeId",nodeId);

        try {
            HttpResponse<String> response = Unirest.post("http://localhost:6000/releaseNode").body(object.toString()).asString();

        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }
}
