/**
 * Created by Praveen on 3/2/2016.
 */

import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

public class CEManagerFrameworkImpl implements CEManagerFramework{

    private final LinkedBlockingQueue<CEAgentCommand> workerQueue;
    private final CommandLineArguments arguments;

    public CEManagerFrameworkImpl() {
        workerQueue = new LinkedBlockingQueue<CEAgentCommand>();
        arguments = null;
    }

    public CEManagerFrameworkImpl(CommandLineArguments arguments) {
        workerQueue = new LinkedBlockingQueue<CEAgentCommand>();
        this.arguments = arguments;
    }

    public void run() {
        System.out.println("CE Manager Plugin Framework Started!!");
        while (true) {
            try {
                new Timer().schedule(new CEAgentTimerTask(this, new CEAgentCommand() {
                    public void execute() {
                        /*System.out.println("Handle CEManager Timer Expiry!!");*/
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


    public void notifyResourceScaling(CEAgentCommand workerCommand) throws CEAgentException {
        try {
            workerQueue.put(workerCommand);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CEAgentException("Failed to Queue HTTP Request for scaling resource!!");
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
