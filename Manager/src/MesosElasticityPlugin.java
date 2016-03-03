/**
 * Created by chemistry_sourabh on 3/2/16.
 */
public class MesosElasticityPlugin implements ElasticityPlugin {

    @DataQuery(queries = {"SELECT * FROM blah"})
    @Override
    public int scaleUp(Data data) {
        return 0;
    }

    @DataQuery(queries = {"SELECT * FROM blah2"})
    @NodeQuery(query = "SELECT * FROM slave")
    @Override
    public boolean scaleDown(Node node, Data data) {
        return false;
    }
}
