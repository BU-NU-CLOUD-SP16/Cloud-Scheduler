/**
 * Created by Praveen on 3/2/2016.
 */

import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

public class CollectorPluginFrameworkImpl implements CollectorPluginFramework {

    private final LinkedBlockingQueue<ClusterElasticityAgentCommand> workerQueue;
    private final CommandLineArguments arguments;

    public CollectorPluginFrameworkImpl(CommandLineArguments argumentList) {
        workerQueue = new LinkedBlockingQueue<ClusterElasticityAgentCommand>();
        arguments = argumentList;
    }

    public void run() {
        System.out.println("Collector Plugin Framework Started!!");
        while (true) {
            try {
                new Timer().schedule(new ClusterElasticityAgentTimerTask(this, new ClusterElasticityAgentCommand() {
                    public void execute() {
                        /*System.out.println("Handled Collector Plugin Timer Expiry");*/
                    }
                }), 5000);
                ClusterElasticityAgentCommand command = workerQueue.take();
                command.execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void notifyTimerExpiry(ClusterElasticityAgentCommand workerCommand) throws ClusterElasticityAgentException {

        try {
            workerQueue.put(workerCommand);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClusterElasticityAgentException("Failed to Queue HTTP Request for scaling resource!!");
        }

    }
}
