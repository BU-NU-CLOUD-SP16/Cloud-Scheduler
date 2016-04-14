import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 4/9/16.
 */
public class ClusterState {

    public static final int ACTIVE_STATUS = 0;
    public static final  int CREATE_STATUS = 1;
    public static final int DELETE_STATUS = 2;

    private String id;
    private double priority;
    private int minNodes;
    private String port;
    private ArrayList<Node> nodesInCluster;
    private int status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public int getMinNodes() {
        return minNodes;
    }

    public void setMinNodes(int minNodes) {
        this.minNodes = minNodes;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public ArrayList<Node> getNodesInCluster() {
        return nodesInCluster;
    }

    public void setNodesInCluster(ArrayList<Node> nodesInCluster) {
        this.nodesInCluster = nodesInCluster;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id",id);
        jsonObject.addProperty("status",status);
        jsonObject.addProperty("priority",priority);
        jsonObject.addProperty("port",port);
        jsonObject.addProperty("minNodes",minNodes);

        JsonArray array = new JsonArray();

        for( Node node : nodesInCluster)
        {
            OpenStackNode node1 = (OpenStackNode) node;
            array.add(node1.getHostname());
        }
        jsonObject.add("nodes",array);

        return  jsonObject.toString();
    }
}
