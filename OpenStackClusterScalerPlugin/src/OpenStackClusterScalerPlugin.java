import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jcabi.ssh.SSH;
import com.jcabi.ssh.Shell;
import org.omg.SendingContext.RunTime;

import java.io.*;
import java.net.URL;
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

            while (p.isAlive());

            String output = "";
            while(true) {
                output = "";
                p = Runtime.getRuntime().exec("python python/list.py --name Spark-Slave-" + slaveCount);

                stdInput = new BufferedReader(new
                        InputStreamReader(p.getInputStream()));

                stdError = new BufferedReader(new
                        InputStreamReader(p.getErrorStream()));
                // read the output from the command
                // read any errors from the attempted command
//                while ((s = stdError.readLine()) != null) {
//                    System.out.println(s);
//                }

                while ((s = stdInput.readLine()) != null) {
                    output += s;
                }
                System.out.println(output);
                Gson gson = new Gson();
                JsonArray json = gson.fromJson(output, JsonArray.class);


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

                while (p.isAlive());

                Thread.sleep(1000);
            }

            System.out.println("Created New Node");

            System.out.println(slave);



            Thread.sleep(10000);
            System.out.println("Creating Script");
            FileWriter fw = new FileWriter("script.sh");
            String s1 = "ssh -o StrictHostKeyChecking=no ubuntu@" + slave.getIp() + " \"sudo sed -i '1s/^/" + slave.getIp() + " " + slave.getHostname() + "\\n /' /etc/hosts\"\n";
            String s2 = "ssh -o StrictHostKeyChecking=no ubuntu@" + slave.getIp() + " './hadoop-2.5.0-cdh5.2.0/bin/hadoop-daemon.sh start datanode &'\n";
            String s3 = "ssh -o StrictHostKeyChecking=no ubuntu@" + slave.getIp() + " 'mesos slave --master=192.168.0.103:5050 --no-hostname_lookup --quiet &'\n";
            fw.write(s1+s2+s3);
            fw.close();
            System.out.println(s1+s2+s3);
            p = Runtime.getRuntime().exec("python python/setup.py");


             stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

             stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            // read the output from the command
            // read any errors from the attempted command


            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

            while(p.isAlive());


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        OpenStackNode newNode = new OpenStackNode(openStackNode.getFlavor());
        newNode.setHostname("Spark-Slave-"+slaveCount);
        slaveCount++;
        return newNode;
    }

    @Override
    public boolean deleteNode(Node node)
    {
        System.out.println("Deleted Node");
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

}
