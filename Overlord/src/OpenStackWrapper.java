import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.*;
import org.openstack4j.openstack.OSFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Praveen on 3/26/2016.
 */
public class OpenStackWrapper implements Runnable {

    private static final String OS_AUTH_URL = "https://keystone.kaizen.massopencloud.org:5000/v2.0";
    private static final String OS_TENANT_NAME = "Cloud Scheduler";
    private static final String OS_REGION_NAME = "MOC_Kaizen";
    private static final String OS_USER_NAME = "bollapragada.s@husky.neu.edu";
    private static final String OS_PASSWORD = "Soumya123$";
    private static final String OS_NODE_FLAVOR = "4";
    private static final String OS_IMAGE_ID = "9b9757a0-df69-4c63-bcf7-6cfa84ad12e3";
    private static final String OS_KEY_PAIR_NAME = "Sourabh-OSX";


    private OSClient osClientHandle;
    private Flavor nodeFlavor;
    private Image nodeImage;
    private Keypair osKeyPair;

    public LinkedBlockingQueue<OpenStackCommand> getWorkerQueue() {
        return workerQueue;
    }

    private final LinkedBlockingQueue<OpenStackCommand> workerQueue;

    private LinkedBlockingQueue<Object> responseQueue;

    public OpenStackWrapper() {
        this.workerQueue = new LinkedBlockingQueue<>();
        this.responseQueue = new LinkedBlockingQueue<>();
    }

    public LinkedBlockingQueue<Object> getResponseQueue() {
        return responseQueue;
    }


    public void initializeOpenStackClient(){
        this.osClientHandle = OSFactory.builder()
                .endpoint(OS_AUTH_URL)
                .credentials(OS_USER_NAME,OS_PASSWORD)
                .tenantName(OS_TENANT_NAME)
                .authenticate();

        this.osClientHandle.useRegion(OS_REGION_NAME);
        this.nodeFlavor = this.osClientHandle.compute().flavors().get(OS_NODE_FLAVOR);
        this.nodeImage = this.osClientHandle.compute().images().get(OS_IMAGE_ID);
        this.osKeyPair = this.osClientHandle.compute().keypairs().get(OS_KEY_PAIR_NAME);
    }

    public Node createNewNode(String nodeName) throws InterruptedException {

        // Create a Server Model Object
        ServerCreate sc = Builders.server()
                .name(nodeName)
                .flavor(this.nodeFlavor)
                .image(this.nodeImage)
                .keypairName(OS_KEY_PAIR_NAME)
                .addSecurityGroup("Hadoop")
                .addSecurityGroup("Mesos-Slave")
                .addSecurityGroup("SSH")
                .build();

        // Boot the Server
        Server server = this.osClientHandle.compute().servers().boot(sc);
//        String consoleOutput = this.osClientHandle.compute().servers().getConsoleOutput( server.getId(), 50);

//        System.out.println(consoleOutput);

//        try {
//            this.getOverlordHandle().getHttpHandle().getResponseQueue().put(server.getId());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        Node node = new Node();
        node.setName(nodeName);
        responseQueue.add(node);
        return  node;
    }

    public void deleteNode(String serverID){
        this.osClientHandle.compute().servers().delete(serverID);
    }

    public ArrayList<Node> listNodes(){
        // List all Servers
        List<? extends Server> servers = this.osClientHandle.compute().servers().list();

        ArrayList<Node> serverIdList = new ArrayList<>();

        for (Server server: servers ) {

            if(server.getName().toLowerCase().contains("slave")) {
                Node node = new Node();
                node.setName(server.getName());
                node.setId(server.getId());
                node.setFlavor(server.getFlavor().getId());
                node.setStatus(server.getStatus().toString());
                try {
                    node.setIp(server.getAddresses().getAddresses("Mesos-Cluster").get(0).getAddr());
                } catch (Exception e) {

                }
                serverIdList.add(node);
            }
        }

        responseQueue.add(serverIdList);
        return serverIdList;
    }

    public Node listNode(String name)
    {
        List<? extends Server> servers = this.osClientHandle.compute().servers().list();

        for (Server server : servers)
        {
            if(server.getName().equals(name))
            {
                Node node = new Node();
                node.setName(server.getName());
                node.setId(server.getId());
                node.setFlavor(server.getFlavor().getId());
                node.setStatus(server.getStatus().toString());
                try {
                    node.setIp(server.getAddresses().getAddresses("Mesos-Cluster").get(0).getAddr());
                }
                catch (Exception ex)
                {

                }
                return  node;
            }
        }
        return  null;
    }



    @Override
    public void run() {
        initializeOpenStackClient();

            try {
                OpenStackCommand command = this.workerQueue.take();

                switch (command.getCommand()) {

                    case "create":
                        createNewNode(command.getNodeName());
                        break;

                    case "delete":
                        deleteNode(command.getNodeName());
                        break;

                    case "list":
                        listNodes();
                        break;

                    case "listNode":
                        listNode(command.getNodeName());
                        break;

                    default:
                        System.out.println("Unknown Command "+command.getCommand());
                        break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}
