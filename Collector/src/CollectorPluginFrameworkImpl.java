import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;

/**
 * Created by Kovit on 3/8/2016.
 */


// TODO: self code review
// TODO : testing
// -----------------------------------------------------------------
// TODO: java doc
// TODO: package structure
// TODO: constants
// TODO: checking if method @DataQuery tag
// TODO: support bulk insert
// TODO: implementing cache

public final class CollectorPluginFrameworkImpl implements ClusterElasticityAgentFramework {

    private final LinkedBlockingQueue<ClusterElasticityAgentCommand> workerQueue;
    private final CommandLineArguments arguments;
    public final static String COLLECTOR_LOGGER_NAME = "CollectorFramework";
    private final static Logger LOGGER = Logger.getLogger(COLLECTOR_LOGGER_NAME);
    private final String masterIpAddr;

    public CollectorPluginFrameworkImpl(CommandLineArguments argumentList) {
        workerQueue = new LinkedBlockingQueue<ClusterElasticityAgentCommand>();
        arguments = argumentList;
        masterIpAddr = getMasterAddr();
        logSetup();
    }

    @Override
    public void run() {
        new Timer().scheduleAtFixedRate(new ClusterElasticityAgentTimerTask(this,
                new CollectorFramewrkCmd(getDBExecInstance(arguments.getDbExecutorPluginMainClass()),
                        getCPluginClsIntances(arguments.getCollectorPluginMainClass()), masterIpAddr)), 0, arguments.getPollInterval());

        while(true) {
            try {
                ClusterElasticityAgentCommand command = workerQueue.take();
                LOGGER.log(Level.FINE, "[Collector Framework] Executing collector plugin command");
                command.execute();
            } catch (Exception e) {
                String errorMsg = "[Collector Framework] Failed to execute collector plugin command. Reason: " +
                        e.getMessage() + ". Cause:" + e.getCause();
                LOGGER.log(Level.SEVERE, errorMsg);
                throw new IllegalStateException(errorMsg, e);
            }
        }
    }

    private String getMasterAddr() {
        String masterIpAddr = this.arguments.getMesosMasterIP() + ":" + this.arguments.getMesosMasterPort();
        LOGGER.log(Level.FINE, "[Collector Framework] Master Ip Address" );
        return masterIpAddr;
    }

    private DBExecutor getDBExecInstance(String dbExecutorPluginMainClass) {
        try {
            Class databaseExecutorPluginClass = Class.forName(arguments.getDbExecutorPluginMainClass());
            return (DBExecutor) databaseExecutorPluginClass.getConstructor().newInstance();
        }
        catch(Exception ex) {
            String errorMsg = "[Collector Framework] Failed to initialize db executor instance " + ex.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new IllegalStateException(errorMsg, ex);
        }
    }

    private List<Object> getCPluginClsIntances(String collectorPluginMainClass) {
        String[] classNamesLst = collectorPluginMainClass.split(",");
        List<Object> classInstance = new ArrayList<>(classNamesLst.length);
        try {
            for (String className : classNamesLst) {
                classInstance.add(Class.forName(className).getConstructor().newInstance());
                LOGGER.log(Level.FINE, "Instance of class " + className + " created successfully");
            }
        }
        catch(Exception ex) {
            String errorMsg = "[Collector Framework] Failed to create instances of collector plugin classes " + ex.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new IllegalStateException(errorMsg, ex);
        }
        return classInstance;
    }

    @Override
    public void notifyTimerExpiry(ClusterElasticityAgentCommand workerCommand) throws ClusterElasticityAgentException {
        try {
            LOGGER.log(Level.FINE, "[Collector Framework] notifyTimerExpiry");
            workerQueue.put(workerCommand);
        } catch (Exception e) {
            String errorMsg = "[Collector Framework] Failed at notifyTimerExpiry method. Reason: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new IllegalStateException(errorMsg, e);
        }
    }

    private void logSetup() {
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(COLLECTOR_LOGGER_NAME);
        logger.setLevel(Level.FINE);
        FileHandler logFileHandler = null;
        try {
            logFileHandler = new FileHandler(arguments.getLogDir() + File.separator + "Collector_Plugin.log");
            SimpleFormatter formatterTxt = new SimpleFormatter();
            logFileHandler.setFormatter(formatterTxt);
            logger.addHandler(logFileHandler);
        }
        catch (Exception e) {
            System.err.print("[Collector Framework] Could not create log file at " + arguments.getLogDir());
        }
    }
}