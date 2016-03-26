/**
 * Created by chemistry_sourabh on 3/9/16.
 */
public class MesosSlave {

    private String nodeId;
    private String hostname;
    private String flavor;
    private String ip;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "MesosSlave{" +
                "nodeId='" + nodeId + '\'' +
                ", hostname='" + hostname + '\'' +
                ", flavor='" + flavor + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
