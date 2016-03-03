/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public class TestMain {

    public static void main(String args[])
    {
        ClusterElasticityManager manager = new ClusterElasticityManager(1,"MesosElasticityPlugin");

        manager.start();
    }
}
