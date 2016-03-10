import java.util.ArrayList;
import java.util.List;

/**
 * Created by kovit on 3/9/2016.
 */

@Table(name="Runs_On")
public final class FrameworkSlaveRelationship implements ICollectorPlugin {

    @Override
    public int fetch(List<Data> data) {
        return 0;
    }

    @Column(name="Slave_ID")
    public String getSlaveId() {
        return null;
    }

    @Column(name="Framework_ID")
    public String getFrameworkId() {
        return null;
    }
}
