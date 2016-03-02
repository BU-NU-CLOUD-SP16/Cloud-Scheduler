/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public class ClusterElasticityManager extends Thread {

    // The poll interval which is an int that tells the frequency at which the polling should take place
    // The Plugin's class name which is a string

    private int pollInterval;
    private String pluginClassName;

    public ClusterElasticityManager(int pollInterval, String pluginClassName) {
        this.pollInterval = pollInterval;
        this.pluginClassName = pluginClassName;
    }

    @Override
    public void run() {

        try
        {
            Class pluginClass = Class.forName(pluginClassName);
            Object plugin = pluginClass.getConstructors()[0].newInstance(null);
        }

        catch(ClassNotFoundException ex)
        {
            System.err.println("Class "+pluginClassName+" not found");
        }

        catch(Exception ex)
        {
            System.err.println(ex);
        }

        // All the mojo will happen here

    }
}
