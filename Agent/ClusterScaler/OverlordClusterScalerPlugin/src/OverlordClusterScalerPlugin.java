import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <h1>OverlordClusterScalerPlugin</h1>
 * Contains OpenStack Functionalities in with
 * the help of overlord.
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-03-25
 */
public class OverlordClusterScalerPlugin implements ClusterScalerPlugin  {

    private  OverlordCommunicator communicator;


    private ArrayList<MesosSlave> slaves;
    private int slaveCount = 0;

    private String username;
    private String password;


    private Logger logger = GlobalLogger.globalLogger;

    private  String privateKey;

    private  boolean registered = false;

    private String id;

    /**
     * <h1>OverlordClusterScalerPlugin</h1>
     * Constructor.
     */
    public OverlordClusterScalerPlugin() {
        communicator = new OverlordCommunicator();

    }

    /**
     * <h1>setup</h1>
     * Sets up the nodes given to the cluster.
     * @param config
     * @param nodes
     */
    @Override
    public void setup(Config config, ArrayList<Node> nodes)
    {
        logger.log(Level.FINER,"Entering setup()",GlobalLogger.MANAGER_LOG_ID);
        String output = "";

        id = config.getValueForKey("Id");

        if (!registered)
        {
            communicator.register(id,config.getValueForKey("Port"),nodes);
            registered = true;
        }

        String user = config.getValueForKey("Username");
        String pass = config.getValueForKey("Password");

        privateKey = config.getValueForKey("SSH-Private-Key");



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

        communicator.setUsername(username);
        communicator.setPassword(password);

        output = communicator.list(id);
        logger.log(Level.FINE,"list output = "+output,GlobalLogger.MANAGER_LOG_ID);

        Gson gson = new Gson();
        JsonArray json = gson.fromJson(output, JsonArray.class);

        slaves = convertToMesosSlaves(json);
        slaveCount = getLargestSlaveNumber() + 1;

        logger.log(Level.FINER,"Exiting setup()",GlobalLogger.MANAGER_LOG_ID);


    }

    /**
     * <h1>createNewNode</h1>
     * @param node
     * @return Node
     * Creates a new node and returns the
     * node which has just been created.
     */
    @Override
    public Node createNewNode(Node node)
    {
        logger.log(Level.INFO,"Creating new node",GlobalLogger.MANAGER_LOG_ID);
        OpenStackNode openStackNode = (OpenStackNode) node;
        MesosSlave slave = new MesosSlave();
        try {

           communicator.createNode(id,openStackNode.getFlavor());

            String output = "";
            int retry = 3;
            int c = 0;

            Gson gson = new Gson();

            String newNodeName = null;

            while (true)
            {
                JsonArray array = new Gson().fromJson(communicator.list(id),JsonArray.class);

                if(array.size() > slaves.size())
                {
                    for(JsonElement element : array)
                    {

                       MesosSlave slave1 = findSlave(element.getAsJsonObject().get("name").getAsString());

                       if(slave1 == null)
                       {
                           newNodeName = element.getAsJsonObject().get("name").getAsString();
                           break;
                       }
                    }

                    if(newNodeName != null)
                    {
                        break;
                    }
                }
            }

            while(true) {
                output = "";

                JsonArray json = gson.fromJson(communicator.list(id,newNodeName),JsonArray.class);

                if(json == null)
                {
                    continue;
                }

                if(json.size() == 0)
                {
                    continue;
                }

                if(json.get(0).getAsJsonObject().get("status").equals("null"))
                {
                    continue;
                }

                if(json.get(0).getAsJsonObject().get("status").getAsString().equalsIgnoreCase("active"))
                {

                    JsonElement obj = json.get(0);
                    slave.setHostname(obj.getAsJsonObject().get("name").getAsString());
                    slave.setFlavor(obj.getAsJsonObject().get("flavor").getAsString());
                    slave.setNodeId(obj.getAsJsonObject().get("id").getAsString());
                    slave.setIp(obj.getAsJsonObject().get("ip").getAsString());
                    slaves.add(slave);
                    break;
                }

                else if(json.get(0).getAsJsonObject().get("status").getAsString().toLowerCase().equals("error"))
                {
                    openStackNode.setId(json.get(0).getAsJsonObject().get("id").getAsString());

                    communicator.deleteNode(id,openStackNode.getId());
                    communicator.createNode(id,openStackNode.getFlavor());
                    c++;
                    if(c > retry)
                    {
                        System.exit(0);
                    }
                }


                Thread.sleep(1000);
            }


            logger.log(Level.INFO,"Created new node with IP "+slave.getIp(),GlobalLogger.MANAGER_LOG_ID);


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
     * Deletes the node and returns true if there
     * was any slave to be deleted.
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

        disconnectNode(node1.getHostname());
        communicator.deleteNode(id,node1.getId());
        logger.log(Level.INFO,"Deleted Node "+node1.getIp(),GlobalLogger.MANAGER_LOG_ID);
        return true;
    }

    /**
     * <h1>disconnectNode</h1>
     * Disonnects the current node from the current
     * cluster where it is being used.
     * @param hostname
     */
    private void disconnectNode(String hostname) {
        SshProxy proxy = new SshProxy(privateKey);

        try {
            proxy.executeCommand(hostname,"hadoop-daemon.sh stop datanode");
            proxy.executeCommand(hostname,"pkill mesos-slave");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <h1>convertToMesosSlaves</h1>
     * @param array
     * @return ArrayList<MesosSlave>
     * Converts the given openstack nodes to
     * the current Cluster Slave Nodes.
     */
    private ArrayList<MesosSlave> convertToMesosSlaves(JsonArray array)
    {
        ArrayList<MesosSlave> slaves = new ArrayList<>();

        if(array == null)
        {
            return slaves;
        }

        for(JsonElement obj : array)
        {
            MesosSlave slave = new MesosSlave();
            slave.setHostname(obj.getAsJsonObject().get("name").getAsString());
            slave.setFlavor(obj.getAsJsonObject().get("flavor").getAsString());
            slave.setNodeId(obj.getAsJsonObject().get("id").getAsString());
            slave.setIp(obj.getAsJsonObject().get("ip").getAsString());
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


}
