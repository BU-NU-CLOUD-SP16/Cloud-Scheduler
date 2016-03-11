import java.util.ArrayList;
import java.util.List;

/**
 * Created by kovit on 3/9/2016.
 */
public interface ICollectorPluginByRow {

    int fetch(List<Data> data, String masterAddr);
}
