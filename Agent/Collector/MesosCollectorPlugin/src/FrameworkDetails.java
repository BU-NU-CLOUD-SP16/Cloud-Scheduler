/**
 * <h1>FrameworkDetails</h1>
 * This class maintains and is the reason for
 * any scaling up or down of the nodes based on
 * data received from Mesos.
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-03-09
 */
public final class FrameworkDetails {

    private String frameworkId;
    private String name;
    private int cpu;
    private float memory;
    private int active;
    private int scheduledTasks;

    public float getMemory() {
        return memory;
    }

    /**
     * <h1>setMemory</h1>
     * Sets given Memory to the object.
     * @param memory type-> Float
     * @return FrameworkDetails
     */
    public FrameworkDetails setMemory(float memory) {
        this.memory = memory;
        return this;
    }

    /**
     * <h1>getFrameworkId</h1>
     * @return String frameworkId
     */
    public String getFrameworkId() {
        return frameworkId;
    }

    /**
     * <h1>setFrameworkId</h1>
     * Sets the frameworkID
     * @param frameworkId
     * @return Framework
     */
    public FrameworkDetails setFrameworkId(String frameworkId) {
        this.frameworkId = frameworkId;
        return this;
    }

    /**
     * <h1>getName</h1>
     * @return String -> name of the framework
     */
    public String getName() {
        return name;
    }

    /**
     * <h1>setName</h1>
     * Sets the Framework Name
     * @param name type-> String
     * @return Object FrameworkDetails
     */
    public FrameworkDetails setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * <h1>getCpu</h1>
     * gets the number of cpu.
     * @return int
     */
    public int getCpu() {
        return cpu;
    }

    /**
     * <h1>setCpu</h1>
     * Sets the number of CPU
     * @param cpu
     * @return Object FrameworkDetails
     */
    public FrameworkDetails setCpu(int cpu) {
        this.cpu = cpu;
        return this;
    }

    /**
     * <h1>getActive</h1>
     * returns either 1 or 0.
     * @return int
     */
    public int getActive() {
        return active;
    }

    /**
     * <h1>setActive</h1>
     * @param active type -> Boolean
     * @return Object FrameworkDetails
     */
    public FrameworkDetails setActive(boolean active) {
        if (active) {
            this.active = 1;
        }
        else {
            this.active = 0;
        }
        return this;
    }

    /**
     * <h1>getScheduledTaskss</h1>
     * @return int -> total scheduled Tasks
     */
    public int getScheduledTasks() {
        return scheduledTasks;
    }

    /**
     * <h1>setScheduledTasks</h1>
     * @param scheduledTasks
     * @return
     */
    public FrameworkDetails setScheduledTasks(int scheduledTasks) {
        this.scheduledTasks = scheduledTasks;
        return this;
    }
}