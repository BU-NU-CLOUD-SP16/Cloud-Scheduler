/**
 * Created by chemistry_sourabh on 4/10/16.
 */
public class ListCommand extends OpenStackCommand {

    private String network;
    private String name;

    public ListCommand(String network) {
        this.network = network;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
