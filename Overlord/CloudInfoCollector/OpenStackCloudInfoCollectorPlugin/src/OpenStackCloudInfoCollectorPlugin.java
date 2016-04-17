import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 4/15/16.
 */
public class OpenStackCloudInfoCollectorPlugin implements CloudInfoCollectorPlugin {

    private String username;
    private String password;

    private String prefix;

    @Override
    public void setup(Config config) {
        username = config.getValueForKey("Username");
        password = config.getValueForKey("Password");
        prefix = config.getValueForKey("Slave-Prefix");
    }

    @Override
    public ArrayList<Node> listNodes() {

        ArrayList<Node> nodes = new ArrayList<>();

        try {
            OpenStackWrapper openStackWrapper = new OpenStackWrapper(username, password);
            ListCommand listCommand = new ListCommand();
            listCommand.setPrefix(prefix);
            openStackWrapper.getWorkerQueue().add(listCommand);
            Thread t = new Thread(openStackWrapper);
            t.start();
            nodes = (ArrayList<Node>) openStackWrapper.getResponseQueue().take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return nodes;
    }
}
