import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 3/4/16.
 */
public class OpenStackClusterScalerPlugin implements ClusterScalerPlugin {

    private ArrayList<MesosSlave> slaves;
    private int slaveCount = 0;

    @Override
    public void setup()
    {
        String output = "";
        try {
            Process p = Runtime.getRuntime().exec("python python/list.py");

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));
            String s;
            // read the output from the command
            // read any errors from the attempted command
//            while ((s = stdError.readLine()) != null) {
//                System.out.println(s);
//            }

            while ((s = stdInput.readLine()) != null) {
                output += s;
            }

            while (p.isAlive());

            System.out.println(output);

            Gson gson = new Gson();
            JsonArray json = gson.fromJson(output, JsonArray.class);

            slaves = convertToMesosSlaves(json);
            slaveCount = getLargestSlaveNumber() + 1;
            System.out.println("OpenStack Setup Done");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Node createNewNode(Node node)
    {
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
//                    Thread.sleep(20000);
//                    slaveCount++;
                    createNode(openStackNode);
                    c++;
                    if(c > retry)
                    {
                        System.exit(0);
                    }
                }


                Thread.sleep(1000);
            }

            System.out.println("Created New Node");

            System.out.println(slave);



//            Thread.sleep(15000);

            String s0 = "sudo sed -i '1s/^/nameserver 192.168.0.51\\n /' /etc/resolv.conf";
            String s1 =  "sudo sed -i '1s/^/" + slave.getIp() + " " + slave.getHostname() + "\\n /' /etc/hosts";
            String s2 = "nohup ./hadoop-2.5.0-cdh5.2.0/bin/hadoop-daemon.sh start datanode &>/dev/null &";
            String s3 = "nohup mesos slave --master=master.mesos:5050 --quiet &>/dev/null &";

            SshProxy proxy = new SshProxy();

            while (true)
            {
                try {
                    proxy.executeCommand(slave.getIp(),"hostname");
                    System.out.println("Host ready");
                    break;
                }
                catch (Exception e)
                {
//                    e.printStackTrace();
                    proxy.closeSessions();
//                    System.out.println("Slave not yet ready "+slave.getIp());
                    Thread.sleep(1000);
                }
            }


            System.out.println(s0);
            System.out.println(s1);
            System.out.println(s2);
            System.out.println(s3);
            proxy.executeCommand(slave.getIp(),s0);
            proxy.executeCommand(slave.getIp(),s1);
            proxy.executeCommand(slave.getIp(),s2);
            proxy.executeCommand(slave.getIp(),s3);




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
        System.out.println("Deleted Node "+node1.getHostname());
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
               JsonArray array = listNode(slave.getHostname());

                if(array == null)
                {
                    break;
                }
            }

            Thread.sleep(6000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

    private MesosSlave findSlave(String name)
    {
        for(MesosSlave slave : slaves)
        {
            if(slave.getHostname().equals(name))
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
        Process p = Runtime.getRuntime().exec("python python/create.py --name Spark-Slave-"+slaveCount+" --flavor "+openStackNode.getFlavor()+" --image 168274f7-9841-4a59-805b-abc44afbffeb --key-name Sourabh-OSX");

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
        String s;
        // read the output from the command
        // read any errors from the attempted command
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }

        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        p.waitFor();
    }

    private JsonArray listNode(String name) throws IOException {
       Process  p = Runtime.getRuntime().exec("python python/list.py --name "+name);

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
        System.out.println(output);
        if (output.equals("") || output.equals("[]"))
        {
            return null;
        }
        Gson gson = new Gson();
        JsonArray json = gson.fromJson(output, JsonArray.class);
        return json;
    }

    private void deleteNode(OpenStackNode node) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("python python/delete.py --id "+node.getId());

        p.waitFor();
    }

}
