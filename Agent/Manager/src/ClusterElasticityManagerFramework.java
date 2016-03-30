/**
 * Created by Praveen on 3/2/2016.
 */

public interface ClusterElasticityManagerFramework extends ClusterElasticityAgentFramework {

    public void notifyResourceScaling(String parameters) throws ClusterElasticityAgentException;
    public void notifyReleaseNodeRequest(String string);
}
