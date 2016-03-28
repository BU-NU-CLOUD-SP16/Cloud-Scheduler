import java.util.ArrayList;

/**
 * Created by Praveen on 3/26/2016.
 */
public class Agent {

    private Integer id;
    private Integer priority;
    private String ip;
    private Integer port;
    private Integer minFixedNodes;
    private Integer maxScaledNodes;
    private ArrayList<Node> nodeList;

    public Agent(){
        this.nodeList = new ArrayList<>();

    }

    public Agent(Integer id) {
        this.id = id;
        this.nodeList = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
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

    public ArrayList<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(ArrayList<Node> nodeList) {
        System.out.println("Node List size" + nodeList.size());
        this.nodeList = nodeList;
    }

    public void addNodeToList(Node node){
        this.nodeList.add(node);
    }

    public void removeNodeFromList(Node node){
        this.nodeList.remove(node);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Agent agent = (Agent) o;

        return id != null ? id.equals(agent.id) : agent.id == null;

    }
}
