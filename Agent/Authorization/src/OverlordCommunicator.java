import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.ArrayList;
import java.util.logging.Level;

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

    public void register(String stateJson)
    {

        try {
            Unirest.post("http://"+overlordIp+":"+overlordPort+"/registerCEAgent").body(stateJson.toString()).asString();
        } catch (UnirestException e) {
            GlobalLogger.globalLogger.log(Level.SEVERE,e.getMessage(),GlobalLogger.MANAGER_LOG_ID);
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
            GlobalLogger.globalLogger.log(Level.SEVERE,e.getMessage(),GlobalLogger.MANAGER_LOG_ID);
        }
        return null;
    }
}
