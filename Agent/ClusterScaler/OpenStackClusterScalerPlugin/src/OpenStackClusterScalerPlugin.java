import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <h1>OpenStackClusterScalerPlugin</h1>
 * Manages the Open Stack Cluster
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-04-03
 */
public class OpenStackClusterScalerPlugin implements ClusterScalerPlugin {

    private static final String LIST_FILE_NAME = "list.py";
    private static  final String CREATE_FILE_NAME = "create.py";
    private static final String DELETE_FILE_NAME = "delete.py";

    private ArrayList<MesosSlave> slaves;
    private int slaveCount = 0;

    private String username;
    private String password;

    private String keyname;
    private String imageName;
    private String clusterSecurityGroup;
    private String clusterNetworkName;
    private String clusterNetworkId;

    private String id;


    private Logger logger = GlobalLogger.globalLogger;

    /**
     * <h1>setup</h1>
     * @param config
     * @param nodes
     * Sets up all the given slave nodes.
     */
    @Override
    public void setup(Config config, ArrayList<Node> nodes)
    {
        logger.log(Level.FINER,"Entering setup()",GlobalLogger.MANAGER_LOG_ID);
        String output = "";

        keyname = config.getValueForKey("Key-Name");
        imageName = config.getValueForKey("Image-Name");

        String user = config.getValueForKey("Username");
        String pass = config.getValueForKey("Password");

        id = config.getValueForKey("Id");
        clusterSecurityGroup = config.getValueForKey("Cluster-Security-Group");
        clusterNetworkName = config.getValueForKey("Cluster-Network");
        clusterNetworkId = config.getValueForKey("Cluster-Network-Id");

        if(user != null)
        {
            this.username = user;
        }

        if(pass != null)
        {
            this.password = pass;
        }

        if(username == null)
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter Username: ");
            try {
                this.username = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(password == null)
        {
            Console console = System.console();
            if(console == null)
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Enter password:");
                try {
                    this.password = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            else {
                password = new String(console.readPassword("Enter password:"));
            }
        }

        try {
            OpenStackWrapper openStackWrapper = new OpenStackWrapper(username,password);
            ListCommand listCommand = new ListCommand(clusterNetworkName);
            openStackWrapper.getWorkerQueue().add(listCommand);
            new Thread(openStackWrapper).start();

            ArrayList<OpenStackNode> openStackNodes = (ArrayList<OpenStackNode>) openStackWrapper.getResponseQueue().take();

            slaves = convertToMesosSlaves(openStackNodes);
            slaveCount = getLargestSlaveNumber() + 1;

            logger.log(Level.FINER,"Exiting setup()",GlobalLogger.MANAGER_LOG_ID);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * <h1>createNewNode</h1>
     * @param node
     * @return Node
     * Creates a new node with current OpenStack
     * Pool.
     */
    @Override
    public Node createNewNode(Node node)
    {
        logger.log(Level.INFO,"Creating new node",GlobalLogger.MANAGER_LOG_ID);
        OpenStackNode openStackNode = (OpenStackNode) node;
        MesosSlave slave = new MesosSlave();
        try {

            createNode(openStackNode);

            int retry = 3;
            int c = 0;
            while(true) {

                ArrayList<OpenStackNode> nodes = listNode("Spark-Slave-"+id+"-"+slaveCount+".cloud");

                if(nodes.size() == 0)
                    continue;

                if(nodes.get(0).getStatus().toLowerCase().equals("active"))
                {

                    OpenStackNode obj = nodes.get(0);
                    slave.setHostname(obj.getHostname());
                    slave.setFlavor(obj.getFlavor());
                    slave.setNodeId(obj.getId());
                    slave.setIp(obj.getIp());
                    slaves.add(slave);
                    break;
                }

                else if(nodes.get(0).getStatus().toLowerCase().equals("error"))
                {
                    openStackNode.setId(nodes.get(0).getId());

                    deleteNode(openStackNode);
                    createNode(openStackNode);
                    c++;
                    if(c > retry)
                    {
                        System.exit(0);
                    }
                }


                Thread.sleep(1000);
            }


            logger.log(Level.INFO,"Created new node with IP "+slave.getIp(),GlobalLogger.MANAGER_LOG_ID);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        OpenStackNode newNode = new OpenStackNode(openStackNode.getFlavor());
        newNode.setHostname(slave.getHostname());
        newNode.setIp(slave.getIp());
        slaveCount++;
        return newNode;
    }

    /**
     * <h1>deleteNode</h1>
     * @param node
     * @return Boolean
     * True iff a node gets deleted.
     */
    @Override
    public boolean deleteNode(Node node)
    {

        OpenStackNode node1 = (OpenStackNode) node;
        logger.log(Level.INFO,"Deleting node with IP "+node1.getIp(),GlobalLogger.MANAGER_LOG_ID);
        MesosSlave slave = findSlave(node1.getHostname());

        if(slave == null)
        {
            return false;
        }


        node1.setId(slave.getNodeId());
        try {

            deleteNode(node1);

            while (true)
            {
               ArrayList<OpenStackNode> nodes = listNode(slave.getHostname());

                if(nodes.size() == 0)
                {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.log(Level.INFO,"Deleted Node "+node1.getIp(),GlobalLogger.MANAGER_LOG_ID);
        return true;
    }

    /**
     * <h1>convertToMesosSlaves</h1>
     * @param openStackNodes
     * @return ArrayList<MesosSlave>
     * Converts the given openstack nodes to
     * the current Cluster Slave Nodes.
     */
    private ArrayList<MesosSlave> convertToMesosSlaves(ArrayList<OpenStackNode> openStackNodes)
    {
        ArrayList<MesosSlave> slaves = new ArrayList<>();

        if(openStackNodes.size() == 0)
        {
            return slaves;
        }

        for(OpenStackNode node : openStackNodes)
        {
            MesosSlave slave = new MesosSlave();
            slave.setHostname(node.getHostname());
            slave.setFlavor(node.getFlavor());
            slave.setNodeId(node.getId());
            slave.setIp(node.getIp());
            slaves.add(slave);
        }
        return slaves;
    }

    /**
     * <h1>findSlave</h1>
     * @param hostname
     * @return MesosSlave
     * Returns the Slave with the given hostname.
     */
    private MesosSlave findSlave(String hostname)
    {
        for(MesosSlave slave : slaves)
        {
            if(slave.getHostname().equals(hostname))
            {
                return slave;
            }
        }
        return null;
    }

    /**
     * <h1>getLargestSlaveNumber</h1>
     * @return int
     * The latest Slave which has been added to the
     * cluster. The Slaves are numbered according
     * to a serial increasing number.
     */
    private int getLargestSlaveNumber()
    {
        String firstHost = null;
        try {
            firstHost = slaves.get(0).getHostname();
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }

        int max = Integer.parseInt(""+firstHost.charAt(firstHost.length() - 7));

        ArrayList<MesosSlave> slaves1 = new ArrayList<>(slaves);
        slaves1.remove(0);

        for(MesosSlave slave : slaves1)
        {
            String hostname = slave.getHostname();
            int num = Integer.parseInt(""+hostname.charAt(hostname.length() - 7));
            if(num > max)
            {
                max = num;
            }
        }

        return max;
    }

    /**
     * <h1>createNode</h1>
     * Creates a new node for the cluster from
     * the Openstack.
     * @param openStackNode
     * @throws IOException
     * @throws InterruptedException
     */
    private void createNode(OpenStackNode openStackNode) throws IOException, InterruptedException {

        OpenStackWrapper openStackWrapper = new OpenStackWrapper(username,password);
        CreateCommand openStackCommand = new CreateCommand();
        openStackCommand.setName("Spark-Slave-"+id+"-"+slaveCount+".cloud");
        openStackCommand.setSecurityGroup(clusterSecurityGroup);
        openStackCommand.setFlavor(openStackNode.getFlavor());
        openStackCommand.setImageName(imageName);
        openStackCommand.setKeyPair(keyname);
        openStackCommand.setNetwork(clusterNetworkId);
        openStackWrapper.getWorkerQueue().add(openStackCommand);
        Thread t = new Thread(openStackWrapper);
        t.start();
        openStackWrapper.getResponseQueue().take();
    }

    /**
     * <h1>listNode </h1>
     * Returns the list of node present in the cluster.
     * @param name
     * @return ArrayList<OpenStackNode>
     * @throws IOException
     */
    private ArrayList<OpenStackNode> listNode(String name) throws IOException {

        OpenStackWrapper openStackWrapper = new OpenStackWrapper(username,password);
        ListCommand openStackCommand = new ListCommand(clusterNetworkName);
        openStackCommand.setName(name);
        openStackWrapper.getWorkerQueue().add(openStackCommand);
        new Thread(openStackWrapper).start();

        try {
            return (ArrayList<OpenStackNode>) openStackWrapper.getResponseQueue().take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <h1>deleteNode</h1>
     * Deletes a node from the cluster.
     * @param node
     * @throws IOException
     * @throws InterruptedException
     */
    private void deleteNode(OpenStackNode node) throws IOException, InterruptedException {
        OpenStackWrapper openStackWrapper = new OpenStackWrapper(username,password);
        DeleteCommand deleteCommand = new DeleteCommand();
        deleteCommand.setId(node.getId());
        openStackWrapper.getWorkerQueue().add(deleteCommand);
        new Thread(openStackWrapper).start();
    }

}
