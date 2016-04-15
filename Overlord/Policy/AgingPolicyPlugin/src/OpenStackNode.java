/**
 * Created by chemistry_sourabh on 3/28/16.
 */
public class OpenStackNode extends Node {

    private String id;
    private String name;
    private String status;
    private String flavor;
    private String ip;

    public OpenStackNode() {
    }

    public OpenStackNode(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
