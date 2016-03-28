/**
 * Created by Praveen on 3/26/2016.
 */
public class CloudResourceInfo {

    private Integer maxNodesInPool;
    private static final Integer MAX_NODES_IN_POOL = 6;

    public CloudResourceInfo(){
        maxNodesInPool = MAX_NODES_IN_POOL;
    }

    public CloudResourceInfo(Integer maxNodesInPool) {
        this.maxNodesInPool = maxNodesInPool;
    }

    public Integer getMaxNodesInPool() {
        return maxNodesInPool;
    }

    public void setMaxNodesInPool(Integer maxNodesInPool) {
        this.maxNodesInPool = maxNodesInPool;
    }
}
