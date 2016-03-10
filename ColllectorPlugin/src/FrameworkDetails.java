import java.util.ArrayList;
import java.util.List;

/**
 * Created by kovit on 3/9/2016.
 */

@Table(name = "Framework")
public final class FrameworkDetails implements ICollectorPlugin {

    @Override
    public int fetch(List<Data> data) {
        return 0;
    }

    @Column(name="Framework_ID")
    public String getFrameworkId() {
        return null;
    }

    @Column(name="Name")
    public String getName() {
        return null;
    }

    @Column(name="CPU")
    public int getNoOfCpu() {
        return 0;
    }

    @Column(name="Memory")
    public float getMemory() {
        return 0;
    }

    @Column(name="Active")
    public int isActive() {
        return 0;
    }

    @Column(name="Scheduled_Tasks")
    public int getNoOfScheduledTasks() {
        return 0;
    }
}
