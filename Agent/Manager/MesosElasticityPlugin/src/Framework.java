import java.util.ArrayList;

/**
 * <h1>Framework</h1>
 * Framework Getter Setters
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-03-07
 */
public class Framework {

    private String id;
    private String name;
    private int cpu;
    private float memory;
    private boolean active;
    private boolean scheduled_tasks;
    private int filterTime;
    private boolean filterSet;
    private ArrayList<Slave> allocated_slaves;


    public Framework() {
        this.allocated_slaves = new ArrayList<>();
    }

    /**
     * <h1>getFilterTime</h1>
     * @return int the time the
     * filter was set.
     */
    public int getFilterTime() {
        return filterTime;
    }

    /**
     * <h1>setFilterTime</h1>
     * @param filterTime type int
     */
    public void setFilterTime(int filterTime) {
        this.filterTime = filterTime;
    }

    /**
     * <h1>isFilterSet</h1>
     * @return Boolean true if the
     * Filter is set for the framework.
     */
    public boolean isFilterSet() {
        return filterSet;
    }

    /**
     * <h1>setFilterSet</h1>
     * @param filterSet type Boolean
     */
    public void setFilterSet(boolean filterSet) {
        this.filterSet = filterSet;
    }

    /**
     * <h1>getAllocated_Slaves</h1>
     * @return ArrayList<Slave> the
     * total slaves allocated to this
     * framework.
     */
    public ArrayList<Slave> getAllocated_slaves() {
        return allocated_slaves;
    }

    /**
     * <h1>setAllocated_slaves</h1>
     * @param allocated_slaves type ArrayList<Slave>
     */
    public void setAllocated_slaves(ArrayList<Slave> allocated_slaves) {
        this.allocated_slaves = allocated_slaves;
    }

    /**
     * <h1>getId</h1>
     * @return String the id
     * of the framework.
     */
    public String getId() {
        return id;
    }

    /**
     * <h1>setId</h1>
     * @param id type String
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * <h1>getName</h1>
     * @return String the name
     * of the Framework.
     */
    public String getName() {
        return name;
    }

    /**
     * <h1>setName</h1>
     * @param name type String
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * <h1>getCpu</h1>
     * @return Int the number of
     * CPU utilized by the Framework.
     */
    public int getCpu() {
        return cpu;
    }

    /**
     * <h1>setCpu</h1>
     * @param cpu type Int
     */
    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    /**
     * <h1>getMemory</h1>
     * @return Float the current
     * memory used by the Framework.
     */
    public float getMemory() {
        return memory;
    }

    /**
     * <h1>setMemory</h1>
     * @param memory type Float
     */
    public void setMemory(float memory) {
        this.memory = memory;
    }

    /**
     * <h1>isActive</h1>
     * @return Boolean
     * if the Framework is currently
     * active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * <h1>setActive</h1>
     * @param active type Boolean
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * <h1>isScheduled_tasks</h1>
     * @return Boolean if there
     * is any Task scheduled or not.
     */
    public boolean isScheduled_tasks() {
        return scheduled_tasks;
    }

    /**
     * <h1>setScheduled_tasks</h1>
     * @param scheduled_tasks type Boolean
     */
    public void setScheduled_tasks(boolean scheduled_tasks) {
        this.scheduled_tasks = scheduled_tasks;
    }


    /**
     * <h1>copy</h1>
     * Copies the cpu, memory,
     * active and scheduled_Tasks
     * of another framework into this
     * object.
     * @param framework
     */
    public void copy(Framework framework)
    {
        this.cpu = framework.getCpu();
        this.memory = framework.getMemory();
        this.active = framework.isActive();
        this.scheduled_tasks = framework.isScheduled_tasks();
    }

    /**
     * <h1>toString</h1>
     * @return String
     * All the values of the Framework
     * object.
     */
    @Override
    public String toString() {
        return "Framework{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", cpu=" + cpu +
                ", memory=" + memory +
                ", active=" + active +
                ", scheduled_tasks=" + scheduled_tasks +
                ", allocated_slaves=" + allocated_slaves +
                '}';
    }
}
