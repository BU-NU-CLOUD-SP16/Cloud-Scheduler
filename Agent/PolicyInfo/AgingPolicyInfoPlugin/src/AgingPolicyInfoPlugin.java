import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Created by chemistry_sourabh on 4/17/16.
 */
public class AgingPolicyInfoPlugin implements PolicyInfoPlugin {

    private static final int FRAMEWORK_NAME = 1;

    private ClusterPriority clusterPriority;


    @Override
    @DataQuery(queries = {"select * from framework"})
    public void fetch(ArrayList<Data> data, Config config) {

        Gson gson = new Gson();

        JsonArray frameworkPriorityJson = gson.fromJson(config.getValueForKey("Framework-Priorities"),JsonArray.class);

        if (clusterPriority == null)
            clusterPriority = new ClusterPriority(Integer.parseInt(config.getValueForKey("Base-Priority")));

        int sumPriority = 0;

        ArrayList<String> names = getFrameworkNames(data.get(0));

        for(String name : names)
        {
            JsonObject obj = findPriorityWithName(frameworkPriorityJson,name);

            if (obj != null)
            {
                sumPriority += obj.get("priority").getAsInt();
            }
        }

        clusterPriority.setJobPriorities(sumPriority);

    }

    @Override
    public void notifyNewNodesCreation(ArrayList<Node> nodes) {
        clusterPriority.decrementNodesSurrendered();
        clusterPriority.resetRequestsSent();
        clusterPriority.incrementPOP();
    }

    @Override
    public void notifyRejectionFromOverlord()
    {
        clusterPriority.incrementRequestsSent();
    }

    @Override
    public void notifyDeletionOfNodes(ArrayList<Node> node) {

    }

    @Override
    public void notifyCreateResponseReceived(ArrayList<Node> nodes) {
        clusterPriority.decrementNodesSurrendered();
        clusterPriority.resetRequestsSent();
        clusterPriority.incrementPOP();
    }

    @Override
    public void notifyRequestToReleaseReceived(ArrayList<Node> nodes) {
        for (Node node : nodes)
            clusterPriority.incrementNodesSurrendered();
    }

    @Override
    public void notifyElapseOfTime(double seconds) {
            clusterPriority.decrementPOPBySeconds(seconds);
    }

    @Override
    public String updateStateInfo(String jsonString) {

        Gson gson = new Gson();
        JsonObject object = gson.fromJson(jsonString,JsonObject.class);
        object.addProperty("priority",clusterPriority.getClusterPriority());

        GlobalLogger.globalLogger.log(Level.INFO,"Cluster Priority = "+clusterPriority.getClusterPriority(),GlobalLogger.MANAGER_LOG_ID);

        return  object.toString();
    }

    private JsonObject findPriorityWithName(JsonArray frameworkPriorityJson, String name)
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

    private ArrayList<String> getFrameworkNames(Data frameworkData) {
        ArrayList<String> names = new ArrayList<>();

        for(String row[] : frameworkData.getData())
        {
            names.add(row[FRAMEWORK_NAME]);
        }

        return names;
    }
}
