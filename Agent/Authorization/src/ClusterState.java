import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * <h1>ClusterState</h1>
 * Contains the details of the
 * current Cluster.
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-04-16
 */
public class ClusterState {

    private String id;
    private double priority;
    private int minNodes;
    private String port;
    private ArrayList<Node> nodesInCluster;


    /**
     * <h1>getId</h1>
     * @return String
     * The id associated with the Cluster.
     */
    public String getId() {
        return id;
    }

    /**
     * <h1>setId</h1>
     * @param id String
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * <h1>getPriority</h1>
     * @return double
     * the current priority of the cluster.
     */
    public double getPriority() {
        return priority;
    }

    /**
     * <h1>setPriority</h1>
     * @param priority double
     */
    public void setPriority(double priority) {
        this.priority = priority;
    }

    /**
     * <h1>setMinNodes</h1>
     * @return int the minimum nodes
     * currently used in the cluster.
     */
    public int getMinNodes() {
        return minNodes;
    }

    /**
     * <h1>setMinNodes</h1>
     * @param minNodes
     */
    public void setMinNodes(int minNodes) {
        this.minNodes = minNodes;
    }

    /**
     * <h1>getPort</h1>
     * @return String
     * the port name.
     */
    public String getPort() {
        return port;
    }

    /**
     * <h1>setPort</h1>
     * @param port String
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * <h1>getNodesInCluster</h1>
     * @return ArrayList<Node>
     *     the total number of nodes
     *     used in the cluster.
     */
    public ArrayList<Node> getNodesInCluster() {
        return nodesInCluster;
    }

    /**
     * <h1>setNodesInCluster</h1>
     * @param nodesInCluster ArrayList<Node>
     */
    public void setNodesInCluster(ArrayList<Node> nodesInCluster) {
        this.nodesInCluster = nodesInCluster;
    }

    /**
     * <h1>toString</h1>
     * @return String
     * All the parameters as a String.
     */
    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id",id);
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
