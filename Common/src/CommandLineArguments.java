/**
 * Created by Praveen on 3/2/2016.
 */

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.CmdLineException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.*;
import java.io.File;
import java.util.Map;
import java.util.Set;

public class CommandLineArguments {

    private static final Integer NULL_PORT = 0;
    private static final Integer DEFAULT_INTERVAL = 5000;
    private static final String DEFAULT_DB_EXECUTOR = "SQLiteDBExecutor";

    private static final String MANAGER_PLUGIN = "Manager-Plugin";
    private static final String COLLECTOR_PLUGIN = "Collector-Plugin";
    private static final String CLUSTER_SCALER_PLUGIN = "Cluster-Scaler-Plugin";
    private static final String DB_EXECUTOR_PLUGIN = "DB-Executor-Plugin";
    private static final String MANAGER_MAIN = "Manager-Main";
    private static final String COLLECTOR_MAIN = "Collector-Main";
    private static final String DATABASE_MAIN = "Database-Main";
    private static final String CLUSTER_SCALER_MAIN = "Cluster-Scaler-Main";
    private static final String DDL_SCRIPT = "DDL-Script";
    private static final String POLL_INTERVAL = "Poll-Interval";
    private static final String LOG = "Log";

    private File collectorPluginJar;
    private File cemanagerPluginJar;
    private File clusterScalerPluginJar;
    private File dbExecutorPluginJar;
    private String collectorPluginMainClass;
    private String cemanagerPluginMainClass;
    private String clusterScalerPluginMainClass;
    private String dbExecutorPluginMainClass;
    private Integer pollInterval;
    private File ddlFile;
    private String logDir = System.getProperty("user.dir");
    private boolean verbose = false;


    private String configFile;
    private  Config config;

    public CommandLineArguments() {

        pollInterval = DEFAULT_INTERVAL;
        dbExecutorPluginJar = null;
        dbExecutorPluginMainClass = DEFAULT_DB_EXECUTOR;
    }


    public Config getConfig() {
        return config;
    }

