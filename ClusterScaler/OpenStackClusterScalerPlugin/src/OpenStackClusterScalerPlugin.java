import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by chemistry_sourabh on 3/4/16.
 */
public class OpenStackClusterScalerPlugin implements ClusterScalerPlugin {

    private static final String OPENSTACK_CLIENT_PATH = "ClusterScaler/OpenStackClusterScalerPlugin/OpenStackClient";
    private static final String LIST_FILE_NAME = "list.py";
    private static  final String CREATE_FILE_NAME = "create.py";
    private static final String DELETE_FILE_NAME = "delete.py";

    private ArrayList<MesosSlave> slaves;
    private int slaveCount = 0;

    private String password;

    private Logger logger = GlobalLogger.globalLogger;

    @Override
    public void setup()
    {
        logger.log(Level.FINER,"Entering setup()",GlobalLogger.MANAGER_LOG_ID);
        String output = "";

        if(password == null)
        {
            Console console = System.console();
            if(console == null)
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Enter password:");
                try {
                    password = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            else {
                password = Arrays.toString(console.readPassword("Enter password:"));
            }
        }

        try {
            Process p = Runtime.getRuntime().exec("python "+OPENSTACK_CLIENT_PATH+File.separator+LIST_FILE_NAME+" --password "+password);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            String s;

            while ((s = stdInput.readLine()) != null) {
                output += s;
            }

            p.waitFor();

            logger.log(Level.FINE,"list.py output = "+output,GlobalLogger.MANAGER_LOG_ID);

            Gson gson = new Gson();
            JsonArray json = gson.fromJson(output, JsonArray.class);

            slaves = convertToMesosSlaves(json);
            slaveCount = getLargestSlaveNumber() + 1;

            logger.log(Level.FINER,"Exiting setup()",GlobalLogger.MANAGER_LOG_ID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Node createNewNode(Node node)
    {
        logger.log(Level.INFO,"Creating new node",GlobalLogger.MANAGER_LOG_ID);
        OpenStackNode openStackNode = (OpenStackNode) node;
        MesosSlave slave = new MesosSlave();
        try {

            createNode(openStackNode);

            String output = "";
            int retry = 3;
            int c = 0;
            while(true) {
                output = "";

                JsonArray json = listNode("Spark-Slave-"+slaveCount);

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

    @Override
    public boolean deleteNode(Node node)
    {

        OpenStackNode node1 = (OpenStackNode) node;
        logger.log(Level.INFO,"Deleting node with IP "+node1.getIp());
        MesosSlave slave = findSlave(node1.getIp());

        if(slave == null)
        {
            return false;
        }


        node1.setId(slave.getNodeId());
        try {

            deleteNode(node1);

            while (true)
            {
               JsonArray array = listNode(slave.getHostname());

                if(array == null)
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

    private ArrayList<MesosSlave> convertToMesosSlaves(JsonArray array)
    {
        ArrayList<MesosSlave> slaves = new ArrayList<>();
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

    private MesosSlave findSlave(String ip)
    {
        for(MesosSlave slave : slaves)
        {
            if(slave.getIp().equals(ip))
            {
                return slave;
            }
        }
        return null;
    }

    private int getLargestSlaveNumber()
    {
        String firstHost = slaves.get(0).getHostname();
        int max = Integer.parseInt(""+firstHost.charAt(firstHost.length() - 1));

        ArrayList<MesosSlave> slaves1 = new ArrayList<>(slaves);
        slaves1.remove(0);

        for(MesosSlave slave : slaves1)
        {
            String hostname = slave.getHostname();
            int num = Integer.parseInt(""+hostname.charAt(hostname.length() - 1));
            if(num > max)
            {
                max = num;
            }
        }

        return max;
    }

    private void createNode(OpenStackNode openStackNode) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("python "+OPENSTACK_CLIENT_PATH+File.separator+CREATE_FILE_NAME+" --password "+password+" --name Spark-Slave-"+slaveCount+" --flavor "+openStackNode.getFlavor()+" --image 168274f7-9841-4a59-805b-abc44afbffeb --key-name Sourabh-OSX");

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        String s;
        // read the output from the command
        // read any errors from the attempted command

        while ((s = stdInput.readLine()) != null) {
            logger.log(Level.FINE,s,GlobalLogger.MANAGER_LOG_ID);
        }

        p.waitFor();
    }

    private JsonArray listNode(String name) throws IOException {
       Process  p = Runtime.getRuntime().exec("python "+OPENSTACK_CLIENT_PATH+File.separator+LIST_FILE_NAME+" --password "+password+" --name "+name);

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
        String s;
        String output = "";
        // read the output from the command
        // read any errors from the attempted command
//                while ((s = stdError.readLine()) != null) {
//                    System.out.println(s);
//                }

        while ((s = stdInput.readLine()) != null) {
            output += s;
        }
        logger.log(Level.FINE,"list.py output = "+output,GlobalLogger.MANAGER_LOG_ID);
        if (output.equals("") || output.equals("[]"))
        {
            return null;
        }
        Gson gson = new Gson();
        JsonArray json = gson.fromJson(output, JsonArray.class);
        return json;
    }

    private void deleteNode(OpenStackNode node) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("python "+OPENSTACK_CLIENT_PATH+File.separator+DELETE_FILE_NAME+" --password "+password+" --id "+node.getId());
        p.waitFor();
    }

}