import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public class MesosElasticityPlugin implements ElasticityPlugin {

    @DataQuery(queries = {"SELECT * FROM blah"})
    @Override
    public int scaleUp(ArrayList<Data> data)
    {
        System.out.println("Scale Up");
        return 0;
    }

    @DataQuery(queries = {"SELECT * FROM framework f inner join runs_on r on r.framework_id=f.framework_id inner join slave s s.slave_id=r.slave_id"})
    @NodeQuery(query = "SELECT * FROM Slave")
    @Override
    public boolean scaleDown(Node node, ArrayList<Data> data)
    {
        System.out.println("Scale Down");
        return false;
    }

    @Override
    public int requestResources(String query) {
        return 1;
    }
}
