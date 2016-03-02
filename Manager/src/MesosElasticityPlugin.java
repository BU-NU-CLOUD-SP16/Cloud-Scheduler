/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public class MesosElasticityPlugin implements ElasticityPlugin {


    @Override
    public int scaleUp(Object data) {
        return 0;
    }

    @Override
    public boolean scaleDown(Object node, Object data) {
        return false;
    }
}
