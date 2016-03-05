/**
 * Created by Praveen on 3/2/2016.
 */
public interface ClusterElasticityAgentFramework extends Runnable {

    public void notifyTimerExpiry(ClusterElasticityAgentCommand workerCommand) throws ClusterElasticityAgentException;

}
