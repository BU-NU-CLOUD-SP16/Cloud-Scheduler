/**
 * <h1>ListCommand</h1>
 * Getters and Setters for the List
 * Command in the OpenStack.
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-04-10
 */
public class ListCommand extends OpenStackCommand {

    private String network;
    private String name;

    /**
     * <h1>ListCommand</h1>
     * Constructor
     * @param network String
     */
    public ListCommand(String network) {
        this.network = network;
    }

    /**
     * <h1>getNetwork</h1>
     * @return String
     * the Network in which the current
     * node resides.
     */
    public String getNetwork() {
        return network;
    }

    /**
     * <h1>setNetwork</h1>
     * @param network String
     */
    public void setNetwork(String network) {
        this.network = network;
    }

    /**
     * <h1>getName</h1>
     * @return String
     * the Name of the Node.
     */
    public String getName() {
        return name;
    }

    /**
     * <h1>setName</h1>
     * @param name String
     */
    public void setName(String name) {
        this.name = name;
    }
}
