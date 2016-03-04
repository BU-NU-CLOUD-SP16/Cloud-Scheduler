/**
 * Created by Praveen on 3/2/2016.
 */

import java.util.*;

public interface CEManagerFramework extends CEAgentFramework {

    public void notifyResourceScaling(CEAgentCommand workerCommand) throws CEAgentException;

}
