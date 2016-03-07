import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public class MesosElasticityPlugin implements ElasticityPlugin {

    // FID Name CPU Memory Active Scheduled_Tasks
    // SID Load Free_Mem Total_Mem CPU Allocated_CPU IP Hostname

    private static final int FRAMEWORK_FID = 0;
    private static final int FRAMEWORK_NAME = 1;
    private static final int FRAMEWORK_CPU = 2;
    private static final int FRAMEWORK_MEMORY = 3;
    private static final int FRAMEWORK_ACTIVE = 4;
    private static final int FRAMEWORK_SCHEDULED_TASKS = 5;

    private static final int SLAVE_SID = 0;
    private static final int SLAVE_LOAD = 1;
    private static final int SLAVE_FREE_MEM = 2;
    private static final int SLAVE_TOTAL_MEM = 3;
    private static final int SLAVE_CPU = 4;
    private static final int SLAVE_ALLOCATED_CPU = 5;
    private static final int SLAVE_IP = 6;
    private static final int SLAVE_HOSTNAME = 7;

    private static final int CLUSTER_LOAD = 0;
    private static final int CLUSTER_FREE_MEM = 1;
    private static final int CLUSTER_TOT_MEM = 2;
    private static final int CLUSTER_CPU = 3;
    private static final int CLUSTER_ALLOCATED_CPU = 4;


    // Can be used to fetch policy data from file
    // Will be called at beginning of each command
    @DataQuery(queries = "")
    @Override
    public void setup(ArrayList<Data> data)
    {

    }

    @DataQuery(queries = {"select * from slave","select * from framework"})
    @Override
    public int scaleUp(ArrayList<Data> data)
    {
        Data slaveData = data.get(0);
        Data frameworkData = data.get(1);

        float clusterMetrics[] = calculateClusterMetrics(slaveData);

        if (clusterMetrics[CLUSTER_LOAD] > 0.85)
        {
            return 1;
        }

        else if(clusterMetrics[CLUSTER_FREE_MEM]/clusterMetrics[CLUSTER_TOT_MEM] < 0.1)
        {
            return 1;
        }

        System.out.println(Arrays.toString(clusterMetrics));

        String[] underObservationFrameworks = findActiveFrameworksWithNoResources(frameworkData);

        System.out.println(Arrays.toString(underObservationFrameworks));

        return 0;
    }

    @DataQuery(queries = {"SELECT Framework.*, Slave.* FROM Framework INNER JOIN Runs_On ON Runs_On.Framework_ID == Framework.Framework_ID INNER JOIN Slave ON Runs_On.Slave_ID == Slave.Slave_ID"})
    @NodeQuery(query = "SELECT * FROM Slave")
    @Override
    public boolean scaleDown(Node node, ArrayList<Data> data)
    {
        System.out.println("Scale Down");
        return false;
    }

    @Override
    public int requestResources(String parameters)
    {
        int equalIndex = parameters.indexOf('=');
        return Integer.parseInt(parameters.substring(equalIndex+1,parameters.length()).trim());
    }

    private float[] calculateClusterMetrics(Data slaveData)
    {
        ArrayList<String []> data = slaveData.getData();

        float tot_load = 0;
        float tot_free_mem = 0;
        float tot_tot_mem = 0;
        float tot_cpu = 0;
        float tot_allocated_cpu = 0;

        for(String row[] : data)
        {
            tot_load +=  Float.parseFloat(row[SLAVE_LOAD])/Float.parseFloat(row[SLAVE_CPU]);
            tot_free_mem +=  Float.parseFloat(row[SLAVE_FREE_MEM]);
            tot_tot_mem +=  Float.parseFloat(row[SLAVE_TOTAL_MEM]);
            tot_cpu +=  Float.parseFloat(row[SLAVE_CPU]);
            tot_allocated_cpu += Float.parseFloat(row[SLAVE_ALLOCATED_CPU])*Float.parseFloat(row[SLAVE_CPU]);
        }


        tot_allocated_cpu = (tot_allocated_cpu/tot_cpu) * 100;

        tot_load = tot_load/data.size();

        float metrics[] = {tot_load,tot_free_mem,tot_tot_mem,tot_cpu,tot_allocated_cpu};

        return metrics;
    }

    private String[] findActiveFrameworksWithNoResources(Data frameworkData)
    {
        ArrayList<String> frameworks = new ArrayList<>();
        for(String row[] : frameworkData.getData())
        {
            if(Float.parseFloat(row[FRAMEWORK_CPU]) == 0 && Float.parseFloat(row[FRAMEWORK_ACTIVE]) == 1 && Float.parseFloat(row[FRAMEWORK_MEMORY]) == 0)
            {
                frameworks.add(row[FRAMEWORK_FID]);
            }
        }
        return frameworks.toArray(new String[1]);
    }
}
