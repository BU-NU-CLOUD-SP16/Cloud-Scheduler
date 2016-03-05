import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public class ClusterElasticityManager implements ClusterElasticityManagerFramework {

    private static final String SCALE_DOWN_METHOD = "scaleDown";
    private static final String SCALE_UP_METHOD = "scaleUp";
    private static final String REQUEST_RESOURCES_METHOD = "requestResources";

    private static final String REQUEST_RESOURCES_COMMAND = "RequestResourcesClusterElasticityAgentCommand";
    private static final String SCALE_COMMAND = "ScaleClusterElasticityAgentCommand";
    // The poll interval which is an int that tells the frequency at which the polling should take place
    // The Plugin's class name which is a string
    private int pollInterval;
    private String elasticityPluginClassName;
    private String clusterScalerPluginClassName;

    private ElasticityPlugin elasticityPlugin;
    private ClusterScalerPlugin scalerPlugin;

    // Instance to database API
    private DummyDB database;

    private String scaleDownDataQueries[];
    private String scaleDownNodeQuery;
    private String scaleUpDataQueries[];

    private Data scaleDownNodeData;
    private ArrayList<Data> scaleDownData;
    private ArrayList<Data> scaleUpData;

    private LinkedBlockingQueue<ClusterElasticityAgentCommand> workerQueue;

    public ClusterElasticityManager(CommandLineArguments arguments) {
        this.pollInterval = 5000;
        this.elasticityPluginClassName = arguments.getCemanagerPluginMainClass();
        this.clusterScalerPluginClassName = "OpenStackClusterScalerPlugin";
        workerQueue = new LinkedBlockingQueue<>();
        database = new DummyDB();
    }

    private Data convertToData(String data[][])
    {
        Data dataObject = new Data();
        ArrayList<ArrayList> rows = new ArrayList<>();

        for(String row[] : data)
        {
            ArrayList<String> rowList = new ArrayList<>(Arrays.asList(row));
            rows.add(rowList);
        }

        dataObject.setData(rows);
        return dataObject;
    }

    @Override
    public void run() {

        try
        {
            Class elasticityPluginClass = Class.forName(elasticityPluginClassName);
            Class clusterScalerPluginClass = Class.forName(clusterScalerPluginClassName);
            elasticityPlugin = (ElasticityPlugin) elasticityPluginClass.getConstructors()[0].newInstance(null);
            scalerPlugin = (ClusterScalerPlugin) clusterScalerPluginClass.getConstructors()[0].newInstance(null);


            Method[] methods = elasticityPluginClass.getMethods();

            for(Method method : methods)
            {
                if(method.getName().equals(SCALE_DOWN_METHOD))
                {
                    DataQuery dataQuery = method.getAnnotation(DataQuery.class);
                    NodeQuery nodeQuery = method.getAnnotation(NodeQuery.class);
                    scaleDownDataQueries = dataQuery.queries();
                    scaleDownNodeQuery = nodeQuery.query();
                }

                else if(method.getName().equals(SCALE_UP_METHOD))
                {
                    DataQuery dataQuery = method.getAnnotation(DataQuery.class);
                    scaleUpDataQueries = dataQuery.queries();
                }


            }
        }

        catch(ClassNotFoundException ex)
        {
            System.err.println("Class "+ elasticityPluginClassName +" not found");
        }

        catch(Exception ex)
        {
            System.err.println(ex);
        }


        new Timer().scheduleAtFixedRate(new ClusterElasticityAgentTimerTask(this, new ScaleClusterElasticityAgentCommand() {
            @Override
            public void execute() {

            }
        }),0,pollInterval);

        while(true)
        {

            try {
                ClusterElasticityAgentCommand command = workerQueue.take();

                switch(command.getClass().getName())
                {
                    case SCALE_COMMAND:
                        fetchData();

                        int newNodes = elasticityPlugin.scaleUp(scaleUpData);

                        for(ArrayList nodeData: scaleDownNodeData.getData())
                        {
                            Node node = new Node();
                            node.setData(nodeData);
                            boolean shouldDelete = elasticityPlugin.scaleDown(node,scaleDownData);
                            if(shouldDelete)
                            {
                                scalerPlugin.deleteNode("");
                            }
                        }

                        for(int i=0;i<newNodes;i++)
                        {
                            scalerPlugin.createNewNode();
                        }
                    case REQUEST_RESOURCES_COMMAND:
                        int newNodesCount = elasticityPlugin.requestResources("");
                        for(int i=0;i<newNodesCount;i++)
                        {
                            scalerPlugin.createNewNode();
                        }

                    default:
                        command.execute();

                }

                command.execute();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void fetchData() {
        scaleDownData = new ArrayList<>();
        scaleUpData = new ArrayList<>();

        for(String query : scaleDownDataQueries)
        {
            String data[][] = database.executeQuery(query);
            Data dataObject = convertToData(data);
            dataObject.setQuery(query);
            scaleDownData.add(dataObject);
        }

        for(String query : scaleUpDataQueries)
        {
            String data[][] = database.executeQuery(query);
            Data dataObject = convertToData(data);
            dataObject.setQuery(query);
            scaleUpData.add(dataObject);
        }

        String data[][] = database.executeQuery(scaleDownNodeQuery);
        Data dataObject = convertToData(data);
        dataObject.setQuery(scaleDownNodeQuery);
        scaleDownNodeData = dataObject;

    }


    @Override
    public void notifyResourceScaling(ClusterElasticityAgentCommand workerCommand) throws ClusterElasticityAgentException {
        try {
            workerQueue.put(workerCommand);
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
