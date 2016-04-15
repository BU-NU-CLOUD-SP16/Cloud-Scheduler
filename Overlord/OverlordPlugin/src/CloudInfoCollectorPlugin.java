import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 4/15/16.
 */
public interface CloudInfoCollectorPlugin {

    public void setup(Config config);
    public ArrayList<Node> listNodes();
}
