import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Praveen on 3/26/2016.
 */
public class CEAgentInfo {

    private Integer ceAgentID;
    private Integer ceAgentPriority;
    private String ceAgentIP;
    private Integer ceAgentPort;
    private Integer minFixedNodes;
    private Integer maxScaledNodes;
    private ArrayList<String> nodeList;

    public CEAgentInfo(){
        this.nodeList = new ArrayList<>();

    }

    public Integer getCeAgentID() {
        return ceAgentID;
    }

    public void setCeAgentID(Integer ceAgentID) {
        this.ceAgentID = ceAgentID;
    }

    public Integer getCeAgentPriority() {
        return ceAgentPriority;
    }

    public void setCeAgentPriority(Integer ceAgentPriority) {
        this.ceAgentPriority = ceAgentPriority;
    }

    public String getCeAgentIP() {
        return ceAgentIP;
    }

    public void setCeAgentIP(String ceAgentIP) {
        this.ceAgentIP = ceAgentIP;
    }

    public Integer getCeAgentPort() {
        return ceAgentPort;
    }

    public void setCeAgentPort(Integer ceAgentPort) {
        this.ceAgentPort = ceAgentPort;
    }

    public Integer getMinFixedNodes() {
        return minFixedNodes;
    }

    public void setMinFixedNodes(Integer minFixedNodes) {
        this.minFixedNodes = minFixedNodes;
    }

    public Integer getMaxScaledNodes() {
        return maxScaledNodes;
    }

    public void setMaxScaledNodes(Integer maxScaledNodes) {
        this.maxScaledNodes = maxScaledNodes;
    }

    public ArrayList<String> getNodeList() {
        return nodeList;
    }

    public void setNodeList(ArrayList<String> nodeList) {
        System.out.println("Node List size" + nodeList.size());
        this.nodeList = nodeList;
    }

    public void addNodeToList(String serverID){
        this.nodeList.add(serverID);
    }

    public void removeNodeFromList(String serverID){
        this.nodeList.remove(serverID);
    }
}
