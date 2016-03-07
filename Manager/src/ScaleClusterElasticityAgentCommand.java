import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by chemistry_sourabh on 3/4/16.
 */
public class ScaleClusterElasticityAgentCommand implements ClusterElasticityAgentCommand {

    private static final String SCALE_DOWN_METHOD = "scaleDown";
    private static final String SCALE_UP_METHOD = "scaleUp";
    private static final String SETUP_METHOD = "setup";

    private ElasticityPlugin elasticityPlugin;
    private ClusterScalerPlugin clusterScalerPlugin;
    private DBExecutor database;
    private ArrayList<Data> scaleDownData;
    private ArrayList<Data> scaleUpData;
    private ArrayList<Data> setupData;
    private String[] setupDataQueries;
    private String[] scaleDownDataQueries;
    private String scaleDownNodeQuery;
    private String[] scaleUpDataQueries;
    private Data scaleDownNodeData;

    public ScaleClusterElasticityAgentCommand(ElasticityPlugin elasticityPlugin, ClusterScalerPlugin clusterScalerPlugin, DBExecutor database) {
        this.elasticityPlugin = elasticityPlugin;
        this.clusterScalerPlugin = clusterScalerPlugin;
        this.database = database;
        processAnnotations();
    }

    private void processAnnotations()
    {
        Method[] methods = elasticityPlugin.getClass().getDeclaredMethods();

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

            else if(method.getName().equals(SETUP_METHOD))
            {
                DataQuery dataQuery = method.getAnnotation(DataQuery.class);
                setupDataQueries = dataQuery.queries();
            }


        }
    }

    private ArrayList<Data> executeQueries(String[] queries)
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

    private void fetchData() {

        setupData = executeQueries(setupDataQueries);
        scaleDownData = executeQueries(scaleDownDataQueries);
        scaleUpData = executeQueries(scaleUpDataQueries);


        if(!scaleDownNodeQuery.equals(""))
        {
            ArrayList<String[]> data = database.executeSelect(scaleDownNodeQuery);
            Data dataObject = new Data();
            dataObject.setData(data);
            dataObject.setQuery(scaleDownNodeQuery);
            scaleDownNodeData = dataObject;
        }
    }

    @Override
    public void execute() {
        fetchData();

        elasticityPlugin.setup(setupData);

        int newNodes = elasticityPlugin.scaleUp(scaleUpData);

        for(String nodeData[]: scaleDownNodeData.getData())
        {
            Node node = new Node();
            node.setData(nodeData);
            boolean shouldDelete = elasticityPlugin.scaleDown(node,scaleDownData);
            if(shouldDelete)
            {
                clusterScalerPlugin.deleteNode("");
            }
        }

        for(int i=0;i<newNodes;i++)
        {
            clusterScalerPlugin.createNewNode();
        }
    }
}


