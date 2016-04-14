/**
 * Created by kovit on 3/9/2016.
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

    public boolean isReachable() {
        return isReachable;
    }

    public void setReachable(boolean reachable) {
        isReachable = reachable;
    }

    public String getIpNPort() {
        return ipNPort;
    }

    public void setIpNPort(String ipNPort) {
        this.ipNPort = ipNPort;
    }

    private String ipNPort;


    public String getSlaveId() {
        return slaveId;
    }

    public SlaveDetails setSlaveId(String slaveId) {
        this.slaveId = slaveId;
        return this;
    }

    public float getLoad5Min() {
        return load5Min;
    }

    public SlaveDetails setLoad5Min(float load5Min) {
        this.load5Min = load5Min;
        return this;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public SlaveDetails setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
        return this;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public SlaveDetails setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
        return this;
    }

    public int getCpu() {
        return cpu;
    }

    public SlaveDetails setCpu(int cpu) {
        this.cpu = cpu;
        return this;
    }

    public int getAllocatedCpu() {
        return allocatedCpu;
    }

    public SlaveDetails setAllocatedCpu(int allocatedCpu) {
        this.allocatedCpu = allocatedCpu;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public SlaveDetails setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getHostName() {
        return hostName;
    }

    public SlaveDetails setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }
}

