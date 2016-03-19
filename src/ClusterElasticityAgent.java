/**
 * Created by Praveen on 3/2/2016.
 */

import static spark.Spark.*;

public class ClusterElasticityAgent {

    private CommandLineArguments arguments;
    private ClusterElasticityManager elasticityManager;
    private Collector resourceCollector;
    private DBExecutor dbHandle;

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

    public Collector getResourceCollector() {
        return resourceCollector;
    }

    public void setResourceCollector(Collector resourceCollector) {
        this.resourceCollector = resourceCollector;
    }

    public DBExecutor getDbHandle() {
        return dbHandle;
    }

    public void setDbHandle(DBExecutor dbHandle) {
        this.dbHandle = dbHandle;
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

        /*
        try {
            ModuleLoader.addFile(argumentList.getCollectorPluginJar());
            ModuleLoader.addFile(argumentList.getCemanagerPluginJar());
            ModuleLoader.addFile(argumentList.getClusterScalerPluginJar());

            if(argumentList.getDbExecutorPluginJar().isFile())
                ModuleLoader.addFile(argumentList.getDbExecutorPluginJar());

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }*/

        /*// Test
        ModuleLoader.testLoadedModule(argumentList.getCollectorPluginMainClass().toString(), "printHello");
        ModuleLoader.testLoadedModule(argumentList.getCemanagerPluginMainClass().toString(), "printWorld");*/

        DBExecutor dbExecutor = null;
        try {
            ClassLoader classLoader = ClusterElasticityAgent.class.getClassLoader();
            Class aClass = classLoader.loadClass(argumentList.getDbExecutorPluginMainClass());
            dbExecutor = (DBExecutor) aClass.newInstance();
//            dbExecutor.executeScript(argumentList.getDdlFile());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InstantiationException e) {
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        agent.setDbHandle(dbExecutor);

        Collector collectorPlugin = new Collector(argumentList);
        agent.setResourceCollector(collectorPlugin);

        ClusterElasticityManager elasticityManager = new ClusterElasticityManager(argumentList);
        agent.setElasticityManager(elasticityManager);

        // Route the end-point request-resource
        post("/request-resource", (req, res) -> {
            String responseString = "";

            try {
                agent.getElasticityManager().notifyResourceScaling("");
                res.status(200);
                res.body("Success");
            }catch(ClusterElasticityAgentException e){
                e.printStackTrace();
                res.status(400);
                res.body("Failure");
            }

            return res.body();
        });

        while(true){
            try {

                collectorPlugin.notifyTimerExpiry();
                elasticityManager.notifyTimerExpiry();

                Thread.sleep(argumentList.getPollInterval());
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ClusterElasticityAgentException e) {
                e.printStackTrace();
            }
        }

    }
}
