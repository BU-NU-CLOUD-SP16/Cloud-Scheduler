import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Kovit on 3/8/2016.
 */

// TODO: implementing http framework and endpoints
// TODO: implementing cache
// TODO: self code review
// TODO : testing - top priority
// TODO: exception handling
// TODO: logging
// TODO: java doc
// TODO: package structure
// TODO: checking if method @DataQuery tag
// TODO: support bulk insert
// TODO: constants

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
                        getCPluginClsIntances(arguments.getCollectorPluginMainClass()), getMasterAddr())), 0, arguments.getPollInterval());

        while(true) {
            try {
                ClusterElasticityAgentCommand command = workerQueue.take();
                command.execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String getMasterAddr() {
        return this.arguments.getMesosMasterIP() + ":" + this.arguments.getMesosMasterPort();
    }

    private DBExecutor getDBExecInstance(String dbExecutorPluginMainClass) {
        try {
            Class databaseExecutorPluginClass = Class.forName(arguments.getDbExecutorPluginMainClass());
            return (DBExecutor) databaseExecutorPluginClass.getConstructor().newInstance();
        }
        catch(ClassNotFoundException ex) {
            System.err.println("Class" +  " not found");
        }
        catch(Exception ex) {
            System.err.println(ex);
        }

        // TODO remove this throw new exception
        return null;
    }

    private List<Object> getCPluginClsIntances(String collectorPluginMainClass) {
        String[] classNamesLst = collectorPluginMainClass.split(",");
        List<Object> classInstance = new ArrayList<>(classNamesLst.length);

        try {
            for (String className : classNamesLst) {
                classInstance.add(Class.forName(className).getConstructor().newInstance());
            }
        }
        catch(ClassNotFoundException ex) {
            System.err.println("Class "+ " not found");
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