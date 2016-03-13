import java.io.File;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;

/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public class ClusterElasticityManager implements ClusterElasticityManagerFramework {

    private static final String MANAGER_LOGGER = "Cluster Elasticity Manager";

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

    private File logDir;
    private Logger logger;
    public ClusterElasticityManager(CommandLineArguments arguments) {
        this.pollInterval = arguments.getPollInterval();
        this.elasticityPluginClassName = arguments.getCemanagerPluginMainClass();
        this.clusterScalerPluginClassName = arguments.getClusterScalerPluginMainClass();
        this.databaseExecutorPluginClassName = arguments.getDbExecutorPluginMainClass();
        this.logDir = new File(arguments.getLogDir());
        workerQueue = new LinkedBlockingQueue<>();

        logger = logSetup();
    }


    @Override
    public void run() {
        logger.info("Started Cluster Elasticity Manager");
        try
        {
            Class elasticityPluginClass = Class.forName(elasticityPluginClassName);
            logger.fine("Got Class of Elasticity Plugin = "+elasticityPluginClassName);

            Class clusterScalerPluginClass = Class.forName(clusterScalerPluginClassName);
            logger.fine("Got Class of Cluster Scaler Plugin = "+clusterScalerPluginClassName);

            Class databaseExecutorPluginClass = Class.forName(databaseExecutorPluginClassName);
            logger.fine("Got Class of Database Executor Plugin = "+databaseExecutorPluginClassName);

            elasticityPlugin = (ElasticityPlugin) elasticityPluginClass.getConstructors()[0].newInstance(null);
            logger.fine("Created Instance of "+elasticityPluginClassName);
            scalerPlugin = (ClusterScalerPlugin) clusterScalerPluginClass.getConstructors()[0].newInstance(null);
            logger.fine("Created Instance of "+clusterScalerPluginClassName);
            database = (DBExecutor) databaseExecutorPluginClass.getConstructors()[1].newInstance(null);
            logger.fine("Created Instance of "+databaseExecutorPluginClassName);
        }

        catch(ClassNotFoundException ex)
        {
            logger.severe(ex.toString());
        }

        catch(Exception ex)
        {
            logger.severe(ex.toString());
        }


        new Timer().scheduleAtFixedRate(new ClusterElasticityAgentTimerTask(this, new ScaleClusterElasticityAgentCommand(elasticityPlugin,scalerPlugin,database)),0,pollInterval);
        logger.info("Scale Commands Scheduled to be added to queue every "+pollInterval+"ms");

        while(true)
        {
            try {
                ClusterElasticityAgentCommand command = workerQueue.take();
                logger.fine("Got a "+command.getClass()+" "+command.toString());
                command.execute();
                logger.fine("Executed command "+command.toString());
            } catch (InterruptedException e) {
                logger.severe(e.toString());
            }
        }
    }


    @Override
    public void notifyResourceScaling(String parameters) throws ClusterElasticityAgentException {
        try {
            workerQueue.put(new RequestResourcesClusterElasticityAgentCommand(elasticityPlugin,scalerPlugin,parameters));
            logger.fine("Adding RequestResourcesClusterElasticityAgentCommand to worker queue with parameters "+parameters);
        } catch (InterruptedException e) {
            logger.severe(e.toString());
        } catch (Exception e) {
            logger.severe(e.toString());
            throw new ClusterElasticityAgentException("Failed to Queue HTTP Request for scaling resource!!");
        }
    }

    @Override
    public void notifyTimerExpiry(ClusterElasticityAgentCommand workerCommand) throws ClusterElasticityAgentException {
        try {
            workerQueue.put(workerCommand);
            logger.fine("Adding "+workerCommand.getClass()+" to worker queue");
        } catch (InterruptedException e) {
            logger.severe(e.toString());
        } catch (Exception e) {
            logger.severe(e.toString());
            throw new ClusterElasticityAgentException("Failed to Queue HTTP Request for scaling resource!!");
        }
    }

    private Logger logSetup() {
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(MANAGER_LOGGER);
        logger.setLevel(Level.FINE);
        FileHandler logFileHandler = null;
        try {
            logFileHandler = new FileHandler(logDir + File.separator + "Manager.log");
            ConsoleHandler consoleHandler = new ConsoleHandler();
            SimpleFormatter formatterTxt = new SimpleFormatter();
            logFileHandler.setFormatter(formatterTxt);
            consoleHandler.setFormatter(formatterTxt);
            consoleHandler.setFilter(new Filter() {
                @Override
                public boolean isLoggable(LogRecord record) {
                    return record.getLevel().toString().toLowerCase().equals("info") || record.getLevel().toString().toLowerCase().equals("severe");
                }
            });
            logger.addHandler(logFileHandler);
            logger.addHandler(consoleHandler);
        }
        catch (Exception e) {
            System.err.print("[Manager] Could not create log file at " + logDir.getAbsolutePath());
        }
        return logger;
    }
}
