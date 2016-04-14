import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 3/7/16.
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

    public Slave() {
        this.frameworks_running = new ArrayList<>();
    }

    public int getFilterTime() {
        return filterTime;
    }

    public void setFilterTime(int filterTime) {
        this.filterTime = filterTime;
    }

    public boolean isFilterSet() {
        return filterSet;
    }

    public void setFilterSet(boolean filterSet) {
        this.filterSet = filterSet;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLoad() {
        return load;
    }

    public void setLoad(double load) {
        this.load = load;
    }

    public double getFree_mem() {
        return free_mem;
    }

    public void setFree_mem(double free_mem) {
        this.free_mem = free_mem;
    }

    public double getTotal_mem() {
        return total_mem;
    }

    public void setTotal_mem(double total_mem) {
        this.total_mem = total_mem;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public double getAllocated_cpu() {
        return allocated_cpu;
    }

    public void setAllocated_cpu(double allocated_cpu) {
        this.allocated_cpu = allocated_cpu;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public ArrayList<Framework> getFrameworks_running() {
        return frameworks_running;
    }

    public void setFrameworks_running(ArrayList<Framework> frameworks_running) {
        this.frameworks_running = frameworks_running;
    }

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
