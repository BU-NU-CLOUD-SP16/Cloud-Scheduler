/**
 * Created by Praveen on 3/2/2016.
 */

import java.io.File;
import java.util.logging.*;

import static spark.Spark.*;

public class ClusterElasticityAgent {

    private CommandLineArguments arguments;
    private ClusterElasticityManager elasticityManager;
    private Collector resourceCollector;
    private DBExecutor dbHandle;

    public static Logger logger;

    public ClusterElasticityAgent() {

    }

    public void setupLogger()
    {
        logger = createLogger();
        GlobalLogger.globalLogger = logger;
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
        agent.setupLogger();
        logger.log(Level.INFO,"Cluster Elasticity Agent Started",Constants.MAIN_LOG_ID);

        try {
            if(argumentList.getCollectorPluginJar() != null)
                ModuleLoader.addFile(argumentList.getCollectorPluginJar());
            if(argumentList.getCemanagerPluginJar() != null)
                ModuleLoader.addFile(argumentList.getCemanagerPluginJar());
            if(argumentList.getClusterScalerPluginJar() != null)
                ModuleLoader.addFile(argumentList.getClusterScalerPluginJar());

            if(argumentList.getDbExecutorPluginJar() != null)
                ModuleLoader.addFile(argumentList.getDbExecutorPluginJar());
            logger.log(Level.INFO,"Loaded all Modules",Constants.MAIN_LOG_ID);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Not able to load modules "+e,Constants.MAIN_LOG_ID);
            System.exit(1);
        }



        DBExecutor dbExecutor = null;
        try {
            ClassLoader classLoader = ClusterElasticityAgent.class.getClassLoader();
            Class aClass = classLoader.loadClass(argumentList.getDbExecutorPluginMainClass());
            dbExecutor = (DBExecutor) aClass.getConstructor(String.class).newInstance(argumentList.getConfig().getValueForKey("Id"));
            if(argumentList.getDdlFile() != null)
                dbExecutor.executeScript(argumentList.getDdlFile());
            logger.log(Level.INFO,"Executed DDL Script Successfully",Constants.MAIN_LOG_ID);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE,"Given Database class not found",Constants.MAIN_LOG_ID);
            System.exit(1);
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE,"Not able to create database instance",Constants.MAIN_LOG_ID);
            System.exit(1);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Some Exception "+e,Constants.MAIN_LOG_ID);
            System.exit(1);
        }
        agent.setDbHandle(dbExecutor);


        Collector collectorPlugin = new Collector(argumentList);
        agent.setResourceCollector(collectorPlugin);

        logger.log(Level.FINE,"Created Collector Object",Constants.MAIN_LOG_ID);

        ClusterElasticityManager elasticityManager = new ClusterElasticityManager(argumentList);
        agent.setElasticityManager(elasticityManager);

        logger.log(Level.FINE,"Created Elasticity Manager Object",Constants.MAIN_LOG_ID);


        logger.log(Level.FINE,"Started HTTP Endpoint",Constants.MAIN_LOG_ID);
        // Route the end-point request-resource
        port(Integer.parseInt(argumentList.getConfig().getValueForKey("Port")));
        post("/request-resource", (req, res) -> {
            String responseString = "";

            try {
                agent.getElasticityManager().notifyResourceScaling("");
                res.status(200);
                res.body("Success");
                logger.log(Level.FINE,"Processed Request Successfully",Constants.MAIN_LOG_ID);
            }catch(ClusterElasticityAgentException e){
                logger.log(Level.SEVERE,"Some Exception "+e,Constants.MAIN_LOG_ID);
                res.status(400);
                res.body("Failure");
            }

            return res.body();
        });

        post("/releaseNode", ((request, response) -> {
            agent.getElasticityManager().notifyReleaseNodeRequest(request.body());
            response.status(200);
            return "";
        }));

        post("/createNode",((request, response) -> {
            agent.getElasticityManager().notifyCreateNodeResponse(request.body());
            response.status(200);
            return "";
        }));

        get("/state",((request, response) -> {
           String json = agent.getElasticityManager().notifyStateRequest();
            response.status(200);
            return json;
        }));

        logger.log(Level.INFO,"Starting Collector - Manager Cycle",Constants.MAIN_LOG_ID);
        while(true){
            try {
                argumentList.updateConfig();
                logger.log(Level.FINE,"Updated Config file",Constants.MAIN_LOG_ID);
                logger.log(Level.FINE,""+argumentList.getConfig(),Constants.MAIN_LOG_ID);
                collectorPlugin.notifyTimerExpiry();
                logger.log(Level.FINE,"Collector finished timer expiry",Constants.MAIN_LOG_ID);
                elasticityManager.notifyTimerExpiry();
                logger.log(Level.FINE,"Manager finished timer expiry",Constants.MAIN_LOG_ID);

                Thread.sleep(argumentList.getPollInterval());
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ClusterElasticityAgentException e) {
                e.printStackTrace();
            }
        }

    }

    private Logger createLogger()
    {
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
        logger.setLevel(Level.FINEST);
        try {
            FileHandler managerFileHandler = new FileHandler(arguments.getLogDir() + File.separator + Constants.MANAGER_LOG_NAME +arguments.getConfig().getValueForKey("Id")+".log");
            FileHandler collectorFileHandler = new FileHandler(arguments.getLogDir()+ File.separator + Constants.COLLECTOR_LOG_NAME+arguments.getConfig().getValueForKey("Id")+".log");
            FileHandler centralFileHandler = new FileHandler(arguments.getLogDir() + File.separator + Constants.CENTRAL_LOG_NAME+arguments.getConfig().getValueForKey("Id")+".log");
            FileHandler mainFileHandler = new FileHandler(arguments.getLogDir() + File.separator + Constants.MAIN_LOG_NAME+arguments.getConfig().getValueForKey("Id")+".log");
            ConsoleHandler consoleHandler = new ConsoleHandler();
            managerFileHandler.setFormatter(new SimpleFormatter());
            managerFileHandler.setFilter(record -> record.getParameters()[0].equals(Constants.MANAGER_LOG_ID));
            collectorFileHandler.setFormatter(new SimpleFormatter());
            collectorFileHandler.setFilter(record -> record.getParameters()[0].equals(Constants.COLLECTOR_LOG_ID));
            centralFileHandler.setFormatter(new SimpleFormatter());
            mainFileHandler.setFilter(record -> record.getParameters()[0].equals(Constants.MAIN_LOG_ID));
            mainFileHandler.setFormatter(new SimpleFormatter());
            consoleHandler.setFormatter(new SimpleFormatter());
            consoleHandler.setFilter(record -> record.getLevel().toString().toLowerCase().equals("info") || record.getLevel().toString().toLowerCase().equals("severe") || record.getLevel().toString().toLowerCase().equals("config"));
            logger.addHandler(managerFileHandler);
            logger.addHandler(collectorFileHandler);
            logger.addHandler(centralFileHandler);
            logger.addHandler(mainFileHandler);
            if(arguments.isVerbose())
                logger.addHandler(consoleHandler);
        }
        catch (Exception e) {
            System.err.print("Not able to create logger ");
            e.printStackTrace();
        }
        return logger;
    }
}
