import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final int FRAMEWORK_FILTER = 30000;
    private static final int SLAVE_NEW_FILTER = 300000;

    private static final int MIN_SLAVES = 2;

    private ArrayList<String> NO_DELETE_SLAVES;
    private double SCALE_UP_CLUSTER_LOAD_THRESHOLD = 0.85;
    private double SCALE_UP_CLUSTER_MEM_THRESHOLD = 0.1;
    private double SCALE_UP_SLAVE_LOAD_THRESHOLD = 0.85;
    private double SCALE_UP_SLAVE_MEM_THRESHOLD = 0.1;
    private double SCALE_DOWN_CLUSTER_LOAD_THRESHOLD = 0.8;
    private double SCALE_DOWN_CLUSTER_MEM_THRESHOLD = 0.3;
    private double SCALE_DOWN_SLAVE_LOAD_THRESHOLD = 0.1;
    private double SCALE_DOWN_SLAVE_MEM_THRESHOLD = 0.7;

    private long last_time = System.currentTimeMillis();

    private long noScaleUpFilter = 0;
    private boolean noScaleUpFilterSet = false;

    private ArrayList<Framework> frameworks;
    private ArrayList<Slave> slaves;

    private Logger logger;

    private String newNodeFlavor;
    private String privateKey;
    private String hdfsIp;

    public MesosElasticityPlugin()
    {
        frameworks = new ArrayList<>();
        slaves = new ArrayList<>();
        logger = GlobalLogger.globalLogger;
    }


    // Should be used to fetch required data from db and policy info from files
    // Will be called at beginning of each command
    @DataQuery(queries = {"select * from slave","select * from framework","select * from runs_on"})
    @Override
    public ArrayList<Node> fetch(ArrayList<Data> data,Config config)
    {
        logger.log(Level.FINER,"Entering fetch",GlobalLogger.MANAGER_LOG_ID);
        long current_time = System.currentTimeMillis();
        Data slaveData = data.get(0);
        Data frameworkData = data.get(1);
        Data runsOnData = data.get(2);

        privateKey = config.getValueForKey("SSH-Private-Key");
        hdfsIp = config.getValueForKey("Mesos-HDFS-Master");
        JsonArray json = new Gson().fromJson(config.getValueForKey("No-Delete-Slaves"),JsonArray.class);

        NO_DELETE_SLAVES = new ArrayList<String>();

        for (JsonElement  element : json)
        {
            NO_DELETE_SLAVES.add(element.getAsString());
        }

        newNodeFlavor = config.getValueForKey("New-Node-Flavor");

        SCALE_UP_CLUSTER_LOAD_THRESHOLD = Float.parseFloat(config.getValueForKey("Scale-Up-Cluster-Load"));
        SCALE_UP_CLUSTER_MEM_THRESHOLD = Float.parseFloat(config.getValueForKey("Scale-Up-Cluster-Memory"));
        SCALE_UP_SLAVE_LOAD_THRESHOLD = Float.parseFloat(config.getValueForKey("Scale-Up-Slave-Load"));
        SCALE_UP_SLAVE_MEM_THRESHOLD = Float.parseFloat(config.getValueForKey("Scale-Up-Slave-Memory"));
        SCALE_DOWN_CLUSTER_LOAD_THRESHOLD = Float.parseFloat(config.getValueForKey("Scale-Down-Cluster-Load"));
        SCALE_DOWN_CLUSTER_MEM_THRESHOLD = Float.parseFloat(config.getValueForKey("Scale-Down-Cluster-Memory"));
        SCALE_DOWN_SLAVE_LOAD_THRESHOLD = Float.parseFloat(config.getValueForKey("Scale-Down-Slave-Load"));
        SCALE_DOWN_SLAVE_MEM_THRESHOLD = Float.parseFloat(config.getValueForKey("Scale-Down-Cluster-Memory"));

        logger.log(Level.INFO,"Cluster Scale Up = "+SCALE_UP_CLUSTER_LOAD_THRESHOLD+" "+SCALE_UP_CLUSTER_MEM_THRESHOLD,GlobalLogger.MANAGER_LOG_ID);
        logger.log(Level.INFO,"Slave Scale Up = "+SCALE_UP_SLAVE_LOAD_THRESHOLD+" "+SCALE_UP_SLAVE_MEM_THRESHOLD,GlobalLogger.MANAGER_LOG_ID);
        logger.log(Level.INFO,"Cluster Scale Down = "+SCALE_DOWN_CLUSTER_LOAD_THRESHOLD+" "+SCALE_DOWN_CLUSTER_MEM_THRESHOLD,GlobalLogger.MANAGER_LOG_ID);
        logger.log(Level.INFO,"Slave Scale Down = "+SCALE_DOWN_SLAVE_LOAD_THRESHOLD+" "+SCALE_DOWN_SLAVE_MEM_THRESHOLD,GlobalLogger.MANAGER_LOG_ID);

        ArrayList<Framework> newFrameworks = convertToFrameworkObjects(frameworkData);
        ArrayList<Slave> newSlaves = convertToSlaveObjects(slaveData);
        integrateFrameworks(newFrameworks);
        integrateSlaves(newSlaves);
        connect(frameworks,slaves,runsOnData);

        for(Framework f : frameworks)
        {
            if(f.isFilterSet())
            {
                f.setFilterTime(f.getFilterTime() - (int) (current_time - last_time));
            }
        }

        for(Slave s : slaves)
        {
            if(s.isFilterSet())
            {
                s.setFilterTime(s.getFilterTime() - (int) (current_time - last_time));
            }
        }

        if(noScaleUpFilterSet)
        {
           noScaleUpFilter = noScaleUpFilter - (current_time - last_time);
        }
        last_time = current_time;
        logger.log(Level.FINER,"Exiting fetch",GlobalLogger.MANAGER_LOG_ID);

        ArrayList<Node> nodes = new ArrayList<>();

        for (Slave slave : slaves)
        {
            OpenStackNode node = new OpenStackNode(newNodeFlavor);
            node.setHostname(slave.getHostname());
            node.setIp(slave.getIp());
            nodes.add(node);
        }

        return nodes;
    }

    @Override
    public ArrayList<Node> scaleUp()
    {

        logger.log(Level.FINER,"Entering Scale Up",GlobalLogger.MANAGER_LOG_ID);
        if(noScaleUpFilterSet)
        {
            if(noScaleUpFilter <= 0)
            {
                noScaleUpFilter = 0;
                noScaleUpFilterSet = false;
            }

            else
            {
                logger.log(Level.FINE,"In No Scale Up Mode",GlobalLogger.MANAGER_LOG_ID);
                return new ArrayList<>();
            }
        }

        ArrayList<Node> nodes = new ArrayList<>();
        double clusterMetrics[] = calculateClusterMetrics();

        logger.log(Level.INFO,"Cluster Metrics "+Arrays.toString(clusterMetrics),GlobalLogger.MANAGER_LOG_ID);

        if (clusterMetrics[CLUSTER_LOAD] > SCALE_UP_CLUSTER_LOAD_THRESHOLD || clusterMetrics[CLUSTER_FREE_MEM]/clusterMetrics[CLUSTER_TOT_MEM] < SCALE_UP_CLUSTER_MEM_THRESHOLD)
        {
            nodes.add(new OpenStackNode(newNodeFlavor));
            if(clusterMetrics[CLUSTER_LOAD] > SCALE_UP_CLUSTER_LOAD_THRESHOLD)
            {
                logger.log(Level.INFO,"Creating New Node since Cluster Load Threshold was crossed",GlobalLogger.MANAGER_LOG_ID);
            }
            else
            {
                logger.log(Level.INFO,"Creating New Node since Cluster Memory Threshold was crossed",GlobalLogger.MANAGER_LOG_ID);
            }
            return nodes;
        }

        ArrayList<Slave> slavesWithResourceCrunch = findSlavesWithResourceCrunch();

//        for(Slave slave : slavesWithResourceCrunch)
//        {
//            ArrayList<Framework> frameworksOnSlave = slave.getFrameworks_running();
//            for(Framework framework : frameworksOnSlave)
//            {
//                if(framework.getAllocated_slaves().size() == 1)
//                {
//                    nodes.add(new OpenStackNode("3"));
//
//                    return nodes;
//                }
//            }
//        }

        if(slavesWithResourceCrunch.size() > 0)
        {
            nodes.add(new OpenStackNode(newNodeFlavor));
            logger.log(Level.INFO,"Creating new node as resource crunch was detected on a slave",GlobalLogger.MANAGER_LOG_ID);
            return nodes;
        }

        ArrayList<Framework> underObservationFrameworks = findActiveFrameworksWithNoResources();
        boolean freeCPUSPresent = isFreeCPUPresent();


        if(!freeCPUSPresent && underObservationFrameworks.size() > 0)
        {
            nodes.add(new OpenStackNode(newNodeFlavor));
            logger.log(Level.INFO,"Creating new node as an active framework has no free cpu",GlobalLogger.MANAGER_LOG_ID);
            return nodes;
        }

        else
        {
            //Observe Framework for a while
            boolean createNewNode = false;
            for(Framework f : underObservationFrameworks)
            {
                if(!f.isFilterSet()) {
                    f.setFilterTime(FRAMEWORK_FILTER);
                    f.setFilterSet(true);
                }
                else
                {
                    if(f.getFilterTime() <= 0)
                    {
                        f.setFilterTime(0);
                        f.setFilterSet(false);
                        createNewNode = true;
                    }
                }
            }

            if(createNewNode)
            {
                nodes.add(new OpenStackNode(newNodeFlavor));
                logger.log(Level.INFO,"Creating new node as an active framework has no resources",GlobalLogger.MANAGER_LOG_ID);
                return nodes;
            }
        }

        return nodes;
    }


    @Override
    public ArrayList<Node> scaleDown()
    {
        ArrayList<Node> toBeDeleted = new ArrayList<>();
        double[] clusterMetrics = calculateClusterMetrics();

        if(slaves.size() <= MIN_SLAVES)
        {
            logger.log(Level.FINE,"Skipping since Min number of slaves are present",GlobalLogger.MANAGER_LOG_ID);
            return toBeDeleted;
        }

        for(Slave slave : slaves)
        {
            OpenStackNode node = new OpenStackNode(newNodeFlavor);
            node.setIp(slave.getHostname());


            if(slave.isFilterSet())
            {
                if(slave.getFilterTime() > 0)
                {
                    continue;
                }

                else
                {
                    slave.setFilterSet(false);
                    slave.setFilterTime(0);
                }
            }

            if(NO_DELETE_SLAVES.contains(slave.getHostname().toLowerCase()))
            {
                logger.log(Level.FINE,"Skipping Slave since it should not be deleted",GlobalLogger.MANAGER_LOG_ID);
                continue;
            }

            ArrayList<Framework> frameworksOnSlave = slave.getFrameworks_running();

            boolean canDelete = true;
            for (Framework f: frameworksOnSlave)
            {
                if(f.getAllocated_slaves().size() == 1)
                {
                    canDelete = false;
                }
            }

            if(!canDelete)
            {
                logger.log(Level.FINE,"Skipping as only one framework is running",GlobalLogger.MANAGER_LOG_ID);
                continue;
            }



            if(clusterMetrics[CLUSTER_LOAD] < SCALE_DOWN_CLUSTER_LOAD_THRESHOLD && clusterMetrics[CLUSTER_FREE_MEM]/clusterMetrics[CLUSTER_TOT_MEM] > SCALE_DOWN_CLUSTER_MEM_THRESHOLD)
            {
                if(slave.getLoad()/slave.getCpu() < SCALE_DOWN_SLAVE_LOAD_THRESHOLD || slave.getFree_mem()/slave.getTotal_mem() > SCALE_DOWN_SLAVE_MEM_THRESHOLD)
                {

                    logger.log(Level.INFO,"Deleting Node "+slave.getHostname(),GlobalLogger.MANAGER_LOG_ID);
                    toBeDeleted.add(node);
                    continue;
                }
            }

        }
        return toBeDeleted;
    }

    @Override
    public ArrayList<Node> requestResources(String parameters)
    {
        int equalIndex = parameters.indexOf('=');
        int newNodesCount = Integer.parseInt(parameters.substring(equalIndex+1,parameters.length()).trim());
        ArrayList<Node> newNodes = new ArrayList<>();
        for(int i=0;i<newNodesCount;i++)
        {
            newNodes.add(new OpenStackNode(newNodeFlavor));
        }
        return newNodes;
    }

    @Override
    public void notifyNewNodeCreation(Node node)  {
        OpenStackNode openStackNode = (OpenStackNode) node;

        String s4 = "sudo hostname "+ openStackNode.getHostname();
//        String s0 = "sudo sed -i '1s/^/nameserver 192.168.0.51\\n /' /etc/resolv.conf";
//        String s1 =  "sudo sed -i '1s/^/" + openStackNode.getIp() + " " + openStackNode.getHostname() + "\\n /' /etc/hosts";
        String s1 = "sudo sed -i '1s/^/"+hdfsIp+" mesos-hdfs-master\\n /' /etc/hosts";
        String s2 = "nohup ./hadoop-2.5.0-cdh5.2.0/bin/hadoop-daemon.sh start datanode &>/dev/null &";
        String s3 = "nohup mesos slave --master="+hdfsIp+":5050 --quiet &>/dev/null &";

        SshProxy proxy = new SshProxy(privateKey);

        while (true)
        {
            try {
                proxy.executeCommand(openStackNode.getIp(),"hostname");
                logger.log(Level.INFO,"New Node Ready",GlobalLogger.MANAGER_LOG_ID);
                break;
            }
            catch (Exception e)
            {
                proxy.closeSessions();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }



        try {
            int exit = proxy.executeCommand(openStackNode.getIp(),s4);
            logger.log(Level.INFO,"Executed "+s4+" with status "+exit,GlobalLogger.MANAGER_LOG_ID);
//            exit = proxy.executeCommand(openStackNode.getIp(),s0);
//            logger.log(Level.INFO,"Executed "+s0+" with status "+exit,GlobalLogger.MANAGER_LOG_ID);
            exit = proxy.executeCommand(openStackNode.getIp(),s1);
            logger.log(Level.INFO,"Executed "+s1+" with status "+exit,GlobalLogger.MANAGER_LOG_ID);
            exit = proxy.executeCommand(openStackNode.getIp(),s2);
            logger.log(Level.INFO,"Executed "+s2+" with status "+exit,GlobalLogger.MANAGER_LOG_ID);
            exit = proxy.executeCommand(openStackNode.getIp(),s3);
            logger.log(Level.INFO,"Executed "+s3+" with status "+exit,GlobalLogger.MANAGER_LOG_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.log(Level.INFO,"Finished Connecting Node to Mesos",GlobalLogger.MANAGER_LOG_ID);

        Slave slave = new Slave();
        slave.setHostname(openStackNode.getHostname());
        slave.setIp(openStackNode.getIp());
        slave.setFilterTime(SLAVE_NEW_FILTER);
        slave.setFilterSet(true);
        slaves.add(slave);
        noScaleUpFilter = 300000;
        noScaleUpFilterSet = true;
    }

    @Override
    public ArrayList<Node> receivedReleaseNodeRequest(String string) {

        JsonObject json = new Gson().fromJson(string,JsonObject.class);

        ArrayList<Node> releaseNodes = new ArrayList<>();

        int number = json.get("number").getAsInt();

        for(Slave slave : slaves)
        {
            if(NO_DELETE_SLAVES.contains(slave.getHostname()))
            {
                continue;
            }

            if (number == 0)
            {
                break;
            }

            OpenStackNode node = new OpenStackNode(newNodeFlavor);
            node.setHostname(slave.getHostname());
            node.setIp(slave.getIp());

            releaseNodes.add(node);
            number--;
        }

        return releaseNodes;
    }


    private double[] calculateClusterMetrics()
    {

        double tot_load = 0;
        double tot_free_mem = 0;
        double tot_tot_mem = 0;
        double tot_cpu = 0;
        double tot_allocated_cpu = 0;

        for(Slave slave : slaves)
        {
            tot_load +=  slave.getLoad()/slave.getCpu();
            tot_free_mem +=  slave.getFree_mem();
            tot_tot_mem +=  slave.getTotal_mem();
            tot_cpu +=  slave.getCpu();
            tot_allocated_cpu += slave.getAllocated_cpu();
        }


        tot_allocated_cpu = (tot_allocated_cpu/tot_cpu) * 100;

        tot_load = tot_load/slaves.size();

        double metrics[] = {tot_load,tot_free_mem,tot_tot_mem,tot_cpu,tot_allocated_cpu};

        return metrics;
    }

    private ArrayList<Framework> findActiveFrameworksWithNoResources()
    {
        ArrayList <Framework> frameworks = new ArrayList<>();
        for(Framework framework : this.frameworks)
        {
            if(framework.getCpu() == 0 && framework.isActive() && framework.getMemory() == 0)
            {
                logger.log(Level.FINE,"Found "+framework.getId()+" has no resources",GlobalLogger.MANAGER_LOG_ID);
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
            s.setAllocated_cpu(Double.parseDouble(row[SLAVE_ALLOCATED_CPU]));
            s.setFree_mem(Double.parseDouble(row[SLAVE_FREE_MEM]));
            s.setTotal_mem(Double.parseDouble(row[SLAVE_TOTAL_MEM]));
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

    private void integrateSlaves(ArrayList<Slave> newSlaves)
    {
        ArrayList<Slave> oldSlaves = slaves;
        slaves = new ArrayList<>();
        for(Slave newS : newSlaves)
        {
            Slave existingS = findSlaveWithHostname(newS.getHostname(),oldSlaves);
            if(existingS == null)
            {
                slaves.add(newS);
            }

            else
            {
                existingS.copy(newS);
                slaves.add(existingS);
            }
        }
    }

    private void integrateFrameworks(ArrayList<Framework> newFrameworks)
    {
        ArrayList<Framework> oldFrameworks = frameworks;
        frameworks = new ArrayList<>();
        for(Framework newF : newFrameworks)
        {
            Framework existingF = findFramework(newF.getId(),oldFrameworks);
            if(existingF == null)
            {
                frameworks.add(newF);
            }

            else
            {
                existingF.copy(newF);
                frameworks.add(existingF);
            }
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

    private Slave findSlaveWithHostname(String hostname, ArrayList<Slave> slaves)
    {
        for(Slave slave : slaves)
        {
            if(slave.getHostname().equals(hostname))
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

    private ArrayList<Slave> findSlavesWithResourceCrunch() {
        ArrayList<Slave> slaves = new ArrayList<>();

        for(Slave slave : this.slaves)
        {
            if(slave.getLoad()/slave.getCpu() > SCALE_UP_CLUSTER_LOAD_THRESHOLD || slave.getFree_mem()/slave.getTotal_mem() < SCALE_UP_CLUSTER_MEM_THRESHOLD)
            {
                logger.log(Level.FINE,"Resource Crunch Detected in "+slave.getHostname(),GlobalLogger.MANAGER_LOG_ID);
                slaves.add(slave);
            }
        }
        return slaves;
    }

}
