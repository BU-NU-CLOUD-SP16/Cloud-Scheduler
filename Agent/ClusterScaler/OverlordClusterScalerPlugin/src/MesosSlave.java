
/**
 * <h1>MesosSlave</h1>
 * Getter Setters for MesosSlave for Overlord.
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-03-19
 */
public class MesosSlave {

    private String nodeId;
    private String hostname;
    private String flavor;
    private String ip;

    /**
     * <h1>getNodeId</h1>
     * @return String
     * The id of the current Mesos Slave Node.
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * <h1>setNodeId</h1>
     * @param nodeId String
     */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * <h1>getHostname</h1>
     * @return String
     * The hostname of the current mesos Slave.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * <h1>setHostname</h1>
     * @param hostname String
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * <h1>getFlavor</h1>
     * @return String
     * The flavor of the current Mesos slave Node.
     */
    public String getFlavor() {
        return flavor;
    }

    /**
     * <h1>setFlavor</h1>
     * @param flavor String
     */
    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    /**
     * <h1>getIp</h1>
     * @return String
     * The IP address of the MesosSlave.
     */
    public String getIp() {
        return ip;
    }

    /**
     * <h1>setIp</h1>
     * @param ip String
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * <h1>toString</h1>
     * @return String
     * Members of the class as String.
     */
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
