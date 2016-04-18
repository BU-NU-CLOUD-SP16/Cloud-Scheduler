import java.util.List;

/**
 * <h1>ICollectorPluginByRow</h1>
 *
 * @author Kovit
 * @version 1.0
 * @since 2016-03-09
 */
public interface ICollectorPluginByRow {
    /**
     * <h1>fetch</h1>
     * @param data
     * @param masterAddr
     * @return int
     */
    int fetch(List<Data> data, String masterAddr);
}
