import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 4/15/16.
 */
public class OpenStackCloudInfoCollectorPlugin implements CloudInfoCollectorPlugin {

    private String username;
    private String password;

    @Override
    public void setup(Config config) {
        username = config.getValueForKey("Username");
        password = config.getValueForKey("Password");
    }

    @Override
    public ArrayList<Node> listNodes() {

        ArrayList<Node> nodes = new ArrayList<>();

        try {
            OpenStackWrapper openStackWrapper = new OpenStackWrapper(username, password);
            openStackWrapper.getWorkerQueue().add(new ListCommand());
            Thread t = new Thread(openStackWrapper);
            t.start();
            nodes = (ArrayList<Node>) openStackWrapper.getResponseQueue().take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return nodes;
    }
}
