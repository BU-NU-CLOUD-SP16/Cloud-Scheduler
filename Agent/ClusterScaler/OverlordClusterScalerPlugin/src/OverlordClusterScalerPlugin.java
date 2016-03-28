import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by chemistry_sourabh on 3/25/16.
 */
public class OverlordClusterScalerPlugin implements ClusterScalerPlugin  {

    private  OverlordCommunicator communicator;


    private ArrayList<MesosSlave> slaves;
    private int slaveCount = 0;

    private String username;
    private String password;


    private Logger logger = GlobalLogger.globalLogger;

    private  String privateKey;

    public OverlordClusterScalerPlugin() {
        communicator = new OverlordCommunicator();
        communicator.register();
    }

    @Override
    public void setup(Config config)
    {
        logger.log(Level.FINER,"Entering setup()",GlobalLogger.MANAGER_LOG_ID);
        String output = "";



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

            output = communicator.list();
            logger.log(Level.FINE,"list output = "+output,GlobalLogger.MANAGER_LOG_ID);

            Gson gson = new Gson();
            JsonArray json = gson.fromJson(output, JsonArray.class);

            slaves = convertToMesosSlaves(json);
            slaveCount = getLargestSlaveNumber() + 1;

            logger.log(Level.FINER,"Exiting setup()",GlobalLogger.MANAGER_LOG_ID);


    }

    @Override
    public Node createNewNode(Node node)
    {
        logger.log(Level.INFO,"Creating new node",GlobalLogger.MANAGER_LOG_ID);
        OpenStackNode openStackNode = (OpenStackNode) node;
        MesosSlave slave = new MesosSlave();
        try {

            communicator.createNode("Spark-Slave-"+slaveCount+".cloud",openStackNode.getFlavor());

            String output = "";
            int retry = 3;
            int c = 0;

            Gson gson = new Gson();

            while(true) {
                output = "";

                JsonArray json = gson.fromJson(communicator.list("Spark-Slave-"+slaveCount+".cloud"),JsonArray.class);

                if(json.get(0).getAsJsonObject().get("status").getAsString().toLowerCase().equals("active"))
                {

                    JsonElement obj = json.get(0);
                    slave.setHostname(obj.getAsJsonObject().get("name").getAsString());
                    slave.setFlavor(obj.getAsJsonObject().get("flavor").getAsJsonObject().get("id").getAsString());
                    slave.setNodeId(obj.getAsJsonObject().get("id").getAsString());
                    slave.setIp(obj.getAsJsonObject().get("ip").getAsString());
                    slaves.add(slave);
                    break;
                }

                else if(json.get(0).getAsJsonObject().get("status").getAsString().toLowerCase().equals("error"))
                {
                    openStackNode.setId(json.get(0).getAsJsonObject().get("id").getAsString());

                    communicator.deleteNode(openStackNode.getId());
                    communicator.createNode("Spark-Slave-"+slaveCount+".cloud",openStackNode.getFlavor());
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
            communicator.deleteNode(node1.getId());
            Gson gson = new Gson();
            while (true)
            {
                JsonArray array = gson.fromJson(communicator.list(slave.getHostname()),JsonArray.class);

                if(array == null)
                {
                    break;
                }
            }
        logger.log(Level.INFO,"Deleted Node "+node1.getIp(),GlobalLogger.MANAGER_LOG_ID);
        return true;
    }

    private void disconnectNode(String hostname) {
        SshProxy proxy = new SshProxy(privateKey);

        try {
            proxy.executeCommand(hostname,"hadoop-daemon.sh stop datanode");
            proxy.executeCommand(hostname,"pkill mesos-slave");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
            slave.setFlavor(obj.getAsJsonObject().get("flavor").getAsJsonObject().get("id").getAsString());
            slave.setNodeId(obj.getAsJsonObject().get("id").getAsString());
            slave.setIp(obj.getAsJsonObject().get("ip").getAsString());
            slaves.add(slave);
        }
        return slaves;
    }

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
