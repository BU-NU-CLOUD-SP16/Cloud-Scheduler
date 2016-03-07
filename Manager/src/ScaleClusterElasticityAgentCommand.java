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

    private void fetchData() {
        scaleDownData = new ArrayList<>();
        scaleUpData = new ArrayList<>();
        setupData = new ArrayList<>();

        for(String query : setupDataQueries)
        {
            String data[][] = database.executeSelect(query);
            Data dataObject = convertToData(data);
            dataObject.setQuery(query);
            setupData.add(dataObject);
        }

        for(String query : scaleDownDataQueries)
        {
            String data[][] = database.executeSelect(query);
            Data dataObject = convertToData(data);
            dataObject.setQuery(query);
            scaleDownData.add(dataObject);
        }

        for(String query : scaleUpDataQueries)
        {
            String data[][] = database.executeSelect(query);
            Data dataObject = convertToData(data);
            dataObject.setQuery(query);
            scaleUpData.add(dataObject);
        }

        String data[][] = database.executeSelect(scaleDownNodeQuery);
        Data dataObject = convertToData(data);
        dataObject.setQuery(scaleDownNodeQuery);
        scaleDownNodeData = dataObject;

    }

    @Override
    public void execute() {
        fetchData();

        elasticityPlugin.setup(setupData);

        int newNodes = elasticityPlugin.scaleUp(scaleUpData);

        for(ArrayList nodeData: scaleDownNodeData.getData())
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