    public void updateConfig()
    {
        File file = new File(configFile);
        Gson gson = new Gson();
        try {

            JsonObject obj = gson.fromJson(new FileReader(file),JsonObject.class);
            cemanagerPluginJar = new File(obj.get(MANAGER_PLUGIN).getAsString());
            obj.remove(MANAGER_PLUGIN);
            collectorPluginJar = new File(obj.get(COLLECTOR_PLUGIN).getAsString());
            clusterScalerPluginJar = new File(obj.get(CLUSTER_SCALER_PLUGIN).getAsString());
            dbExecutorPluginJar = new File(obj.get(DB_EXECUTOR_PLUGIN).getAsString());
            ddlFile = new File(obj.get(DDL_SCRIPT).getAsString());
            pollInterval = obj.get(POLL_INTERVAL).getAsInt();
            collectorPluginMainClass = obj.get(COLLECTOR_MAIN).getAsString();
            dbExecutorPluginMainClass = obj.get(DATABASE_MAIN).getAsString();
            cemanagerPluginMainClass = obj.get(MANAGER_MAIN).getAsString();
            clusterScalerPluginMainClass = obj.get(CLUSTER_SCALER_MAIN).getAsString();
            if(obj.has(LOG)) {
                logDir = obj.get(LOG).getAsString();
                obj.remove(LOG);
            }

            obj.remove(MANAGER_PLUGIN);
            obj.remove(COLLECTOR_PLUGIN);
            obj.remove(CLUSTER_SCALER_PLUGIN);
            obj.remove(DB_EXECUTOR_PLUGIN);
            obj.remove(DDL_SCRIPT);
            obj.remove(POLL_INTERVAL);
            obj.remove(MANAGER_MAIN);
            obj.remove(COLLECTOR_MAIN);
            obj.remove(CLUSTER_SCALER_MAIN);
            obj.remove(DATABASE_MAIN);

            Set<Map.Entry<String,JsonElement>> members = obj.entrySet();

           config.clear();

            for(Map.Entry<String,JsonElement> member : members)
            {
                try {
                    config.addValueForKey(member.getKey(),member.getValue().getAsString());
                } catch (IllegalStateException e) {
                    config.addValueForKey(member.getKey(),member.getValue().getAsJsonArray().toString());
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getConfigFile() {
        return configFile;
    }

    @Option(name = "-config", usage = "Specifies location of config.json", required = true)
    public void setConfigFile(String configFile) {
        File file = new File(configFile);
        Gson gson = new Gson();
        try {

            JsonObject obj = gson.fromJson(new FileReader(file),JsonObject.class);
            cemanagerPluginJar = new File(obj.get(MANAGER_PLUGIN).getAsString());
            obj.remove(MANAGER_PLUGIN);
            collectorPluginJar = new File(obj.get(COLLECTOR_PLUGIN).getAsString());
            clusterScalerPluginJar = new File(obj.get(CLUSTER_SCALER_PLUGIN).getAsString());
            dbExecutorPluginJar = new File(obj.get(DB_EXECUTOR_PLUGIN).getAsString());
            ddlFile = new File(obj.get(DDL_SCRIPT).getAsString());
            pollInterval = obj.get(POLL_INTERVAL).getAsInt();
            collectorPluginMainClass = obj.get(COLLECTOR_MAIN).getAsString();
            dbExecutorPluginMainClass = obj.get(DATABASE_MAIN).getAsString();
            cemanagerPluginMainClass = obj.get(MANAGER_MAIN).getAsString();
            clusterScalerPluginMainClass = obj.get(CLUSTER_SCALER_MAIN).getAsString();
            if(obj.has(LOG)) {
                logDir = obj.get(LOG).getAsString();
                obj.remove(LOG);
            }

            obj.remove(MANAGER_PLUGIN);
            obj.remove(COLLECTOR_PLUGIN);
            obj.remove(CLUSTER_SCALER_PLUGIN);
            obj.remove(DB_EXECUTOR_PLUGIN);
            obj.remove(DDL_SCRIPT);
            obj.remove(POLL_INTERVAL);
            obj.remove(MANAGER_MAIN);
            obj.remove(COLLECTOR_MAIN);
            obj.remove(CLUSTER_SCALER_MAIN);
            obj.remove(DATABASE_MAIN);

            Set<Map.Entry<String,JsonElement>> members = obj.entrySet();

            config = new Config();

            for(Map.Entry<String,JsonElement> member : members)
            {
                try {
                    config.addValueForKey(member.getKey(),member.getValue().getAsString());
                } catch (IllegalStateException e) {
                    config.addValueForKey(member.getKey(),member.getValue().getAsJsonArray().toString());
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.configFile = configFile;
    }

    public boolean isVerbose() {
        return verbose;
    }

    @Option(name = "-v",usage = "turn on verbose scripting")
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        File dir = new File(logDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.logDir = logDir;
    }

    public File getClusterScalerPluginJar() {
        return clusterScalerPluginJar;
    }

    public void setClusterScalerPluginJar(File clusterScalerPluginJar) throws ClusterElasticityAgentException {
        if(clusterScalerPluginJar.exists())
            this.clusterScalerPluginJar = clusterScalerPluginJar;
        else
            throw new ClusterElasticityAgentException("Cluster Scaler Plugin Jar File Not available!!");
    }

    public File getDbExecutorPluginJar() {
        return dbExecutorPluginJar;
    }

    public void setDbExecutorPluginJar(File dbExecutorPluginJar) throws ClusterElasticityAgentException {
        if(dbExecutorPluginJar.exists())
            this.dbExecutorPluginJar = dbExecutorPluginJar;
        else
            throw new ClusterElasticityAgentException("DB Executor Plugin Jar Not Available!!");
    }

    public String getClusterScalerPluginMainClass() {
        return clusterScalerPluginMainClass;
    }

    public void setClusterScalerPluginMainClass(String clusterScalerPluginMainClass) {
        this.clusterScalerPluginMainClass = clusterScalerPluginMainClass;
    }

    public String getDbExecutorPluginMainClass() {
        return dbExecutorPluginMainClass;
    }

    public void setDbExecutorPluginMainClass(String dbExecutorPluginMainClass) {
        this.dbExecutorPluginMainClass = dbExecutorPluginMainClass;
    }

    public Integer getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Integer pollInterval) {
        this.pollInterval = pollInterval;
    }

    public File getDdlFile() {
        return ddlFile;
    }

    public void setDdlFile(File ddlFile) throws ClusterElasticityAgentException {
        if(ddlFile.exists())
            this.ddlFile = ddlFile;
        else
            throw new ClusterElasticityAgentException("DB Schema File doesn't exist");
    }

    public File getCollectorPluginJar() {
        return collectorPluginJar;
    }

    public void setCollectorPluginJar(File collectorPluginJar) throws ClusterElasticityAgentException {
        if(collectorPluginJar.exists())
            this.collectorPluginJar = collectorPluginJar;
        else
            throw new ClusterElasticityAgentException("Collector Plugin Jar Doesn't Exit!!");
    }

    public File getCemanagerPluginJar() {
        return cemanagerPluginJar;
    }

    public void setCemanagerPluginJar(File cemanagerPluginJar) throws ClusterElasticityAgentException {
        if(cemanagerPluginJar.exists())
            this.cemanagerPluginJar = cemanagerPluginJar;
        else
            throw new ClusterElasticityAgentException("CEManager Plugin Jar not found!!");
    }

    public String getCollectorPluginMainClass() {
        return collectorPluginMainClass;
    }

    public void setCollectorPluginMainClass(String collectorPluginMainClass) {
        this.collectorPluginMainClass = collectorPluginMainClass;
    }

    public String getCemanagerPluginMainClass() {
        return cemanagerPluginMainClass;
    }

    public void setCemanagerPluginMainClass(String cemanagerPluginMainClass) {
        this.cemanagerPluginMainClass = cemanagerPluginMainClass;
    }

    public void parseCommandLineArguments(String[] args) throws ClusterElasticityAgentException {

        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            // handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            throw new ClusterElasticityAgentException("Command Line Arguments Error!!");
        }
    }
}
