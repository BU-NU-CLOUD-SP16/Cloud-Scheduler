import java.lang.reflect.Method;
import java.util.ArrayList;
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
    private String elasticityPluginClassName;
    private String clusterScalerPluginClassName;
    private String databaseExecutorPluginClassName;

    private ElasticityPlugin elasticityPlugin;
    private ClusterScalerPlugin scalerPlugin;

    // Instance to database API
    private DBExecutor database;

    private String[] setupDataQueries;

    private Logger logger = GlobalLogger.globalLogger;

    private  Config config;

    public ClusterElasticityManager(CommandLineArguments arguments) {

        logger.log(Level.FINER,"Entering ClusterElasticityManager Constructor",Constants.MANAGER_LOG_ID);

        this.elasticityPluginClassName = arguments.getCemanagerPluginMainClass();
        this.clusterScalerPluginClassName = arguments.getClusterScalerPluginMainClass();
        this.databaseExecutorPluginClassName = arguments.getDbExecutorPluginMainClass();
        this.config = arguments.getConfig();

        logger.log(Level.CONFIG,"Elasticity Plugin Class Name = "+elasticityPluginClassName,Constants.MANAGER_LOG_ID);
        logger.log(Level.CONFIG,"Cluster Scaler Plugin Class Name = "+clusterScalerPluginClassName,Constants.MANAGER_LOG_ID);
        logger.log(Level.CONFIG,"Database Executor Plugin Class Name = "+databaseExecutorPluginClassName,Constants.MANAGER_LOG_ID);

        createInstances();
        processAnnotations();
        logger.log(Level.FINER,"Exiting ClusterElasticityManager Constructor",Constants.MANAGER_LOG_ID);
    }


    private void createInstances() {
        logger.log(Level.FINER,"Entering createInstances",Constants.MANAGER_LOG_ID);
        logger.log(Level.INFO,"Started Cluster Elasticity Manager",Constants.MANAGER_LOG_ID);
        try
        {
            Class elasticityPluginClass = Class.forName(elasticityPluginClassName);
            logger.log(Level.FINE,"Got Class of Elasticity Plugin = "+elasticityPluginClassName,Constants.MANAGER_LOG_ID);

            Class clusterScalerPluginClass = Class.forName(clusterScalerPluginClassName);
            logger.log(Level.FINE,"Got Class of Cluster Scaler Plugin = "+clusterScalerPluginClassName,Constants.MANAGER_LOG_ID);

            Class databaseExecutorPluginClass = Class.forName(databaseExecutorPluginClassName);
            logger.log(Level.FINE,"Got Class of Database Executor Plugin = "+databaseExecutorPluginClassName,Constants.MANAGER_LOG_ID);

            elasticityPlugin = (ElasticityPlugin) elasticityPluginClass.getConstructor().newInstance();
            logger.log(Level.FINE,"Created Instance of "+elasticityPluginClassName,Constants.MANAGER_LOG_ID);
            scalerPlugin = (ClusterScalerPlugin) clusterScalerPluginClass.getConstructor().newInstance();
            logger.log(Level.FINE,"Created Instance of "+clusterScalerPluginClassName,Constants.MANAGER_LOG_ID);
            database = (DBExecutor) databaseExecutorPluginClass.getConstructor(String.class).newInstance(config.getValueForKey("Id"));
            logger.log(Level.FINE,"Created Instance of "+databaseExecutorPluginClassName,Constants.MANAGER_LOG_ID);
        }

        catch(ClassNotFoundException ex)
        {
            logger.log(Level.SEVERE,ex.toString(),Constants.MANAGER_LOG_NAME);
        }

        catch(Exception ex)
        {
            logger.log(Level.SEVERE,ex.toString(),Constants.MANAGER_LOG_ID);
        }
        logger.log(Level.FINER,"Exiting ClusterElasticityManager Constructor",Constants.MANAGER_LOG_ID);
    }


    @Override
    public void notifyResourceScaling(String parameters) throws ClusterElasticityAgentException {
        logger.log(Level.FINER,"Entering notifyResourceScaling",Constants.MANAGER_LOG_ID);
        ArrayList<Node> newNodes = elasticityPlugin.requestResources(parameters);
        logger.log(Level.FINE,"Executed RequestResources on plugin",Constants.MANAGER_LOG_ID);
        for(Node newNode : newNodes)
        {
            Node node = scalerPlugin.createNewNode(newNode);
            logger.log(Level.FINE,"Executed CreateNewNode on plugin",Constants.MANAGER_LOG_ID);
            elasticityPlugin.notifyNewNodeCreation(node);
            logger.log(Level.FINE,"Executed NotifyNewNodeCreation on plugin",Constants.MANAGER_LOG_ID);
        }
        logger.log(Level.FINER,"Exiting notifyResourceScaling",Constants.MANAGER_LOG_ID);
    }

    @Override
    public void notifyReleaseNodeRequest(String string)
    {
        logger.log(Level.FINER,"Entering notifyReleaseNodeRequest",Constants.MANAGER_LOG_ID);
        ArrayList<Node> releaseNodes = elasticityPlugin.receivedReleaseNodeRequest(string);
        for(Node node : releaseNodes) {
            scalerPlugin.deleteNode(node);
        }
    }

    @Override
    public void notifyTimerExpiry() throws ClusterElasticityAgentException {
        logger.log(Level.FINER,"Entering notifyTimerExpiry",Constants.MANAGER_LOG_ID);
        ArrayList<Node> nodes = elasticityPlugin.fetch(fetchData(setupDataQueries),config);
        logger.log(Level.FINE,"Executed Fetch on plugin",Constants.MANAGER_LOG_ID);

        ArrayList<Node> newNodes = elasticityPlugin.scaleUp();
        logger.log(Level.FINE,"Executed ScaleUp on plugin",Constants.MANAGER_LOG_ID);

        scalerPlugin.setup(config,nodes);
        logger.log(Level.FINE,"Executed Setup on plugin",Constants.MANAGER_LOG_ID);

        if(newNodes != null) {
            for (Node newNode : newNodes) {
                Node node = scalerPlugin.createNewNode(newNode);
                logger.log(Level.FINE,"Executed CreateNewNode on plugin",Constants.MANAGER_LOG_ID);
                elasticityPlugin.notifyNewNodeCreation(node);
                logger.log(Level.FINE,"Executed NotifyNewNodeCreation on plugin",Constants.MANAGER_LOG_ID);
            }
        }

        ArrayList<Node> shouldBeDeletedNodes = elasticityPlugin.scaleDown();
        logger.log(Level.FINE,"Executed ScaleDown on plugin",Constants.MANAGER_LOG_ID);
        if(shouldBeDeletedNodes != null) {
            for (Node nodeToBeDeleted : shouldBeDeletedNodes) {
                scalerPlugin.deleteNode(nodeToBeDeleted);
                logger.log(Level.FINE,"Executed DeleteNode on plugin",Constants.MANAGER_LOG_ID);
            }
        }
        logger.log(Level.FINER,"Exiting notifyTimerExpiry",Constants.MANAGER_LOG_ID);
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
