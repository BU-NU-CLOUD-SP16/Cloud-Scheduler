import com.google.gson.Gson;
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

    private String username;
    private String password;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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
