import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public class ClusterElasticityManager implements ClusterElasticityManagerFramework {

    // The poll interval which is an int that tells the frequency at which the polling should take place
    // The Plugin's class name which is a string
    private int pollInterval;
    private String elasticityPluginClassName;
    private String clusterScalerPluginClassName;
    private String databaseExecutorPluginClassName;

    private ElasticityPlugin elasticityPlugin;
    private ClusterScalerPlugin scalerPlugin;

    // Instance to database API
    private DBExecutor database;

    private LinkedBlockingQueue<ClusterElasticityAgentCommand> workerQueue;

    public ClusterElasticityManager(CommandLineArguments arguments) {
        this.pollInterval = arguments.getPollInterval();
        this.elasticityPluginClassName = arguments.getCemanagerPluginMainClass();
        this.clusterScalerPluginClassName = arguments.getClusterScalerPluginMainClass();
        this.databaseExecutorPluginClassName = arguments.getDbExecutorPluginMainClass();
        workerQueue = new LinkedBlockingQueue<>();
    }


    @Override
    public void run() {
        System.out.println("Cluster Elasticity Manager Started");
        try
        {
            Class elasticityPluginClass = Class.forName(elasticityPluginClassName);
            Class clusterScalerPluginClass = Class.forName(clusterScalerPluginClassName);
            Class databaseExecutorPluginClass = Class.forName(databaseExecutorPluginClassName);
            elasticityPlugin = (ElasticityPlugin) elasticityPluginClass.getConstructors()[0].newInstance(null);
            scalerPlugin = (ClusterScalerPlugin) clusterScalerPluginClass.getConstructors()[0].newInstance(null);
            database = (DBExecutor) databaseExecutorPluginClass.getConstructors()[0].newInstance(null);
        }

        catch(ClassNotFoundException ex)
        {
            System.err.println("Class "+ elasticityPluginClassName +" not found");
        }

        catch(Exception ex)
        {
            System.err.println(ex);
        }

        System.out.println("Timer Started");
        new Timer().scheduleAtFixedRate(new ClusterElasticityAgentTimerTask(this, new ScaleClusterElasticityAgentCommand(elasticityPlugin,scalerPlugin,database)),0,pollInterval);

        while(true)
        {
            try {
                ClusterElasticityAgentCommand command = workerQueue.take();
                command.execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void notifyResourceScaling(String parameters) throws ClusterElasticityAgentException {
        try {
            workerQueue.put(new RequestResourcesClusterElasticityAgentCommand(elasticityPlugin,scalerPlugin,parameters));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClusterElasticityAgentException("Failed to Queue HTTP Request for scaling resource!!");
        }
    }

    @Override
    public void notifyTimerExpiry(ClusterElasticityAgentCommand workerCommand) throws ClusterElasticityAgentException {
        try {
            workerQueue.put(workerCommand);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClusterElasticityAgentException("Failed to Queue HTTP Request for scaling resource!!");
        }
    }
}
