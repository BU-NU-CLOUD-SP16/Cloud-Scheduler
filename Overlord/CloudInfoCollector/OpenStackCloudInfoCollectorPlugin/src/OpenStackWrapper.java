import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.*;
import org.openstack4j.model.network.Network;
import org.openstack4j.openstack.OSFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Praveen on 3/26/2016.
 */
public class OpenStackWrapper implements Runnable {

    private static final String OS_AUTH_URL = "https://keystone.kaizen.massopencloud.org:5000/v2.0";
    private static final String OS_TENANT_NAME = "Cloud Scheduler";
    private static final String OS_REGION_NAME = "MOC_Kaizen";

    private String username;
    private String password;
    private String keyPair;
    private String imageName;
    private String flavor;

    private OSClient osClientHandle;
    private Flavor nodeFlavor;
    private Image nodeImage;
    private Keypair osKeyPair;

    public LinkedBlockingQueue<ListCommand> getWorkerQueue() {
        return workerQueue;
    }

    private final LinkedBlockingQueue<ListCommand> workerQueue;

    private LinkedBlockingQueue<Object> responseQueue;

    public OpenStackWrapper(String username,String password)
    {
        this.workerQueue = new LinkedBlockingQueue<>();
        this.responseQueue = new LinkedBlockingQueue<>();
        this.username = username;
        this.password = password;
    }

    public LinkedBlockingQueue<Object> getResponseQueue() {
        return responseQueue;
    }


    public void initializeOpenStackClient(){
        this.osClientHandle = OSFactory.builder()
                .endpoint(OS_AUTH_URL)
                .credentials(username,password)
                .tenantName(OS_TENANT_NAME)
                .authenticate();

        this.osClientHandle.useRegion(OS_REGION_NAME);
    }


    private ArrayList<String> getNetworks()
    {
        List<? extends Network> networks = this.osClientHandle.networking().network().list();

        ArrayList<String> names = new ArrayList<>();

        for(Network network : networks)
        {
           names.add(network.getName());
        }
        return names;
    }

    public ArrayList<Node> listNodes(ListCommand command){
        // List all Servers
        ArrayList<Node> serverIdList = new ArrayList<>();

        if(command.getName() != null)
        {
            Node node = listNode(command);
            if (node != null)
                serverIdList.add(node);
            responseQueue.add(serverIdList);
            return serverIdList;
        }

        ArrayList<String> networkNames = getNetworks();

        List<? extends Server> servers = this.osClientHandle.compute().servers().list();

        for (Server server: servers ) {

            if(server.getName().toLowerCase().contains("slave")) {
                Node node = new Node();
                node.setName(server.getName());
                node.setId(server.getId());
                node.setFlavor(server.getFlavor().getId());
                node.setStatus(server.getStatus().value());
                for (String name : networkNames) {
                    try {
                        node.setIp(server.getAddresses().getAddresses(name).get(0).getAddr());
                        serverIdList.add(node);
                        break;
                    } catch (Exception e) {

                    }
                }
            }
        }

        responseQueue.add(serverIdList);
        return serverIdList;
    }

    public Node listNode(ListCommand command)
    {
        List<? extends Server> servers = this.osClientHandle.compute().servers().list();

        ArrayList<String> names = getNetworks();

        for (Server server : servers)
        {
            if(server.getName().equals(command.getName()))
            {
                Node node = new Node();
                node.setName(server.getName());
                node.setId(server.getId());
                node.setFlavor(server.getFlavor().getId());
                node.setStatus(server.getStatus().value());

                for (String name : names) {
                    try {
                        node.setIp(server.getAddresses().getAddresses(name).get(0).getAddr());
                        return node;
                    } catch (Exception ex) {

                    }
                }
            }
        }
        return  null;
    }



    @Override
    public void run() {
        initializeOpenStackClient();

            try {
                ListCommand command = this.workerQueue.take();

                listNodes(command);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}
