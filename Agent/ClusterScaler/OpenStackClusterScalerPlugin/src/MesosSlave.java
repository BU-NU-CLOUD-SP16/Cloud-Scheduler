/**
 * <h1>MesosSlave</h1>
 * Getters and Setters for Mesos Slaves.
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-03-09
 */
public class MesosSlave {

    private String nodeId;
    private String hostname;
    private String flavor;
    private String ip;

    /**
     * <h1>getNodeId</h1>
     * @return String
     * The node Id of the Slave Node.
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
     * The hostname of the slave node.
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
     * The flavor in which slave node uses.
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
     * The ip Addresss of the Slave.
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
     * All the Class members as a String.
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
