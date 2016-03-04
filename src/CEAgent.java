/**
 * Created by Praveen on 3/2/2016.
 */

import java.io.IOException;
import static spark.Spark.*;

public class CEAgent {

    private CommandLineArguments arguments;
    private static CEManagerFrameworkImpl elasticityManager = null;
    private CollectorPluginFrameworkImpl resourceCollector;

    public CEAgent() {
    }

    public CommandLineArguments getArguments() {
        return arguments;
    }

    public void setArguments(CommandLineArguments arguments) {
        this.arguments = arguments;
    }

    public static CEManagerFrameworkImpl getElasticityManager() {
        return CEAgent.elasticityManager;
    }

    public static void setElasticityManager(CEManagerFrameworkImpl elasticityManager) {
        CEAgent.elasticityManager = elasticityManager;
    }

    public CollectorPluginFrameworkImpl getResourceCollector() {
        return resourceCollector;
    }

    public void setResourceCollector(CollectorPluginFrameworkImpl resourceCollector) {
        this.resourceCollector = resourceCollector;
    }

    public static void main(String args[]){

        CEAgent agent = new CEAgent();

        CommandLineArguments argumentList = new CommandLineArguments();
        try {
            argumentList.parseCommandLineArguments(args);
        } catch (CEAgentException e) {
            e.printStackTrace();
            System.exit(1);
        }
        agent.setArguments(argumentList);

        try {
            ModuleLoader.addFile(argumentList.getCollectorPluginJar());
            ModuleLoader.addFile(argumentList.getCemanagerPluginJar());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        /*// Test
        ModuleLoader.testLoadedModule(argumentList.getCollectorPluginMainClass().toString(), "printHello");
        ModuleLoader.testLoadedModule(argumentList.getCemanagerPluginMainClass().toString(), "printWorld");*/

        CollectorPluginFrameworkImpl collectorPlugin = new CollectorPluginFrameworkImpl(argumentList);
        Thread collectorThread = new Thread(collectorPlugin);
        collectorThread.start();
        agent.setResourceCollector(collectorPlugin);

        CEManagerFrameworkImpl elasticityManager = new CEManagerFrameworkImpl(argumentList);
        Thread elasticityManagerThread = new Thread(elasticityManager);
        elasticityManagerThread.start();
        agent.setElasticityManager(elasticityManager);

        // Route the end-point request-resource
        post("/request-resource", (req, res) -> {
            String responseString = "";

            try {
                CEAgent.getElasticityManager().notifyResourceScaling(new CEAgentCommand() {
                    public void execute() {
                        /*System.out.println("Handled HTTP Request");*/
                    }
                });
                res.status(200);
                res.body("Success");
            }catch(CEAgentException e){
                e.printStackTrace();
                res.status(400);
                res.body("Failure");
            }

            return res.body();
        });

        while(true);

    }
}
