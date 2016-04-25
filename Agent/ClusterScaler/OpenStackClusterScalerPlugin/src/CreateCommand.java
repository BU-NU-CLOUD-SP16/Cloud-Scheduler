/**
 * <h1>CreateCommand</h1>
 * Getter and Setters for Creating a node
 * in Openstack
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-04-10
 */
public class CreateCommand extends OpenStackCommand {

    private  String name;
    private String imageName;
    private String network;
    private String keyPair;
    private String securityGroup;
    private String flavor;


    /**
     * <h1>getName</h1>
     * @return String
     * The name of the new node.
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

    /**
     * <h1>getImageName</h1>
     * @return String
     * The Image with which new node will be created.
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * <h1>setImageName</h1>
     * @param imageName String
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * <h1>getNetwork</h1>
     * @return String
     * The Network the new node will belong to.
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
     * <h1>getKeyPair</h1>
     * @return String
     * The keypair the new node
     * belongs to.
     */
    public String getKeyPair() {
        return keyPair;
    }

    /**
     * <h1>setKeyPair</h1>
     * @param keyPair String
     */
    public void setKeyPair(String keyPair) {
        this.keyPair = keyPair;
    }

    /**
     * <h1>getSecurityGroup</h1>
     * @return String
     * Which Security group the new node should
     * belong to.
     */
    public String getSecurityGroup() {
        return securityGroup;
    }

    /**
     * <h1>setSecurityGroup</h1>
     * @param securityGroup String
     */
    public void setSecurityGroup(String securityGroup) {
        this.securityGroup = securityGroup;
    }

    /**
     * <h1>getFlavor</h1>
     * @return String
     * The flavor the node should be.
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
}
