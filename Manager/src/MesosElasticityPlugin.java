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


    private long last_time = System.currentTimeMillis();

    private ArrayList<Framework> frameworks;
    private ArrayList<Slave> slaves;
    // Should be used to fetch required data from db and policy info from files
    // Will be called at beginning of each command
    @DataQuery(queries = {"select * from slave","select * from framework","select * from runs_on"})
    @Override
    public void fetch(ArrayList<Data> data)
    {
        last_time = System.currentTimeMillis();
        Data slaveData = data.get(0);
        Data frameworkData = data.get(1);
        Data runsOnData = data.get(2);

        frameworks = convertToFrameworkObjects(frameworkData);
        slaves = convertToSlaveObjects(slaveData);
        connect(frameworks,slaves,runsOnData);

    }

    @Override
    public ArrayList<Node> scaleUp()
    {
        ArrayList<Node> nodes = new ArrayList<>();
        float clusterMetrics[] = calculateClusterMetrics();

        if (clusterMetrics[CLUSTER_LOAD] > 0.85)
        {
            nodes.add(new OpenStackNode());
            return nodes;
        }

        else if(clusterMetrics[CLUSTER_FREE_MEM]/clusterMetrics[CLUSTER_TOT_MEM] < 0.1)
        {
            nodes.add(new OpenStackNode());
            return nodes;
        }

        System.out.println(Arrays.toString(clusterMetrics));

        ArrayList<Framework> underObservationFrameworks = findActiveFrameworksWithNoResources();
        boolean freeCPUSPresent = isFreeCPUPresent();
        System.out.println(underObservationFrameworks);

        if(!freeCPUSPresent && underObservationFrameworks.size() > 0)
        {
            nodes.add(new OpenStackNode());
            return nodes;
        }

        else
        {
            //Observe Framework for a while
        }
        System.out.println(last_time);
        return nodes;
    }


    @Override
    public ArrayList<Node> scaleDown()
    {
        System.out.println("Scale Down");
        return null;
    }

    @Override
    public int requestResources(String parameters)
    {
        int equalIndex = parameters.indexOf('=');
        return Integer.parseInt(parameters.substring(equalIndex+1,parameters.length()).trim());
    }

    private float[] calculateClusterMetrics()
    {

        float tot_load = 0;
        float tot_free_mem = 0;
        float tot_tot_mem = 0;
        float tot_cpu = 0;
        float tot_allocated_cpu = 0;

        for(Slave slave : slaves)
        {
            tot_load +=  slave.getLoad()/slave.getCpu();
            tot_free_mem +=  slave.getFree_mem();
            tot_tot_mem +=  slave.getTotal_mem();
            tot_cpu +=  slave.getCpu();
            tot_allocated_cpu += slave.getAllocated_cpu() * slave.getCpu();
        }


        tot_allocated_cpu = (tot_allocated_cpu/tot_cpu) * 100;

        tot_load = tot_load/slaves.size();

        float metrics[] = {tot_load,tot_free_mem,tot_tot_mem,tot_cpu,tot_allocated_cpu};

        return metrics;
    }

    private ArrayList<Framework> findActiveFrameworksWithNoResources()
    {
        ArrayList <Framework> frameworks = new ArrayList<>();
        for(Framework framework : frameworks)
        {
            if(framework.getCpu() == 0 && framework.isActive() && framework.getMemory() == 0)
            {
                frameworks.add(framework);
            }
        }
        return frameworks;
    }

    private boolean isFreeCPUPresent() {

        for(Slave slave : slaves)
        {
            if(slave.getAllocated_cpu() < 1)
            {
                return true;
            }
        }

        return false;
    }

    private ArrayList<Framework> convertToFrameworkObjects(Data frameworkData) {
        ArrayList<Framework> frameworks = new ArrayList<>();

        for(String row[] : frameworkData.getData())
        {
            Framework f = new Framework();
            f.setId(row[FRAMEWORK_FID]);
            f.setName(row[FRAMEWORK_NAME]);
            if(Integer.parseInt(row[FRAMEWORK_ACTIVE]) == 1)
            {
                f.setActive(true);
            }

            else
            {
                f.setActive(false);
            }

            f.setCpu(Integer.parseInt(row[FRAMEWORK_CPU]));
            f.setMemory(Float.parseFloat(row[FRAMEWORK_MEMORY]));
            if(Integer.parseInt(row[FRAMEWORK_SCHEDULED_TASKS]) == 1)
            {
                f.setScheduled_tasks(true);
            }

            else
            {
                f.setScheduled_tasks(false);
            }
            frameworks.add(f);
        }

        return frameworks;
    }

    private ArrayList<Slave> convertToSlaveObjects(Data slaveData) {
        ArrayList<Slave> slaves = new ArrayList<>();

        for(String row[] : slaveData.getData())
        {
            Slave s = new Slave();
            s.setId(row[SLAVE_SID]);
            s.setLoad(Float.parseFloat(row[SLAVE_LOAD]));
            s.setCpu(Integer.parseInt(row[SLAVE_CPU]));
            s.setAllocated_cpu(Float.parseFloat(row[SLAVE_ALLOCATED_CPU]));
            s.setFree_mem(Float.parseFloat(row[SLAVE_FREE_MEM]));
            s.setTotal_mem(Float.parseFloat(row[SLAVE_TOTAL_MEM]));
            s.setIp(row[SLAVE_IP]);
            s.setHostname(row[SLAVE_HOSTNAME]);
            slaves.add(s);
        }

        return slaves;
    }

    private void connect(ArrayList<Framework> frameworks, ArrayList<Slave> slaves, Data runsOnData)
    {
        for(Framework f : frameworks)
        {
            ArrayList<Slave> match = new ArrayList<>();
            for(String row[] : runsOnData.getData())
            {
                if(row[0].equals(""+f.getId()))
                {
                    match.add(findSlave(row[1],slaves));
                }
            }
            f.setAllocated_slaves(match);
        }

        for(Slave s : slaves)
        {
            ArrayList<Framework> match = new ArrayList<>();
            for(String row[] : runsOnData.getData())
            {
                if(row[1].equals(""+s.getId()))
                {
                    match.add(findFramework(row[0],frameworks));
                }
            }
            s.setFrameworks_running(match);
        }
    }

    private Slave findSlave(String id, ArrayList<Slave> slaves)
    {
        for(Slave slave : slaves)
        {
            if(slave.getId().equals(id))
            {
                return slave;
            }
        }

        return null;
    }

    private Framework findFramework(String id, ArrayList<Framework> frameworks)
    {
        for(Framework framework : frameworks)
        {
            if(framework.getId().equals(id))
            {
                return framework;
            }
        }
        return null;
    }
}
