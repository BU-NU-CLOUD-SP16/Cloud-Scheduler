/**
 * Created by Praveen on 3/2/2016.
 */

import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

public class CollectorPluginFrameworkImpl implements CollectorPluginFramework {

    private final LinkedBlockingQueue<CEAgentCommand> workerQueue;
    private final CommandLineArguments arguments;

    public CollectorPluginFrameworkImpl(CommandLineArguments argumentList) {
        workerQueue = new LinkedBlockingQueue<CEAgentCommand>();
        arguments = argumentList;
    }

    public void run() {
        System.out.println("Collector Plugin Framework Started!!");
        while (true) {
            try {
                new Timer().schedule(new CEAgentTimerTask(this, new CEAgentCommand() {
                    public void execute() {
                        /*System.out.println("Handled Collector Plugin Timer Expiry");*/
                    }
                }), 5000);
                CEAgentCommand command = workerQueue.take();
                command.execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void notifyTimerExpiry(CEAgentCommand workerCommand) throws CEAgentException {

        try {
            workerQueue.put(workerCommand);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CEAgentException("Failed to Queue HTTP Request for scaling resource!!");
        }

    }
}
