import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <h1>MesosElasticityPlugin</h1>
 * This class maintains and is the reason for
 * any scaling up or down of the nodes based on
 * data received from Mesos.
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-03-02
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
    private static final int DECREMENT_TIME = 5000;

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
    private String sshHost;
    private int sshPort;

    /**
     * <h1>MesosElasticityPlugin Constructor</h1>
     * Creates new objects for framework and slave
     */
    public MesosElasticityPlugin()
    {
        frameworks = new ArrayList<>();
        slaves = new ArrayList<>();
        logger = GlobalLogger.globalLogger;
    }

    /**
     * <h1>fetch</h1>
     * Takes in the list of queries as the first parameter
     * and the config values as the second
     * Should be used to fetch required data from db and
     * policy info from files, and will be called at beginning
     * of each command
     * @param data First parameter of type Arraylist<Data>
     * @param config Second parameter of type Config
     * @return ArrayList<Node>
     */
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
        sshHost = config.getValueForKey("SSH-Host");
        sshPort = Integer.parseInt(config.getValueForKey("SSH-Port"));
        JsonArray json = new Gson().fromJson(config.getValueForKey("No-Delete-Slaves"),JsonArray.class);

        NO_DELETE_SLAVES = new ArrayList<String>();

        for (JsonElement  element : json)
        {
            NO_DELETE_SLAVES.add(element.getAsString().toLowerCase());
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
                f.setFilterTime(f.getFilterTime() - (int) DECREMENT_TIME);
            }
        }

        for(Slave s : slaves)
        {
            if(s.isFilterSet())
            {
                s.setFilterTime(s.getFilterTime() - (int) DECREMENT_TIME);
                logger.log(Level.INFO,"Slave "+s.getHostname()+" filter = "+s.getFilterTime(),GlobalLogger.MANAGER_LOG_ID);
            }
        }

        if(noScaleUpFilterSet)
        {
           noScaleUpFilter = noScaleUpFilter - DECREMENT_TIME;
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

    /**
     * <h1>getNodes</h1>
     * Returns the list of nodes currently used
     * by all the slaves of the agent.
     * @return ArrayList<Nodes> List of Nodes
     */
    @Override
    public ArrayList<Node> getNodes()
    {
        ArrayList<Node> nodes = new ArrayList<>();

        for (Slave slave : slaves)
        {
            OpenStackNode node = new OpenStackNode();
            node.setHostname(slave.getHostname());
            node.setIp(slave.getIp());
            nodes.add(node);
        }
        return nodes;
    }

    /**
     * <h1>getFrameworkNames</h1>
     * Returns the list of Framework Names.
     * @return ArrayList<String> List of Framework names
     */
    @Override
    public ArrayList<String> getFrameworkNames()
    {
        ArrayList<String> names = new ArrayList<>();
        for(Framework framework : frameworks)
        {
            names.add(framework.getName());
        }

        return names;
    }

    /**
     * <h1>scaleUp</h1>
     * Scales up a node and provides the
     * list of nodes.
     * @return ArrayList<Node> List of Nodes
     */
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


    /**
     * <h1>scaleDown</h1>
     * Scales down a node based on a reason
     * and return the list of nodes
     * @return ArrayList<Node>
     */
    @Override
    public ArrayList<Node> scaleDown()
    {
        ArrayList<Node> toBeDeleted = new ArrayList<>();
        double[] clusterMetrics = calculateClusterMetrics();


        for (Slave slave : slaves)
        {
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
        }

        if(slaves.size() <= MIN_SLAVES)
        {
            logger.log(Level.FINE,"Skipping since Min number of slaves are present",GlobalLogger.MANAGER_LOG_ID);
            return toBeDeleted;
        }

        for(Slave slave : slaves)
        {
            OpenStackNode node = new OpenStackNode(newNodeFlavor);
            node.setHostname(slave.getHostname());
            node.setIp(slave.getIp());

            if (slave.isFilterSet() && slave.getFilterTime() > 0) continue;

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

    /**
     * <h1>requestResources</h1>
     * Dynamic request to add a new node
     * based on the given parameter and
     * returns the list of nodes
     * @param parameters
     * @return ArrayList<Node>
     */
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
        String s1 = "sudo sed -i '1s/^/"+hdfsIp+" mesos-hdfs-master\\n /' /etc/hosts";
        String s2 = "nohup hadoop-daemon.sh start datanode &>/dev/null &";
        String s3 = "nohup sudo mesos slave --master="+hdfsIp+":5050 --quiet --hadoop_home='/home/ubuntu/hadoop-2.5.0-cdh5.2.0' &>/dev/null &";
        String s5 = "sudo service mesos-master stop";
        String s6 = "sudo service mesos-slave stop";
        String s7 = "sudo service ganglia-monitor stop && sudo service gmetad stop && sudo service apache2 stop";
        String s8 = "sudo perl -i -p0e 's/\\/\\*\\nudp_send_channel {.*?#mcast_join = 239.2.11.71.*?host = localhost.*?port = 8649 .*?ttl = 1 .*?}.*?\\*\\/\\n/udp_send_channel {\\nhost = "+hdfsIp+"\\nport=8649\\nttl=1\\n}\\n/s' /etc/ganglia/gmond.conf";
        String s9 = "sudo service ganglia-monitor start";


        String s = "("+s4 + "; "+s1+"; "+s2+ "; "+s5+"; "+s6+"; "+s3+") &";

        SshProxy proxy = new SshProxy(sshHost,sshPort,privateKey);

        int timeout = 5000;
        while (true)
        {
            try {
                proxy.executeCommand(openStackNode.getIp(),"hostname",timeout);
                logger.log(Level.INFO,"New Node Ready",GlobalLogger.MANAGER_LOG_ID);
                Thread.sleep(1000);
                break;
            }
            catch (Exception e)
            {
                proxy.closeSessions();
                logger.log(Level.SEVERE,e.getMessage(),GlobalLogger.MANAGER_LOG_ID);
                if (e.getMessage().toLowerCase().contains("no route") || e.getMessage().toLowerCase().contains("connection refused"))
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    continue;
                }

                else
                {
                    break;
                }
//                if (e.getMessage().equalsIgnoreCase("timeout: socket is not established"))
//                {
//                    timeout = 40000;
//                    logger.log(Level.INFO,"New Timeout = "+timeout,GlobalLogger.MANAGER_LOG_ID);
//                }
            }
        }

        int exit;
        exit = tryExecutingForever(proxy,openStackNode.getIp(),s);
        logger.log(Level.INFO, "Executed " + s + " with status " + exit, GlobalLogger.MANAGER_LOG_ID);
//        exit = tryExecutingForever(proxy,openStackNode.getIp(),s1);
//        logger.log(Level.INFO,"Executed "+s1+" with status "+exit,GlobalLogger.MANAGER_LOG_ID);
//        exit = tryExecutingForever(proxy,openStackNode.getIp(),s2);
//        logger.log(Level.INFO,"Executed "+s2+" with status "+exit,GlobalLogger.MANAGER_LOG_ID);
//        exit = tryExecutingForever(proxy,openStackNode.getIp(),s5);
//        logger.log(Level.INFO,"Executed "+s5+" with status "+exit,GlobalLogger.MANAGER_LOG_ID);
//        exit = tryExecutingForever(proxy,openStackNode.getIp(),s6);
//        logger.log(Level.INFO,"Executed "+s6+" with status "+exit,GlobalLogger.MANAGER_LOG_ID);
////        exit = tryExecutingForever(proxy,openStackNode.getIp(),s7);
////        logger.log(Level.INFO,"Executed "+s7+" with status "+exit,GlobalLogger.MANAGER_LOG_ID);
////        exit = tryExecutingForever(proxy,openStackNode.getIp(),s8);
////        logger.log(Level.INFO,"Executed "+s8+" with status "+exit,GlobalLogger.MANAGER_LOG_ID);
////        exit = tryExecutingForever(proxy,openStackNode.getIp(),s9);
////        logger.log(Level.INFO,"Executed "+s9+" with status "+exit,GlobalLogger.MANAGER_LOG_ID);
//        exit = tryExecutingForever(proxy,openStackNode.getIp(),s3);
//        logger.log(Level.INFO,"Executed "+s3+" with status "+exit,GlobalLogger.MANAGER_LOG_ID);


//            exit = proxy.executeCommand(openStackNode.getIp(),s0);
//            logger.log(Level.INFO,"Executed "+s0+" with status "+exit,GlobalLogger.MANAGER_LOG_ID);

        logger.log(Level.INFO,"Finished Connecting Node to Mesos",GlobalLogger.MANAGER_LOG_ID);

        noScaleUpFilter = 300000;
        noScaleUpFilterSet = true;
    }

    private int tryExecutingForever(SshProxy proxy,String ip, String command) {
        int exit = 0;
        int timeout = 60000;
        while (true) {
            try {
                exit = proxy.executeCommand(ip, command,timeout);
//                Thread.sleep(1000);
                break;
            } catch (Exception ex) {
                proxy.closeSessions();
                logger.log(Level.SEVERE,ex.getMessage(),GlobalLogger.MANAGER_LOG_ID);
                if (ex.getMessage().equalsIgnoreCase("timeout: socket is not established"))
                {
                    timeout += 5000;
                    logger.log(Level.INFO,"New Timeout = "+timeout,GlobalLogger.MANAGER_LOG_ID);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return exit;
    }

    /**
     * <h1>receivedReleaseNodeRequest</h1>
     * Provides the list of Nodes which were released
     * @param string
     * @return ArrayList<Node> List of nodes which are released
     */
    @Override
    public ArrayList<Node> receivedReleaseNodeRequest(String string) {

        JsonObject json = new Gson().fromJson(string,JsonObject.class);

        ArrayList<Node> releaseNodes = new ArrayList<>();

        int number = json.get("number").getAsInt();

        for(Slave slave : slaves)
        {
            if(NO_DELETE_SLAVES.contains(slave.getHostname().toLowerCase()))
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

    /**
     * <h1>receivedCreateNodeResponse</h1>
     * Returns a list of nodes which are to be added
     * to the existing frameworks
     * @param jsonString
     * @return ArrayList<Node> The new list of nodes to be added to the agent.
     */
    @Override
    public ArrayList<Node> receivedCreateNodeResponse(String jsonString)
    {
        JsonObject json = new Gson().fromJson(jsonString,JsonObject.class);

        ArrayList<Node> createNodes = new ArrayList<>();

        int number = json.get("number").getAsInt();

        for (int i = 0; i < number;i++)
        {
            OpenStackNode openStackNode = new OpenStackNode(newNodeFlavor);
            createNodes.add(openStackNode);
        }

        return createNodes;

    }


    /**
     * <h1>calculateClusterMetrics</h1>
     * Calculates the cluster metrics from the nodes.
     * This data is received from Mesos Endpoints of Slave nodes.
     * @return double[] An array of the below metrics
     * {total load,total free mem,total mem,total cpu,total allocated cpu}
     */
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

    /**
     * <h1>findActiveFrameworksWithNoResources</h1>
     * Provides the list of Frameworks which do not have
     * any resources to work upon.
     * @return ArrayList<Framework> List of Frameworks without
     * any resources
     */
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

    /**
     * <h1>isFreeCPUPresent</h1>
     * Returns true if there are free CPUS
     * available for any Slave.
     * @return Boolean
     */
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

    /**
     * <h1>convertToFrameworkObjects</h1>
     * Takes the Data from the Framework table
     * and converts it into an object and returns
     * the list of all the rows of Framework table
     * in a list of objects.
     * @param frameworkData
     * @return ArrayList<Framework> Returns List of
     * Framework Objects
     */
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

    /**
     * <h1>convertToSlaveObjects</h1>
     * Takes the Data from the Slave table
     * and converts it into an object and returns
     * the list of all the rows of Slave table
     * in a list of Slave objects.
     * @param slaveData of Data type
     * @return ArrayList<Slave> Returns List of
     * Slave Objects
     */
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

    /**
     * <h1>connect</h1>
     * Using The RunsOnData table, we connect
     * the slaves with their current
     * respective frameworks.
     * @param frameworks ArrayList<Frameworks>
     * @param slaves ArrayList<Frameworks>
     * @param runsOnData Data
     */
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

    /**
     * <h1>integrateSlaves</h1>
     * Adds newly added slaves to the
     * main list of slaves.
     * @param newSlaves ArralList<Slave>
     */
    private void integrateSlaves(ArrayList<Slave> newSlaves)
    {
        ArrayList<Slave> oldSlaves = slaves;
        slaves = new ArrayList<>();
        for(Slave newS : newSlaves)
        {
            Slave existingS = findSlaveWithHostname(newS.getHostname(),oldSlaves);
            if(existingS == null)
            {
                newS.setFilterTime(SLAVE_NEW_FILTER);
                newS.setFilterSet(true);
                slaves.add(newS);
            }

            else
            {
                existingS.copy(newS);
                slaves.add(existingS);
            }
        }
    }

    /**
     * <h1>integrateFrameworks</h1>
     * Adds newly added Frameworks to the
     * existing List of Frameworks.
     * @param newFrameworks ArrayList<Framework>
     */
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

    /**
     * <h1>findSlave</h1>
     * Returns the Slave object using the
     * slave id from the list of existing Slaves.
     * Returns null if not available.
     * @param id type-> String
     * @param slaves type-> ArrayList<Slave>
     * @return Slave
     */
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

    /**
     * <h1>findSlaveWithHostname</h1>
     * Returns the Slave object using the
     * slave hostname from the list of existing Slaves.
     * Returns null if not available.
     * @param hostname type->String
     * @param slaves type->ArrayList<Slave>
     * @return Slave
     */
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

    /**
     * <h1>findFramework</h1>
     * finds the framework from the given
     * framework id and the list of
     * Frameworks.
     * Returns null if not found.
     * @param id type-> String
     * @param frameworks type-> ArrayList<Framework>
     * @return Framework
     */
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

    /**
     * <h1>findSlavesWithResourceCrunch</h1>
     * Returns the list of Slaves with resource crunch.
     * @return ArrayList<Slave> having resource crunch.
     */
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
