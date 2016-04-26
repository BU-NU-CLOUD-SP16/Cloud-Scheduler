import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 4/15/16.
 */
public class OpenStackCloudInfoCollectorPlugin implements CloudInfoCollectorPlugin {

    private String username;
    private String password;

    private String prefix;

    private OpenStackWrapper openStackWrapper = null;

    @Override
    public void setup(Config config) {
        username = config.getValueForKey("Username");
        password = config.getValueForKey("Password");
        prefix = config.getValueForKey("Slave-Prefix");

        if (openStackWrapper == null)
        {
            openStackWrapper = new OpenStackWrapper(username, password);
            Thread t = new Thread(openStackWrapper);
            t.start();
        }
    }

    @Override
    public ArrayList<Node> listNodes() {

        ArrayList<Node> nodes = new ArrayList<>();
        try {
            ListCommand listCommand = new ListCommand();
            listCommand.setPrefix(prefix);
            openStackWrapper.getWorkerQueue().add(listCommand);
            nodes = (ArrayList<Node>) openStackWrapper.getResponseQueue().take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return nodes;
    }
}
