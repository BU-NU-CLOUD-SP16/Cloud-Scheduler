/**
 * Created by chemistry_sourabh on 3/4/16.
 */
public class OpenStackClusterScalerPlugin implements ClusterScalerPlugin {

    public Node createNewNode(Node node)
    {
        System.out.println("Created new node");
        OpenStackNode newNode = new OpenStackNode();
        newNode.setIp("192.168.0.10");
        return newNode;
    }

    public boolean deleteNode(Node node)
    {
        System.out.println("Deleted Node");
        return true;
    }

}
