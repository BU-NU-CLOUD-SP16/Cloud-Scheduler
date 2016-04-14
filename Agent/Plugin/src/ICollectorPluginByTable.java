import java.util.List;

/**
 * Created by kovit on 3/10/2016.
 */
public interface ICollectorPluginByTable {
    List<ITableInfo> fetch(List<Data> data, String masterAddr);
}
