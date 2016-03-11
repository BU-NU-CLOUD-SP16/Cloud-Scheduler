import java.util.List;
import java.util.Map;

/**
 * Created by kovit on 3/10/2016.
 */
public interface ICollectorPluginByTable {
    List<ITableInfo> fetch(List<Data> data, String masterAddr);
}
