import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.ArrayList;
import java.util.logging.Level;

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
    public void register(String stateJson)
    {
        try {
            Unirest.post("http://"+overlordIp+":"+overlordPort+"/registerCEAgent").body(stateJson).asString();
        } catch (UnirestException e) {
            GlobalLogger.globalLogger.log(Level.SEVERE,e.getMessage(),GlobalLogger.MANAGER_LOG_ID);
        }
    }

    /**
     * <h1>createNode</h1>
     * Creates a node based on Overlords response.
     * @param number
     * @return String
     */
    public String createNode(String json)
    {

        try {
           HttpResponse<String> response = Unirest.post("http://"+overlordIp+":"+overlordPort+"/requestNode").body(json).asString();
           return ""+response.getBody();
        } catch (UnirestException e) {
            GlobalLogger.globalLogger.log(Level.SEVERE,e.getMessage(),GlobalLogger.MANAGER_LOG_ID);
        }
        return null;
    }
}
