import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public class ClusterElasticityManager extends Thread {

    private static final String SCALE_DOWN_METHOD = "scaleDown";
    private static final String SCALE_UP_METHOD = "scaleUp";

    // The poll interval which is an int that tells the frequency at which the polling should take place
    // The Plugin's class name which is a string
    private int pollInterval;
    private String pluginClassName;

    // Instance to database API
    private DummyDB database;

    public ClusterElasticityManager(int pollInterval, String pluginClassName) {
        this.pollInterval = pollInterval;
        this.pluginClassName = pluginClassName;

        database = new DummyDB();
    }

    @Override
    public void run() {

        try
        {
            Class pluginClass = Class.forName(pluginClassName);
            ElasticityPlugin plugin = (ElasticityPlugin) pluginClass.getConstructors()[0].newInstance(null);

            Method[] methods = pluginClass.getMethods();

            for(Method method : methods)
            {
                if(method.getName().equals(SCALE_DOWN_METHOD))
                {
                    DataQuery dataQuery = method.getAnnotation(DataQuery.class);
                    NodeQuery nodeQuery = method.getAnnotation(NodeQuery.class);
                    String dataQueries[] = dataQuery.queries();
                    String nodeDataQuery = nodeQuery.query();

                    for(String query : dataQueries)
                    {
                        String data[] = database.executeQuery(query);

                    }
                }

                else if(method.getName().equals(SCALE_UP_METHOD))
                {

                }
            }
        }

        catch(ClassNotFoundException ex)
        {
            System.err.println("Class "+pluginClassName+" not found");
        }

        catch(Exception ex)
        {
            System.err.println(ex);
        }
    }

    
}
