/**
 * Created by Praveen on 3/26/2016.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.concurrent.LinkedBlockingQueue;

import static spark.Spark.*;

public class HttpEndPoints {

    private static final int HTTP_SUCCESS_RESPONSE = 200;
    private static final int HTTP_CREATE_CODE = 201;
    private static final int HTTP_CLIENT_ERROR = 400;
    private static final int HTTP_SERVER_ERROR = 500;
    private static final int SUCCESS = 1;

    private Overlord overlordHandle;

    public LinkedBlockingQueue<String> getResponseQueue() {
        return responseQueue;
    }

    public void setResponseQueue(LinkedBlockingQueue<String> responseQueue) {
        this.responseQueue = responseQueue;
    }

    private LinkedBlockingQueue<String> responseQueue;

    public HttpEndPoints(Overlord overlordHandle) {
        this.overlordHandle = overlordHandle;
        this.responseQueue = new LinkedBlockingQueue<>();
    }

    public void configureHttpEndPoints(){

        post("/registerCEAgent", (request, response) -> {
            response.status(registerCEAgent(request.body(), request.ip(), request.port()));
            return SUCCESS;
        });

        post("/requestNode", (request, response) -> {
            response.body( requestNode( request.body() ) );
            response.type("application/json");

            if(response.body() == "")
                response.status(HTTP_SERVER_ERROR);
            else
                response.status(HTTP_CREATE_CODE);

            return response.body();
        });

        post("/releaseNode", (request, response) -> {
            releaseNode( request.body() );
            response.status(HTTP_SUCCESS_RESPONSE);
            return SUCCESS;
        });

        post("/getNodeList", (request, response) -> {
           response.body( getNodeList( request.body() ) );
            response.status(HTTP_SUCCESS_RESPONSE);
            response.type("application/json");
            return response.body();
        });
    }


    private int registerCEAgent(String jsonString, String ceAgentIP, int ceAgentPort) {

        Gson gson = new Gson();
        Integer ceAgentID = gson.fromJson(jsonString, JSONRegisterCEAgent.class).getCeAgentID();

        if(overlordHandle.getPolicyConfigHandle().isPolicyConfigured(ceAgentID) == false){
            return HTTP_CLIENT_ERROR;
        }

        CEAgentInfo ceAgent = overlordHandle.getRegisteredCEAgents().AddNewCEAgent(ceAgentID);
        ceAgent.setCeAgentID(ceAgentID);
        ceAgent.setCeAgentPriority(overlordHandle.getPolicyConfigHandle().getClusterPriority(ceAgentID));
        ceAgent.setCeAgentIP(ceAgentIP);
        ceAgent.setCeAgentPort(ceAgentPort);

        return HTTP_SUCCESS_RESPONSE;
    }

    private String requestNode(String jsonString) {

        Gson gson = new Gson();
        Integer ceAgentID = gson.fromJson(jsonString, JSONRequestNode.class).getCeAgentID();

        if( overlordHandle.getRegisteredCEAgents().isRegisteredCEAgent(ceAgentID)) {
            int slaveCount = overlordHandle.getRegisteredCEAgents().getCEAgentDetails(ceAgentID).getNodeList().size()
                    + 1;
            String serverID = null;
            try {
                OpenStackCommand command = new OpenStackCommand();
                command.setCommand("create");
                command.setNodeName("Spark-Slave-" + ceAgentID + "-" +
                        slaveCount + "" +
                        ".cloud");
                overlordHandle.getOpenStackHandle().getWorkerQueue().put( command );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                serverID = overlordHandle.getHttpHandle().getResponseQueue().take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            overlordHandle.getRegisteredCEAgents().getCEAgentDetails(ceAgentID).addNodeToList(serverID);
            return "{ \"serverID\": \"" + serverID + "\" }";
        }
        else
            return "";
    }

    private void releaseNode(String jsonString) {

        Gson gson = new Gson();
        JSONReleaseNode releaseNodeDetails = gson.fromJson(jsonString, JSONReleaseNode.class);

        if( overlordHandle.getRegisteredCEAgents().isRegisteredCEAgent(releaseNodeDetails.getCeAgentID())) {

            OpenStackCommand command = new OpenStackCommand();
            command.setCommand("delete");
            command.setNodeName(releaseNodeDetails.getServerID());

            try {
                overlordHandle.getOpenStackHandle().getWorkerQueue().put(command);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            overlordHandle.getRegisteredCEAgents().getCEAgentDetails(releaseNodeDetails.getCeAgentID()).removeNodeFromList
                    (releaseNodeDetails.getServerID());
        }

    }

    private String getNodeList(String jsonString) {

        Gson gson = new Gson();
        Integer ceAgentID = gson.fromJson(jsonString, JSONRequestNode.class).getCeAgentID();

        String response = "{ [";
        if( overlordHandle.getRegisteredCEAgents().isRegisteredCEAgent(ceAgentID)) {

            for(String serverID: overlordHandle.getRegisteredCEAgents().getCEAgentDetails(ceAgentID).getNodeList()){
                response += "\"ServerID\": \"" + serverID + "\",";
            }

            if(response != "{ [")
                response = response.substring(0, response.length()-1);
        }

        response += "] }";
        return response;
    }

}
