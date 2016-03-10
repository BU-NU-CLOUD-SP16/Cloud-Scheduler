import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Kovit on 3/8/2016.
 */

// TODO: self code review
// TODO : testing - top priority
// TODO: exception handling
// TODO: logging
// TODO: checking if method @DataQuery tag
// TODO: java doc
// TODO: support bulk insert

public final class CollectorPluginFrameworkImpl implements ClusterElasticityAgentFramework {

    private final LinkedBlockingQueue<ClusterElasticityAgentCommand> workerQueue;
    private final CommandLineArguments arguments;

    public CollectorPluginFrameworkImpl(CommandLineArguments argumentList) {
        workerQueue = new LinkedBlockingQueue<ClusterElasticityAgentCommand>();
        arguments = argumentList;
    }

    @Override
    public void run() {
        new Timer().scheduleAtFixedRate(new ClusterElasticityAgentTimerTask(this,
                new CollectorFramewrkCmd(getDBExecInstance(arguments.getDbExecutorPluginMainClass()),
                        getCPluginClsIntances(arguments.getCollectorPluginMainClass()))), 0, arguments.getPollInterval());

        while(true) {
            try {
                ClusterElasticityAgentCommand command = workerQueue.take();
                command.execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private DBExecutor getDBExecInstance(String dbExecutorPluginMainClass) {
        try {
            Class databaseExecutorPluginClass = Class.forName(arguments.getDbExecutorPluginMainClass());
            return (DBExecutor) databaseExecutorPluginClass.getConstructor().newInstance();
        }
        catch(ClassNotFoundException ex) {
            System.err.println("Class "+ elasticityPluginClassName +" not found");
        }
        catch(Exception ex) {
            System.err.println(ex);
        }
    }

    private List<ICollectorPlugin> getCPluginClsIntances(String collectorPluginMainClass) {
        String[] classNamesLst = collectorPluginMainClass.split(",");
        List<ICollectorPlugin> classInstance = new ArrayList<>(classNamesLst.length);

        try {
            for (String className : classNamesLst) {
                classInstance.add((ICollectorPlugin) Class.forName(className).getConstructor().newInstance());
            }
        }
        catch(ClassNotFoundException ex) {
            System.err.println("Class "+ elasticityPluginClassName +" not found");
        }
        catch(Exception ex) {
            System.err.println(ex);
        }

        return classInstance;

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

