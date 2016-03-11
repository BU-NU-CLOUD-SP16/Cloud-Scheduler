import java.util.ArrayList;
import java.util.List;

/**
 * Created by kovit on 3/9/2016.
 */

public final class SlaveDetails {

    private String slaveId;
    private float load5Min;
    private float freeMemory;
    private float totalMemory;
    private int cpu;
    private int allocatedCpu;
    private String ip;
    private String hostName;

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

    public float getFreeMemory() {
        return freeMemory;
    }

    public SlaveDetails setFreeMemory(float freeMemory) {
        this.freeMemory = freeMemory;
        return this;
    }

    public float getTotalMemory() {
        return totalMemory;
    }

    public SlaveDetails setTotalMemory(float totalMemory) {
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

