import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.*;
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
    private static final String OS_USER_NAME = "Enter here";
    private static final String OS_PASSWORD = "Enter Here";
    private static final String OS_NODE_FLAVOR = "4";
    private static final String OS_IMAGE_ID = "dc786b61-64eb-495a-a58a-3068e0231614";
    private static final String OS_KEY_PAIR_NAME = "Your Key";

    public Overlord getOverlordHandle() {
        return overlordHandle;
    }

    public void setOverlordHandle(Overlord overlordHandle) {
        this.overlordHandle = overlordHandle;
    }

    private Overlord overlordHandle;
    private OSClient osClientHandle;
    private Flavor nodeFlavor;
    private Image nodeImage;
    private Keypair osKeyPair;

    public LinkedBlockingQueue<OpenStackCommand> getWorkerQueue() {
        return workerQueue;
    }

    private final LinkedBlockingQueue<OpenStackCommand> workerQueue;

    public OpenStackWrapper(Overlord handle) {
        this.overlordHandle = handle;
        this.workerQueue = new LinkedBlockingQueue<>();
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

    public String createNewNode(String nodeName) throws InterruptedException {

        // Create a Server Model Object
        ServerCreate sc = Builders.server()
                .name(nodeName)
                .flavor(this.nodeFlavor)
                .image(this.nodeImage)
                .keypairName(OS_KEY_PAIR_NAME)
                .build();

        // Boot the Server
        Server server = this.osClientHandle.compute().servers().boot(sc);
        String consoleOutput = this.osClientHandle.compute().servers().getConsoleOutput( server.getId(), 50);

        System.out.println(consoleOutput);

        try {
            this.getOverlordHandle().getHttpHandle().getResponseQueue().put(server.getId());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return server.getId();
    }

    public void deleteNode(String serverID){
        this.osClientHandle.compute().servers().delete(serverID);
    }

    public ArrayList<String> listNodes(){
        // List all Servers
        List<? extends Server> servers = this.osClientHandle.compute().servers().list();

        ArrayList<String> serverIdList = new ArrayList<>();

        for (Server server: servers ) {
            serverIdList.add(server.getId());
        }

        return serverIdList;
    }

    @Override
    public void run() {
        initializeOpenStackClient();

        while (true) {
            try {
                OpenStackCommand command = this.workerQueue.take();

                switch (command.getCommand()) {

                    case "create":
                        createNewNode(command.getNodeName());
                        break;

                    case "delete":
                        deleteNode(command.getNodeName());
                        break;

                    default:
                        System.out.println("Unknown Command");
                        break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
