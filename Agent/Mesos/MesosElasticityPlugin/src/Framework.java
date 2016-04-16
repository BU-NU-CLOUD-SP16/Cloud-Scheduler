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
     *
     * @return
     */
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

    public ArrayList<Slave> getAllocated_slaves() {
        return allocated_slaves;
    }

    public void setAllocated_slaves(ArrayList<Slave> allocated_slaves) {
        this.allocated_slaves = allocated_slaves;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public float getMemory() {
        return memory;
    }

    public void setMemory(float memory) {
        this.memory = memory;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isScheduled_tasks() {
        return scheduled_tasks;
    }

    public void setScheduled_tasks(boolean scheduled_tasks) {
        this.scheduled_tasks = scheduled_tasks;
    }


    public void copy(Framework framework)
    {
        this.cpu = framework.getCpu();
        this.memory = framework.getMemory();
        this.active = framework.isActive();
        this.scheduled_tasks = framework.isScheduled_tasks();
    }

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
