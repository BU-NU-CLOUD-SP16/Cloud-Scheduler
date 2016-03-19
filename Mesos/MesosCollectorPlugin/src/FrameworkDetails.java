/**
 * Created by kovit on 3/9/2016.
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

    public FrameworkDetails setMemory(float memory) {
        this.memory = memory;
        return this;
    }

    public String getFrameworkId() {
        return frameworkId;
    }

    public FrameworkDetails setFrameworkId(String frameworkId) {
        this.frameworkId = frameworkId;
        return this;
    }

    public String getName() {
        return name;
    }

    public FrameworkDetails setName(String name) {
        this.name = name;
        return this;
    }

    public int getCpu() {
        return cpu;
    }

    public FrameworkDetails setCpu(int cpu) {
        this.cpu = cpu;
        return this;
    }

    public int getActive() {
        return active;
    }

    public FrameworkDetails setActive(boolean active) {
        if (active) {
            this.active = 1;
        }
        else {
            this.active = 0;
        }
        return this;
    }

    public int getScheduledTasks() {
        return scheduledTasks;
    }

    public FrameworkDetails setScheduledTasks(int scheduledTasks) {
        this.scheduledTasks = scheduledTasks;
        return this;
    }
}