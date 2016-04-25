import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * <h1>AuthorizationAgent</h1>
 * Based on the data received, makes a
 * decision if the agent can get a new node
 * from OpenStack.
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-04-16
 */

public class AuthorizationAgent {


    private  OverlordCommunicator overlordCommunicator;
    private boolean waitingForResponse = false;

    /**
     * <h1>AuthorizationAgent</h1>
     * Constructor
     * @param ip String
     * @param port int
     * @param state ClusterState
     */
    public AuthorizationAgent(String ip, int port, String stateJson) {
        overlordCommunicator = new OverlordCommunicator(ip,port);
        overlordCommunicator.register(stateJson);
    }

    /**
     * <h1>isWaitingForResponse</h1>
     * @return Boolean
     * True if the Agent is waiting for response.
     */
    public boolean isWaitingForResponse() {
        return waitingForResponse;
    }

    /**
     * <h1>setWaitingForResponse</h1>
     * @param waitingForResponse Boolean
     */
    public void setWaitingForResponse(boolean waitingForResponse) {
        this.waitingForResponse = waitingForResponse;
    }

    /**
     * <h1>canCreateNewNodes</h1>
     * @param numberOfNodes int
     * @return Boolean
     * True iff the numberofNodes is greater
     * than zero and the overlordCommunicator
     * gets a 200 http response
     */
    public boolean canCreateNewNodes(String id,int numberOfNodes)
    {
        if (numberOfNodes == 0)
            return  false;

        if(waitingForResponse)
            return false;


        JsonObject object = new JsonObject();
        object.addProperty("ceAgentId",id);
        object.addProperty("numberOfNodes",numberOfNodes);

        String status = overlordCommunicator.createNode(object.toString());

        switch(status)
        {
            case "200":
                waitingForResponse = false;
                return true;
            case "201":
                waitingForResponse = true;
                return false;
            default:
                return false;
        }
    }

}
