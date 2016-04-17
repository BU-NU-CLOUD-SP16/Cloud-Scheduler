import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.*;

import static java.lang.Thread.sleep;
import static spark.Spark.port;
import static spark.Spark.post;

/**
 * Created by Praveen on 3/26/2016.
 */
public class Overlord {

    private CommandLineArguments commandLineArguments;

    private PolicyPlugin policyPlugin;
    private CloudInfoCollectorPlugin cloudInfoCollectorPlugin;

    public static Logger logger;


    public Overlord(CommandLineArguments commandLineArguments) {
        this.commandLineArguments = commandLineArguments;
        setupLogger();
    }

    public PolicyPlugin getPolicyPlugin() {
        return policyPlugin;
    }

    public void setPolicyPlugin(PolicyPlugin policyPlugin) {
        this.policyPlugin = policyPlugin;
    }

    public CloudInfoCollectorPlugin getCloudInfoCollectorPlugin() {
        return cloudInfoCollectorPlugin;
    }

    public void setCloudInfoCollectorPlugin(CloudInfoCollectorPlugin cloudInfoCollectorPlugin) {
        this.cloudInfoCollectorPlugin = cloudInfoCollectorPlugin;
    }

    private Logger createLogger()
    {
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
        logger.setLevel(Level.FINE);
        new File(commandLineArguments.getLogDir()).mkdirs();
        try {
            FileHandler overlordFileHandler = new FileHandler(commandLineArguments.getLogDir() + File.separator + Constants.LOG_NAME +"-"+ System.currentTimeMillis()+".log");
            ConsoleHandler consoleHandler = new ConsoleHandler();
            overlordFileHandler.setFormatter(new SimpleFormatter());
            consoleHandler.setFormatter(new SimpleFormatter());
            consoleHandler.setFilter(record -> record.getLevel().toString().toLowerCase().equals("info") || record.getLevel().toString().toLowerCase().equals("severe") || record.getLevel().toString().toLowerCase().equals("config"));
            logger.addHandler(overlordFileHandler);
            if(commandLineArguments.isVerbose())
                logger.addHandler(consoleHandler);
        }
        catch (Exception e) {
            System.err.print("Not able to create logger ");
            e.printStackTrace();
        }
        return logger;
    }

    private void createInstances()
    {
        try {

            Class policyClass = Class.forName(commandLineArguments.getPolicyPluginMainClass());
            logger.log(Level.FINE,"Got Class "+commandLineArguments.getPolicyPluginMainClass());
            Class cloudInfoCollectorClass = Class.forName(commandLineArguments.getCloudInfoCollectorMainClass());
            logger.log(Level.FINE,"Got Class "+commandLineArguments.getCloudInfoCollectorMainClass());

            policyPlugin = (PolicyPlugin) policyClass.getConstructor().newInstance();
            logger.log(Level.FINE,"Created Policy Plugin Instance");
            cloudInfoCollectorPlugin = (CloudInfoCollectorPlugin) cloudInfoCollectorClass.getConstructor().newInstance();
            logger.log(Level.FINE,"Created CloudInfoCollector Plugin");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setupLogger()
    {
        logger = createLogger();
        GlobalLogger.globalLogger = logger;
    }

    public static void main(String args[]) throws InterruptedException {


        CommandLineArguments commandLineArguments = new CommandLineArguments();
        commandLineArguments.parseCommandLineArguments(args);

        Overlord overlordHandle = new Overlord(commandLineArguments);

        logger.log(Level.INFO,"Overlord Started");

        try {
            if (commandLineArguments.getCloudInfoCollectorJar() != null)
            {
                ModuleLoader.addFile(commandLineArguments.getCloudInfoCollectorJar());
            }

            if (commandLineArguments.getPolicyPluginJar() != null)
            {
                ModuleLoader.addFile(commandLineArguments.getPolicyPluginJar());
            }
        }

        catch (Exception ex)
        {
            logger.log(Level.SEVERE,""+ex.getMessage());
        }

        logger.log(Level.INFO,"Loaded All Modules");

        overlordHandle.createInstances();

        port(commandLineArguments.getPort());
        post("/registerCEAgent", (request, response) -> {
            logger.log(Level.INFO,"Processing Request to Register from "+request.ip());
            overlordHandle.getPolicyPlugin().registerAgent(request.ip(),request.body());
            logger.log(Level.INFO,"Registered New Agent at "+request.ip()+" Successfully");
            return "";
        });

        post("/requestNode", (request, response) -> {
            logger.log(Level.INFO,"Processing Request for Node from "+request.ip());
            String decision = overlordHandle.getPolicyPlugin().requestNode(request.body());
            response.type("application/json");
            response.status(200);
            logger.log(Level.INFO,"Processed Decision is "+decision);
            return decision;
        });

        logger.log(Level.INFO,"Endpoints Setup Done");


        while (true) {
            try {
                commandLineArguments.updateConfig();
                logger.log(Level.FINE,"Updated Config");
                overlordHandle.getPolicyPlugin().setup(commandLineArguments.getConfig());
                logger.log(Level.FINE,"Executed Setup of Policy Plugin");
                overlordHandle.getCloudInfoCollectorPlugin().setup(commandLineArguments.getConfig());
                logger.log(Level.FINE,"Executed Setup of CloudInfoCollector Plugin");
                ArrayList<Node> nodes = overlordHandle.getCloudInfoCollectorPlugin().listNodes();
                logger.log(Level.INFO,"Slaves in Cloud = "+nodes.size());
                HashMap<String,String> hostnames = overlordHandle.getPolicyPlugin().getAgentHostnames();
                logger.log(Level.INFO,"Agents Registered = "+hostnames.keySet().size());
                HashMap<String,String> states = new HashMap<>();
                for (String id : hostnames.keySet())
                {
                    String state = overlordHandle.getAgentState(hostnames.get(id));
                    states.put(id,state);
                    logger.log(Level.FINE,"Pulled State From Agent with "+id+" and it is "+state);
                }
                logger.log(Level.INFO,"Pulled State from "+hostnames.keySet().size()+" agents");

                overlordHandle.getPolicyPlugin().updateState(states,nodes);
                logger.log(Level.INFO,"Finished Updating State");

                sleep(commandLineArguments.getPollInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private String getAgentState(String hostname)
    {
        try {
            HttpResponse<String> response = Unirest.get("http://"+hostname+"/state").asString();
            return response.getBody();
        }

        catch (UnirestException ex)
        {
            logger.log(Level.SEVERE,""+ex.getMessage());
        }
        return "";
    }

}
