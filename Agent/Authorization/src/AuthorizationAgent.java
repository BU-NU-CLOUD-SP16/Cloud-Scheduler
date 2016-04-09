import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 4/5/16.
 */
public class AuthorizationAgent {


    private  OverlordCommunicator overlordCommunicator;
    private boolean waitingForResponse = false;

    public AuthorizationAgent(String ip, int port, ClusterState state) {
        overlordCommunicator = new OverlordCommunicator(ip,port);
        overlordCommunicator.register(state);
    }

    public boolean isWaitingForResponse() {
        return waitingForResponse;
    }

    public void setWaitingForResponse(boolean waitingForResponse) {
        this.waitingForResponse = waitingForResponse;
    }

    public boolean canCreateNewNodes(int numberOfNodes)
    {

        String status = overlordCommunicator.createNode(numberOfNodes);

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

    public void deletedNode(Node node)
    {
//        overlordCommunicator.deleteNode();
    }


}
