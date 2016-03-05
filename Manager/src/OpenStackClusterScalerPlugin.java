import interfaces.ClusterScalerPlugin;

/**
 * Created by chemistry_sourabh on 3/4/16.
 */
public class OpenStackClusterScalerPlugin implements ClusterScalerPlugin {

    public boolean createNewNode()
    {
        return true;
    }

    public boolean deleteNode(String id)
    {
        return true;
    }

}
