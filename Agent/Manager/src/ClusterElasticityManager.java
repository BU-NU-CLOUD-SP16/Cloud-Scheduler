import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

    private AuthorizationAgent authorizationAgent;

    private String[] setupDataQueries;

    private Logger logger = GlobalLogger.globalLogger;

    private  Config config;

    private int status = ClusterState.ACTIVE_STATUS;
    private ClusterPriority clusterPriority;
    private  long lastTime = 0;
    private JsonArray frameworkPriorityJson;

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
        createAuthorizationAgent();
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
            clusterPriority.decrementNodesSurrendered();
            clusterPriority.resetRequestsSent();
            clusterPriority.incrementPOP();
        }
        logger.log(Level.FINER,"Exiting notifyResourceScaling",Constants.MANAGER_LOG_ID);
    }

    @Override
    public void notifyReleaseNodeRequest(String string)
    {
        logger.log(Level.FINER,"Entering notifyReleaseNodeRequest",Constants.MANAGER_LOG_ID);
        logger.log(Level.INFO,"Got order from Overlord to Give up node",Constants.MANAGER_LOG_ID);
        ArrayList<Node> releaseNodes = elasticityPlugin.receivedReleaseNodeRequest(string);
        for(Node node : releaseNodes) {
            scalerPlugin.deleteNode(node);
        }
        clusterPriority.incrementNodesSurrendered();
        logger.log(Level.FINER,"Exiting notifyReleaseNodeRequest",Constants.MANAGER_LOG_ID);
    }

    @Override
    public void notifyCreateNodeResponse(String json) {
        logger.log(Level.FINER,"Entering notifyCreateNodeResponse",Constants.MANAGER_LOG_ID);
        logger.log(Level.INFO,"Got Permission from Overlord to create node",Constants.MANAGER_LOG_ID);
        ArrayList<Node> nodes = elasticityPlugin.receivedCreateNodeResponse(json);

        for(Node node : nodes)
        {
            Node newNode = scalerPlugin.createNewNode(node);
            logger.log(Level.FINE,"Executed CreateNewNode on plugin",Constants.MANAGER_LOG_ID);
            elasticityPlugin.notifyNewNodeCreation(newNode);
            logger.log(Level.FINE,"Executed NotifyNewNodeCreation on plugin",Constants.MANAGER_LOG_ID);
        }

        authorizationAgent.setWaitingForResponse(false);
        clusterPriority.decrementNodesSurrendered();
        clusterPriority.resetRequestsSent();
        clusterPriority.incrementPOP();
        logger.log(Level.FINER,"Exiting notifyCreateNodeResponse",Constants.MANAGER_LOG_ID);
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
        if(newNodes != null && authorizationAgent.canCreateNewNodes(newNodes.size())) {
            status = ClusterState.CREATE_STATUS;
            for (Node newNode : newNodes) {
                Node node = scalerPlugin.createNewNode(newNode);
                logger.log(Level.FINE,"Executed CreateNewNode on plugin",Constants.MANAGER_LOG_ID);
                elasticityPlugin.notifyNewNodeCreation(node);
                logger.log(Level.FINE,"Executed NotifyNewNodeCreation on plugin",Constants.MANAGER_LOG_ID);
                clusterPriority.decrementNodesSurrendered();
                clusterPriority.resetRequestsSent();
                clusterPriority.incrementPOP();
            }
        }

        else if(!authorizationAgent.isWaitingForResponse() && newNodes.size() > 0)
        {
            clusterPriority.incrementRequestsSent();
        }

        ArrayList<Node> shouldBeDeletedNodes = elasticityPlugin.scaleDown();
        logger.log(Level.FINE,"Executed ScaleDown on plugin",Constants.MANAGER_LOG_ID);
        if(shouldBeDeletedNodes != null) {
            status = ClusterState.DELETE_STATUS;
            for (Node nodeToBeDeleted : shouldBeDeletedNodes) {
                scalerPlugin.deleteNode(nodeToBeDeleted);
                logger.log(Level.FINE,"Executed DeleteNode on plugin",Constants.MANAGER_LOG_ID);
            }
        }

        status = ClusterState.ACTIVE_STATUS;

        logger.log(Level.FINER,"Exiting notifyTimerExpiry",Constants.MANAGER_LOG_ID);
    }

    @Override
    public String notifyStateRequest() {
        ArrayList<Node> nodes = elasticityPlugin.getNodes();
        ArrayList<String> names = elasticityPlugin.getFrameworkNames();

        double diffTime = (System.currentTimeMillis() - lastTime) / 1000.0;

        clusterPriority.decrementPOPBySeconds(diffTime);
        lastTime = System.currentTimeMillis();
        Gson gson = new Gson();

        frameworkPriorityJson = gson.fromJson(config.getValueForKey("Framework-Priorities"),JsonArray.class);

        int sumPriority = 0;

        for(String name : names)
        {
            JsonObject obj = findPriorityWithName(name);

            if (obj != null)
            {
                sumPriority += obj.get("priority").getAsInt();
            }
        }

        clusterPriority.setJobPriorities(sumPriority);

        ClusterState state = new ClusterState();
        state.setId(config.getValueForKey("Id"));
        state.setPriority(clusterPriority.getClusterPriority());
        state.setPort(config.getValueForKey("Port"));
        state.setMinNodes(Integer.parseInt(config.getValueForKey("Min-Nodes")));
        state.setNodesInCluster(nodes);
        state.setStatus(status);

        logger.log(Level.INFO,"Cluster Priority = "+clusterPriority,Constants.MANAGER_LOG_ID);

        return state.toString();
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

    private void createAuthorizationAgent()
    {
        ArrayList<Node> nodes = elasticityPlugin.getNodes();
        ArrayList<String> names = elasticityPlugin.getFrameworkNames();


        Gson gson = new Gson();

        frameworkPriorityJson = gson.fromJson(config.getValueForKey("Framework-Priorities"),JsonArray.class);

        clusterPriority = new ClusterPriority(Integer.parseInt(config.getValueForKey("Base-Priority")));

        int sumPriority = 0;

        for(String name : names)
        {
            JsonObject obj = findPriorityWithName(name);

            if (obj != null)
            {
                sumPriority += obj.get("priority").getAsInt();
            }
        }

        clusterPriority.setJobPriorities(sumPriority);

        lastTime = System.currentTimeMillis();
        ClusterState state = new ClusterState();
        state.setId(config.getValueForKey("Id"));
        state.setPriority(clusterPriority.getClusterPriority());
        state.setPort(config.getValueForKey("Port"));
        state.setMinNodes(Integer.parseInt(config.getValueForKey("Min-Nodes")));
        state.setNodesInCluster(nodes);
        state.setStatus(status);

        this.authorizationAgent = new AuthorizationAgent(config.getValueForKey("Overlord-Ip"),Integer.parseInt(config.getValueForKey("Overlord-Port")),state);
    }

    private JsonObject findPriorityWithName(String name)
    {
        for(JsonElement element : frameworkPriorityJson)
        {
            JsonObject object = element.getAsJsonObject();
            if (object.get("name").getAsString().toLowerCase().contains(name.toLowerCase()))
            {
                return object;
            }
        }

        return null;
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
