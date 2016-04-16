/**
 * Created by kovit on 3/9/2016.
 */

/**
 * <h1>FrameworkSlaveRelationship</h1>
 * This class maintains the Framework Slave
 * relationship.
 *
 * @author Kovit
 * @version 1.0
 * @since 2016-03-09
 */

public final class FrameworkSlaveRelationship {

    private String slaveId;
    private String frameworkId;

    /**
     * <h1>getSlave</h1>
     * @return Slave associaated with the framework
     */
    public SlaveDetails getSlave() {
        return slave;
    }

    /**
     * <h1>setSlave</h1>
     * @param slave
     * @return FrameworkSlaveRelationship Object
     * after the slave is set.
     */
    public FrameworkSlaveRelationship setSlave(SlaveDetails slave) {
        this.slave = slave;
        return this;
    }

    private SlaveDetails slave;

    /**
     * <h1>getSlaveId</h1>
     * @return String slaveID
     */
    public String getSlaveId() {
        return slaveId;
    }

    /**
     * <h1>setSlaveId</h1>
     * @param slaveId
     * @return FrameworkSlaveRelationship Object
     * after the slaveid is set
     */
    public FrameworkSlaveRelationship setSlaveId(String slaveId) {
        this.slaveId = slaveId;
        return this;
    }

    /**
     * <h1>getFrameworkId</h1>
     *
     * @return String the frameworkId
     */
    public String getFrameworkId() {
        return frameworkId;
    }

    /**
     * <h1>setFrameworkId</h1>
     * @param frameworkId
     * @return FrameworkSlaveRelationship Object
     * after the frameworkId is set
     */
    public FrameworkSlaveRelationship setFrameworkId(String frameworkId) {
        this.frameworkId = frameworkId;
        return this;
    }
}
