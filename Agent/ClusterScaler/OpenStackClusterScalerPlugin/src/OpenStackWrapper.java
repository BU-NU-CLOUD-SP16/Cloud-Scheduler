import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.*;
import org.openstack4j.openstack.OSFactory;

import java.util.ArrayList;
import java.util.Arrays;
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

    public LinkedBlockingQueue<OpenStackCommand> getWorkerQueue() {
        return workerQueue;
    }

    private final LinkedBlockingQueue<OpenStackCommand> workerQueue;

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

    public Node createNewNode(CreateCommand command) throws InterruptedException {



        // Create a Server Model Object
        ServerCreate sc = Builders.server()
                .name(command.getName())
                .flavor(this.osClientHandle.compute().flavors().get(command.getFlavor()))
                .image(this.osClientHandle.compute().images().get(command.getImageName()))
                .keypairName(command.getKeyPair())
                .networks(Arrays.asList(new String[] {command.getNetwork()}))
                .addSecurityGroup(command.getSecurityGroup())
                .addSecurityGroup("Admin")
                .build();

        // Boot the Server
        Server server = this.osClientHandle.compute().servers().boot(sc);

        OpenStackNode node = new OpenStackNode();
        node.setHostname(command.getName());
        responseQueue.add(node);
        return  node;
    }

    public void deleteNode(DeleteCommand command){
        this.osClientHandle.compute().servers().delete(command.getId());
    }

    public ArrayList<OpenStackNode> listNodes(ListCommand command){
        // List all Servers
        ArrayList<OpenStackNode> serverIdList = new ArrayList<>();

        if(command.getName() != null)
        {
            OpenStackNode node = listNode(command);
            if (node != null)
                serverIdList.add(node);
            responseQueue.add(serverIdList);
            return serverIdList;
        }

        List<? extends Server> servers = this.osClientHandle.compute().servers().list();

        for (Server server: servers ) {

            if(server.getName().toLowerCase().contains("slave")) {
                OpenStackNode node = new OpenStackNode();
                node.setHostname(server.getName());
                node.setId(server.getId());
                node.setFlavor(server.getFlavor().getId());
                node.setStatus(server.getStatus().value());
                try {
                    node.setIp(server.getAddresses().getAddresses(command.getNetwork()).get(0).getAddr());
                    serverIdList.add(node);
                } catch (Exception e) {

                }
            }
        }

        responseQueue.add(serverIdList);
        return serverIdList;
    }

    public OpenStackNode listNode(ListCommand command)
    {
        List<? extends Server> servers = this.osClientHandle.compute().servers().list();

        for (Server server : servers)
        {
            if(server.getName().equals(command.getName()))
            {
                OpenStackNode node = new OpenStackNode();
                node.setHostname(server.getName());
                node.setId(server.getId());
                node.setFlavor(server.getFlavor().getId());
                node.setStatus(server.getStatus().value());
                try {
                    node.setIp(server.getAddresses().getAddresses(command.getNetwork()).get(0).getAddr());
                    return node;
                }
                catch (Exception ex)
                {

                }
            }
        }
        return  null;
    }



    @Override
    public void run() {
        initializeOpenStackClient();

            try {
                OpenStackCommand command = this.workerQueue.take();

                switch (command.getClass().getName()) {

                    case "CreateCommand":
                        createNewNode((CreateCommand) command);
                        break;

                    case "DeleteCommand":
                        deleteNode((DeleteCommand) command);
                        break;

                    case "ListCommand":
                        listNodes((ListCommand) command);
                        break;

                    default:
                        System.out.println("Unknown Command "+command.getClass().getName());
                        break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}
