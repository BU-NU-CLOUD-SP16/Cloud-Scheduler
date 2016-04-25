import java.util.List;

/**
 * <h1>ICollectorPluginByTable</h1>
 *
 * @author Kovit
 * @version 1.0
 * @since 2016-03-10
 */
public interface ICollectorPluginByTable {
    /**
     * <h1>fetch</h1>
     * @param data
     * @param masterAddr
     * @return List<ITableInfo>
     */
    List<ITableInfo> fetch(List<Data> data, String masterAddr);
}
