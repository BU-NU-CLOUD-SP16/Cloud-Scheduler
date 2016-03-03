/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public interface ElasticityPlugin {

    public int scaleUp(Data data);
    public boolean scaleDown(Node node,Data data);

}
