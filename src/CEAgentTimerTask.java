import java.util.TimerTask;

/**
 * Created by Praveen on 3/2/2016.
 */
public class CEAgentTimerTask extends TimerTask {
    private CEAgentFramework notifyFramework;
    private CEAgentCommand commandToExecute;

    public CEAgentTimerTask(CEAgentFramework notifyFramework, CEAgentCommand ceAgentCommand) {
        this.notifyFramework = notifyFramework;
        this.commandToExecute = ceAgentCommand;
    }

    public CEAgentFramework getNotifyFramework() {
        return notifyFramework;
    }

    public void setNotifyFramework(CEAgentFramework notifyFramework) {
        this.notifyFramework = notifyFramework;
    }

    @Override
    public void run() {
        try {
            notifyFramework.notifyTimerExpiry(commandToExecute);
        } catch (CEAgentException e) {
            e.printStackTrace();
        }
    }
}
