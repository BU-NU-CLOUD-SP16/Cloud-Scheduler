/**
 * Created by Praveen on 3/2/2016.
 */

import java.io.IOException;
import static spark.Spark.*;

public class ClusterElasticityAgent {

    private CommandLineArguments arguments;
    private ClusterElasticityManager elasticityManager;
    private CollectorPluginFrameworkImpl resourceCollector;

    public ClusterElasticityAgent() {
    }

    public CommandLineArguments getArguments() {
        return arguments;
    }

    public void setArguments(CommandLineArguments arguments) {
        this.arguments = arguments;
    }

    public ClusterElasticityManager getElasticityManager() {
        return elasticityManager;
    }

    public void setElasticityManager(ClusterElasticityManager elasticityManager) {
        this.elasticityManager = elasticityManager;
    }

    public CollectorPluginFrameworkImpl getResourceCollector() {
        return resourceCollector;
    }

    public void setResourceCollector(CollectorPluginFrameworkImpl resourceCollector) {
        this.resourceCollector = resourceCollector;
    }

    public static void main(String args[]){

        ClusterElasticityAgent agent = new ClusterElasticityAgent();

        CommandLineArguments argumentList = new CommandLineArguments();
        try {
            argumentList.parseCommandLineArguments(args);
        } catch (ClusterElasticityAgentException e) {
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

        ClusterElasticityManager elasticityManager = new ClusterElasticityManager(argumentList);
        Thread elasticityManagerThread = new Thread(elasticityManager);
        elasticityManagerThread.start();
        agent.setElasticityManager(elasticityManager);

        // Route the end-point request-resource
        post("/request-resource", (req, res) -> {
            String responseString = "";

            try {
                agent.getElasticityManager().notifyResourceScaling(new RequestResourcesClusterElasticityAgentCommand() {
                    public void execute() {
                        /*System.out.println("Handled HTTP Request");*/
                    }
                });
                res.status(200);
                res.body("Success");
            }catch(ClusterElasticityAgentException e){
                e.printStackTrace();
                res.status(400);
                res.body("Failure");
            }

            return res.body();
        });

        while(true);

    }
}
