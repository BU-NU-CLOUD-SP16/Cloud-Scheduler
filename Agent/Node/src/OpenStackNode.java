/**
 * Created by chemistry_sourabh on 3/7/16.
 */
public class OpenStackNode extends Node {

    private String hostname;
    private String flavor;
    private String id;
    private String ip;

    public OpenStackNode(String flavor) {
        this.flavor = flavor;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
