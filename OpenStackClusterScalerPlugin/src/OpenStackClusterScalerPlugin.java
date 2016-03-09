import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
        try {
            Process p = Runtime.getRuntime().exec("python python/list.py");

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


            FileReader fr = new FileReader(new File("servers.json"));

            Gson gson = new Gson();
            JsonArray json = gson.fromJson(fr, JsonArray.class);

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


            slaveCount++;

            System.out.println("Created New Node");

        } catch (IOException e) {
            e.printStackTrace();
        }

        OpenStackNode newNode = new OpenStackNode(openStackNode.getFlavor());
        newNode.setHostname("Spark-Slave-"+slaveCount);
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
            slaves.add(slave);
        }
        return slaves;
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
