package interfaces;

import wrappers.Data;
import wrappers.Node;

import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public interface ElasticityPlugin {

    public int scaleUp(ArrayList<Data> data);
    public boolean scaleDown(Node node, ArrayList<Data> data);
    public int requestResources(String query);

}
