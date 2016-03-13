import java.util.ArrayList;

/**
 * Created by kovit on 3/9/2016.
 */

public final class FrameworkSlaveRelationship {

    private String slaveId;
    private String frameworkId;

    public SlaveDetails getSlave() {
        return slave;
    }

    public FrameworkSlaveRelationship setSlave(SlaveDetails slave) {
        this.slave = slave;
        return this;
    }

    private SlaveDetails slave;

    public String getSlaveId() {
        return slaveId;
    }

    public FrameworkSlaveRelationship setSlaveId(String slaveId) {
        this.slaveId = slaveId;
        return this;
    }

    public String getFrameworkId() {
        return frameworkId;
    }

    public FrameworkSlaveRelationship setFrameworkId(String frameworkId) {
        this.frameworkId = frameworkId;
        return this;
    }
}
