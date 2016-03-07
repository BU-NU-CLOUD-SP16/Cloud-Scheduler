/**
 * Created by chemistry_sourabh on 3/4/16.
 */
public class OpenStackClusterScalerPlugin implements ClusterScalerPlugin {

    public boolean createNewNode()
    {
        System.out.println("Created new node");
        return true;
    }

    public boolean deleteNode(String id)
    {
        System.out.println("Deleted Node");
        return true;
    }

}
