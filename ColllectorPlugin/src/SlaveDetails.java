import java.util.ArrayList;
import java.util.List;

/**
 * Created by kovit on 3/9/2016.
 */

@Table(name="Slave")
public final class SlaveDetails implements ICollectorPlugin {

    @Override
    public int fetch(List<Data> data) {
        return 0;
    }

    @Column(name="Slave_ID")
    public String getSlaveId() {
        return null;
    }

    @Column(name="Load_5min")
    public float getLoad5min() {
        return 0;
    }

    @Column(name="Free_Memory")
    public float getFreeMemory() {
        return 0;
    }

    @Column(name="Total_Memory")
    public float getTotalMemory() {
        return 0;
    }

    @Column(name="CPU")
    public int getCpu() {
        return 0;
    }

    @Column(name="Allocated_CPU")
    public float getAllocatedCpu() {
        return 0;
    }

    @Column(name="IP")
    public String getIpAddr() {
        return null;
    }

    @Column(name="Hostname")
    public String getHostname() {
        return null;
    }
}
