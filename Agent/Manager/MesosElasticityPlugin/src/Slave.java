import java.util.ArrayList;

/**
 * <h1>Slave</h1>
 * Slave Getter Setters
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-03-16
 */
public class Slave {

    private String id;
    private double load;
    private double free_mem;
    private double total_mem;
    private int cpu;
    private double allocated_cpu;
    private String ip;
    private String hostname;

    private int filterTime;
    private boolean filterSet;

    private ArrayList<Framework> frameworks_running;

    /**
     * <h1>Slave</h1>
     * Constructor
     */
    public Slave() {
        this.frameworks_running = new ArrayList<>();
    }

    /**
     * <h1>getFilterTime</h1>
     * @return Int the time the
     * filter was set.
     */
    public int getFilterTime() {
        return filterTime;
    }

    /**
     * <h1>setFilterTime</h1>
     * @param filterTime Int
     */
    public void setFilterTime(int filterTime) {
        this.filterTime = filterTime;
    }

    /**
     * <h1>isFilterSet</h1>
     * @return Boolean true if the
     * filter is set for the slave.
     */
    public boolean isFilterSet() {
        return filterSet;
    }

    /**
     * <h1>setFilterSet</h1>
     * @param filterSet Boolean
     */
    public void setFilterSet(boolean filterSet) {
        this.filterSet = filterSet;
    }

    /**
     * <h1>getId</h1>
     * @return String id of the slave.
     */
    public String getId() {
        return id;
    }

    /**
     * <h1>setId</h1>
     * @param id String
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * <h1>getLoad</h1>
     * @return double , the load currently
     * taken by the Slave.
     */
    public double getLoad() {
        return load;
    }

    /**
     * <h1>setLoad</h1>
     * @param load type double
     */
    public void setLoad(double load) {
        this.load = load;
    }

    /**
     * <h1>getFree_mem</h1>
     * @return double the
     * free memory available in the slave.
     */
    public double getFree_mem() {
        return free_mem;
    }

    /**
     * <h1>setFree_mem</h1>
     * @param free_mem
     */
    public void setFree_mem(double free_mem) {
        this.free_mem = free_mem;
    }

    /**
     * <h1>getTotal_mem</h1>
     * @return double the total memory
     * present in the slave.
     */
    public double getTotal_mem() {
        return total_mem;
    }

    /**
     * <h1>setTotal_mem</h1>
     * @param total_mem type double
     */
    public void setTotal_mem(double total_mem) {
        this.total_mem = total_mem;
    }

    /**
     * <h1>getCpu</h1>
     * @return int the number of
     * cpus used by the slave.
     */
    public int getCpu() {
        return cpu;
    }

    /**
     * <h1>setCpu</h1>
     * @param cpu type int
     */
    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    /**
     * <h1>getAllocated_cpu</h1>
     * @return double the total number of the
     * allocated CPU.
     */
    public double getAllocated_cpu() {
        return allocated_cpu;
    }

    /**
     * <h1>setAllocated_cpu</h1>
     * @param allocated_cpu type double
     */
    public void setAllocated_cpu(double allocated_cpu) {
        this.allocated_cpu = allocated_cpu;
    }

    /**
     * <h1>getIp</h1>
     * @return String the ip address
     * of the current Slave.
     */
    public String getIp() {
        return ip;
    }

    /**
     * <h1>setIp</h1>
     * @param ip type String
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * <h1>getHostname</h1>
     * @return String the hostname
     * of the Slave.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * <h1>setHostname</h1>
     * @param hostname type String
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * <h1>getFrameworks_running</h1>
     * @return ArrayList<Framework>
     *     the total frameworks this
     *     slave is running.
     */
    public ArrayList<Framework> getFrameworks_running() {
        return frameworks_running;
    }

    /**
     * <h1>setFrameworks_running</h1>
     * @param frameworks_running type ArrayList<Framework>
     */
    public void setFrameworks_running(ArrayList<Framework> frameworks_running) {
        this.frameworks_running = frameworks_running;
    }

    /**
     * <h1>toString</h1>
     * @return String
     * All the values of the Slave
     * object.
     */
    public void copy(Slave slave)
    {
        this.id = slave.getId();
        this.free_mem = slave.getFree_mem();
        this.load = slave.getLoad();
        this.cpu = slave.getCpu();
        this.hostname = slave.getHostname();
        this.ip = slave.getIp();
        this.total_mem = slave.getTotal_mem();
        this.allocated_cpu = slave.getAllocated_cpu();
    }

}
