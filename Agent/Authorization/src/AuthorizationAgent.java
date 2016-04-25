import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 4/5/16.
 */
public class AuthorizationAgent {


    private  OverlordCommunicator overlordCommunicator;
    private boolean waitingForResponse = false;

    public AuthorizationAgent(String ip, int port, String stateJson) {
        overlordCommunicator = new OverlordCommunicator(ip,port);
        overlordCommunicator.register(stateJson);
    }

    public boolean isWaitingForResponse() {
        return waitingForResponse;
    }

    public void setWaitingForResponse(boolean waitingForResponse) {
        this.waitingForResponse = waitingForResponse;
    }

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
