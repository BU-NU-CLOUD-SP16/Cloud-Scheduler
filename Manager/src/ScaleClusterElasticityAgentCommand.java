import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 3/4/16.
 */
public class ScaleClusterElasticityAgentCommand implements ClusterElasticityAgentCommand {

    private static final String SCALE_DOWN_METHOD = "scaleDown";
    private static final String SCALE_UP_METHOD = "scaleUp";
    private static final String SETUP_METHOD = "fetch";

    private ElasticityPlugin elasticityPlugin;
    private ClusterScalerPlugin clusterScalerPlugin;
    private DBExecutor database;
    private String[] setupDataQueries;

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


    @Override
    public void execute() {

        elasticityPlugin.fetch(fetchData(setupDataQueries));

        ArrayList<Node> newNodes = elasticityPlugin.scaleUp();


        if(newNodes != null) {
            for (Node newNode : newNodes) {
               Node node = clusterScalerPlugin.createNewNode(newNode);
                elasticityPlugin.notifyNewNodeCreation(node);
            }
        }

        ArrayList<Node> shouldBeDeletedNodes = elasticityPlugin.scaleDown();
        if(shouldBeDeletedNodes != null) {
            for (Node nodeToBeDeleted : shouldBeDeletedNodes) {
                clusterScalerPlugin.deleteNode(nodeToBeDeleted);
            }
        }
    }
}


