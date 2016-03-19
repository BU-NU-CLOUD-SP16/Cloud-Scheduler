import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Timer;
import java.util.logging.*;

/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public class ClusterElasticityManager implements ClusterElasticityManagerFramework {

    private static final String MANAGER_LOGGER = "Cluster Elasticity Manager";
    private static final String SCALE_DOWN_METHOD = "scaleDown";
    private static final String SCALE_UP_METHOD = "scaleUp";
    private static final String SETUP_METHOD = "fetch";

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

    private String[] setupDataQueries;


    private File logDir;
    private Logger logger;


    public ClusterElasticityManager(CommandLineArguments arguments) {
        this.pollInterval = arguments.getPollInterval();
        this.elasticityPluginClassName = arguments.getCemanagerPluginMainClass();
        this.clusterScalerPluginClassName = arguments.getClusterScalerPluginMainClass();
        this.databaseExecutorPluginClassName = arguments.getDbExecutorPluginMainClass();
        this.logDir = new File(arguments.getLogDir());

        logger = logSetup();

        logger.config("Elasticity Plugin Class Name = "+elasticityPluginClassName);
        logger.config("Cluster Scaler Plugin Class Name = "+clusterScalerPluginClassName);
        logger.config("Database Executor Plugin Class Name = "+databaseExecutorPluginClassName);

        createInstances();
        processAnnotations();
    }


    private void createInstances() {
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
    }


    @Override
    public void notifyResourceScaling(String parameters) throws ClusterElasticityAgentException {
        ArrayList<Node> newNodes = elasticityPlugin.requestResources(parameters);
        for(Node newNode : newNodes)
        {
            scalerPlugin.createNewNode(newNode);
        }
    }

    @Override
    public void notifyTimerExpiry() throws ClusterElasticityAgentException {
        elasticityPlugin.fetch(fetchData(setupDataQueries));

        ArrayList<Node> newNodes = elasticityPlugin.scaleUp();

        scalerPlugin.setup();

        if(newNodes != null) {
            for (Node newNode : newNodes) {
                Node node = scalerPlugin.createNewNode(newNode);
                elasticityPlugin.notifyNewNodeCreation(node);
            }
        }

        ArrayList<Node> shouldBeDeletedNodes = elasticityPlugin.scaleDown();
        if(shouldBeDeletedNodes != null) {
            for (Node nodeToBeDeleted : shouldBeDeletedNodes) {
                scalerPlugin.deleteNode(nodeToBeDeleted);
            }
        }
    }

    private Logger logSetup() {
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
                    return record.getLevel().toString().toLowerCase().equals("info") || record.getLevel().toString().toLowerCase().equals("severe") ||
                            record.getLevel().toString().toLowerCase().equals("config");
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

    private void processAnnotations()
    {
        Method[] methods = elasticityPlugin.getClass().getDeclaredMethods();

        for(Method method : methods)
        {

            if(method.getName().equals(SETUP_METHOD))
            {
                DataQuery dataQuery = method.getAnnotation(DataQuery.class);
                setupDataQueries = dataQuery.queries();
            }

        }
    }

    private ArrayList<Data> fetchData(String[] queries)
    {
        ArrayList<Data> datas = new ArrayList<>();
        for(String query : queries)
        {
            if(query.equals(""))
            {
                continue;
            }

            ArrayList<String[]> data = database.executeSelect(query);
            Data dataObject = new Data();
            dataObject.setData(data);
            dataObject.setQuery(query);
            datas.add(dataObject);
        }
        return datas;
    }
}
