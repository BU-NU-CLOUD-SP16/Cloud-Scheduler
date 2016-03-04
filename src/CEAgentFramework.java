/**
 * Created by Praveen on 3/2/2016.
 */
public interface CEAgentFramework extends Runnable {

    public void notifyTimerExpiry(CEAgentCommand workerCommand) throws CEAgentException;

}
