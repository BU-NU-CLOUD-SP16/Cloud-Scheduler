
/**
 * <h1>SlaveDetails</h1>
 * This class contains all the necessary
 * Slave Node Details.
 *
 * @author Kovit
 * @version 1.0
 * @since 2016-03-09
 */
public final class SlaveDetails {

    private String slaveId;
    private float load5Min;
    private long freeMemory;
    private long totalMemory;
    private int cpu;
    private int allocatedCpu;
    private String ip;
    private String hostName;
    private boolean isReachable = true;

    /**
     * <h1>isReachable</h1>
     * @return true if the slave is
     * reachable.
     */
    public boolean isReachable() {
        return isReachable;
    }

    /**
     * <h1>setReachable</h1>
     * Sets the boolean value id the
     * slave is reachable.
     * @param reachable
     */
    public void setReachable(boolean reachable) {
        isReachable = reachable;
    }

    /**
     * <h1>getIpNPort</h1>
     *
     * @return String IP address
     * and Port number where the
     * current slave resides
     */
    public String getIpNPort() {
        return ipNPort;
    }

    /**
     * <h1>setIpNPort</h1>
     * Sets the String IP address
     * and Port number where the
     * current Slave resides.
     * @param ipNPort
     */
    public void setIpNPort(String ipNPort) {
        this.ipNPort = ipNPort;
    }

    private String ipNPort;


    /**
     * <h1>getSlaveId</h1>
     * @return String the SlaveID
     */
    public String getSlaveId() {
        return slaveId;
    }

    /**
     * <h1>setSlaveId</h1>
     * @param slaveId
     * @return Object of class
     * after setting slave id.
     */
    public SlaveDetails setSlaveId(String slaveId) {
        this.slaveId = slaveId;
        return this;
    }

    /**
     * <h1>getLoad5Min</h1>
     * @return Float The average load
     * spread across a period of 5 mins.
     */
    public float getLoad5Min() {
        return load5Min;
    }

    /**
     * <h1>setLoad5Min</h1>
     * @param load5Min
     * @return Object of class after
     * avg Load of 5 mins is set
     */
    public SlaveDetails setLoad5Min(float load5Min) {
        this.load5Min = load5Min;
        return this;
    }

    /**
     * <h1>getFreeMemort</h1>
     * @return Long FreeMemory
     * available in the Slave Node.
     */
    public long getFreeMemory() {
        return freeMemory;
    }

    /**
     * <h1>SetFreeMemory</h1>
     * @param freeMemory
     * @return Object of the class
     * after free memory is set.
     */
    public SlaveDetails setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
        return this;
    }

    /**
     * <h1>getTotalMemory</h1>
     * @return Long the total Memory
     * available in the Slave Node.
     */
    public long getTotalMemory() {
        return totalMemory;
    }

    /**
     * <h1>setTotalMemory</h1>
     * @param totalMemory
     * @return Object of class after the
     * total memory of the slave is set.
     */
    public SlaveDetails setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
        return this;
    }

    /**
     * <h1>getCpu</h1>
     * @return int the number of
     * CPUSs present in the Slave
     * Machine.
     */
    public int getCpu() {
        return cpu;
    }

    /**
     * <h1>setCPU</h1>
     * @param cpu
     * @return Object of the class
     * after the number of cpus in
     * the slave machines are set.
     */
    public SlaveDetails setCpu(int cpu) {
        this.cpu = cpu;
        return this;
    }

    /**
     * <h1>getAllocatedCpu</h1>
     * @return int the number of
     * Cpus actually used by the
     * Slave Machine.
     */
    public int getAllocatedCpu() {
        return allocatedCpu;
    }

    /**
     * <h1>setAllocatedCpu</h1>
     * @param allocatedCpu
     * @return Object of the class
     * after the number of Cpus
     * actually used by the Slave
     * Node is set.
     */
    public SlaveDetails setAllocatedCpu(int allocatedCpu) {
        this.allocatedCpu = allocatedCpu;
        return this;
    }

    /**
     * <h1>getIp</h1>
     * @return String the IP
     * address of the Slave.
     */
    public String getIp() {
        return ip;
    }

    /**
     * <h1>setIp</h1>
     * @param ip
     * @return Object of the class
     * after the slave's IP is set.
     */
    public SlaveDetails setIp(String ip) {
        this.ip = ip;
        return this;
    }

    /**
     * <h1>getHostName</h1>
     * @return String the hostname
     * of the slave machine.
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * <h1>setHostName</h1>
     * @param hostName
     * @return Object of the class
     * after the Hostname is set
     * for the Slave.
     */
    public SlaveDetails setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }
}

