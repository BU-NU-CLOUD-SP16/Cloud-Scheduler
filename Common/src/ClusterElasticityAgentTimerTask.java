import java.util.TimerTask;

/**
 * Created by Praveen on 3/2/2016.
 */
public class ClusterElasticityAgentTimerTask extends TimerTask {
    private ClusterElasticityAgentFramework notifyFramework;
    private ClusterElasticityAgentCommand commandToExecute;

    public ClusterElasticityAgentTimerTask(ClusterElasticityAgentFramework notifyFramework, ClusterElasticityAgentCommand ceAgentCommand) {
        this.notifyFramework = notifyFramework;
        this.commandToExecute = ceAgentCommand;
    }

    public ClusterElasticityAgentFramework getNotifyFramework() {
        return notifyFramework;
    }

    public void setNotifyFramework(ClusterElasticityAgentFramework notifyFramework) {
        this.notifyFramework = notifyFramework;
    }

    @Override
    public void run() {
        try {
            notifyFramework.notifyTimerExpiry(commandToExecute);
        } catch (ClusterElasticityAgentException e) {
            e.printStackTrace();
        }
    }
}
