
/**
 * <h1>MesosSlave</h1>
 * Getter Setters for MesosSlave for Overlord.
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-03-19
 */
public class OpenStackNode extends Node {

    private String hostname;
    private String flavor;
    private String id;
    private String ip;
    private String status;

    public OpenStackNode() {
    }

    public OpenStackNode(String flavor) {
        this.flavor = flavor;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
