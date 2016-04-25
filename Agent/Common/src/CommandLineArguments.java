/**
 * <h1>CommandLineArguments</h1>
 *
 * @author Praveen
 * @version 1.0
 * @since 2016-03-02
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
    private static final String POLICY_INFO_PLUGIN = "Policy-Info-Plugin";
    private static final String POLICY_INFO_MAIN = "Policy-Info-Main";




    private File collectorPluginJar;
    private File cemanagerPluginJar;
    private File clusterScalerPluginJar;
    private File policyInfoPluginJar;
    private File dbExecutorPluginJar;
    private String collectorPluginMainClass;
    private String cemanagerPluginMainClass;
    private String clusterScalerPluginMainClass;
    private String dbExecutorPluginMainClass;
    private String policyInfoMainClass;
    private Integer pollInterval;
    private File ddlFile;
    private String logDir = System.getProperty("user.dir");
    private boolean verbose = false;


    private String configFile;
    private  Config config;

    /**
     * <h1>CommandLineArguments</h1>
     * Constructor
     */
    public CommandLineArguments() {

        pollInterval = DEFAULT_INTERVAL;
        dbExecutorPluginJar = null;
        dbExecutorPluginMainClass = DEFAULT_DB_EXECUTOR;
    }

    /**
     * <h1>getConfig</h1>
     * Returns the Config object.
     * @return Config
     */
    public Config getConfig() {
        return config;
    }

    /**
     * <h1>updateConfig</h1>
     * Updates the config file.
     */
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
            policyInfoPluginJar = new File(obj.get(POLICY_INFO_PLUGIN).getAsString());
            policyInfoMainClass = obj.get(POLICY_INFO_MAIN).getAsString();
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
            obj.remove(POLICY_INFO_PLUGIN);
            obj.remove(POLICY_INFO_MAIN);

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

    /**
     * <h1>setConfig</h1>
     * @param config
     */
    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * <h1>getConfigFile</h1>
     * @return String
     */
    public String getConfigFile() {
        return configFile;
    }

    /**
     * <h1>setConfigFile</h1>
     * Sets the config file with String given.
     * @param configFile String
     */
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
            policyInfoPluginJar = new File(obj.get(POLICY_INFO_PLUGIN).getAsString());
            policyInfoMainClass = obj.get(POLICY_INFO_MAIN).getAsString();
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
            obj.remove(POLICY_INFO_PLUGIN);
            obj.remove(POLICY_INFO_MAIN);

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

    /**
     * <h1>isVerbose</h1>
     * @return Boolean
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * <h1>setVerbose</h1>
     * @param verbose Boolean
     */
    @Option(name = "-v",usage = "turn on verbose scripting")
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * <h1>getLogDir</h1>
     * @return String
     */
    public String getLogDir() {
        return logDir;
    }

    /**
     * <h1>setLogDir</h1>
     * @param logDir String
     */
    public void setLogDir(String logDir) {
        File dir = new File(logDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.logDir = logDir;
    }

    /**
     * <h1>getClusterScalerPluginJar</h1>
     * @return File
     */
    public File getClusterScalerPluginJar() {
        return clusterScalerPluginJar;
    }

    /**
     * <h1>setClusterScalerPluginJar</h1>
     * @param clusterScalerPluginJar
     * @throws ClusterElasticityAgentException
     */
    public void setClusterScalerPluginJar(File clusterScalerPluginJar) throws ClusterElasticityAgentException {
        if(clusterScalerPluginJar.exists())
            this.clusterScalerPluginJar = clusterScalerPluginJar;
        else
            throw new ClusterElasticityAgentException("Cluster Scaler Plugin Jar File Not available!!");
    }

    /**
     * <h1>getDbExecutorPluginJar</h1>
     * @return File
     */
    public File getDbExecutorPluginJar() {
        return dbExecutorPluginJar;
    }

    /**
     * <h1>setDbExecutorPluginJar</h1>
     * @param dbExecutorPluginJar
     * @throws ClusterElasticityAgentException
     */
    public void setDbExecutorPluginJar(File dbExecutorPluginJar) throws ClusterElasticityAgentException {
        if(dbExecutorPluginJar.exists())
            this.dbExecutorPluginJar = dbExecutorPluginJar;
        else
            throw new ClusterElasticityAgentException("DB Executor Plugin Jar Not Available!!");
    }

    /**
     * <h1>getClusterScalerPluginMainClass</h1>
     * @return String
     */
    public String getClusterScalerPluginMainClass() {
        return clusterScalerPluginMainClass;
    }

    /**
     * <h1>setClusterScalerPluginMainClass</h1>
     * @param clusterScalerPluginMainClass
     */
    public void setClusterScalerPluginMainClass(String clusterScalerPluginMainClass) {
        this.clusterScalerPluginMainClass = clusterScalerPluginMainClass;
    }

    /**
     * <h1>getDbExecutorPluginMainClass</h1>
     * @return String
     */
    public String getDbExecutorPluginMainClass() {
        return dbExecutorPluginMainClass;
    }

    /**
     * <h1>setDbExecutorPluginMainClass</h1>
     * @param dbExecutorPluginMainClass
     */
    public void setDbExecutorPluginMainClass(String dbExecutorPluginMainClass) {
        this.dbExecutorPluginMainClass = dbExecutorPluginMainClass;
    }

    /**
     * <h1>getPollInterval</h1>
     * @return Integer
     */
    public Integer getPollInterval() {
        return pollInterval;
    }

    /**
     * <h1>setPollInterval</h1>
     * @param pollInterval
     */
    public void setPollInterval(Integer pollInterval) {
        this.pollInterval = pollInterval;
    }

    /**
     * <h1>getDd1File</h1>
     * @return File
     */
    public File getDdlFile() {
        return ddlFile;
    }

    /**
     * <h1>setDd1File</h1>
     * @param ddlFile
     * @throws ClusterElasticityAgentException
     */
    public void setDdlFile(File ddlFile) throws ClusterElasticityAgentException {
        if(ddlFile.exists())
            this.ddlFile = ddlFile;
        else
            throw new ClusterElasticityAgentException("DB Schema File doesn't exist");
    }

    /**
     * <h1>getCollectorPluginJar</h1>
     * @return File
     */
    public File getCollectorPluginJar() {
        return collectorPluginJar;
    }

    /**
     * <h1>setCollectorPluginJar</h1>
     * @param collectorPluginJar
     * @throws ClusterElasticityAgentException
     */
    public void setCollectorPluginJar(File collectorPluginJar) throws ClusterElasticityAgentException {
        if(collectorPluginJar.exists())
            this.collectorPluginJar = collectorPluginJar;
        else
            throw new ClusterElasticityAgentException("Collector Plugin Jar Doesn't Exit!!");
    }

    /**
     * <h1>getCemanagerPluginJar</h1>
     * @return File
     */
    public File getCemanagerPluginJar() {
        return cemanagerPluginJar;
    }

    /**
     * <h1>setCemanagerPluginJar</h1>
     * @param cemanagerPluginJar
     * @throws ClusterElasticityAgentException
     */
    public void setCemanagerPluginJar(File cemanagerPluginJar) throws ClusterElasticityAgentException {
        if(cemanagerPluginJar.exists())
            this.cemanagerPluginJar = cemanagerPluginJar;
        else
            throw new ClusterElasticityAgentException("CEManager Plugin Jar not found!!");
    }

    /**
     * <h1>getCollectorPluginMainClass</h1>
     * @return String
     */
    public String getCollectorPluginMainClass() {
        return collectorPluginMainClass;
    }

    /**
     * <h1>setCollectorPluginMainClass</h1>
     * @param collectorPluginMainClass
     */
    public void setCollectorPluginMainClass(String collectorPluginMainClass) {
        this.collectorPluginMainClass = collectorPluginMainClass;
    }

    /**
     * <h1>getCemanagerPluginMainClass</h1>
     * @return String
     */
    public String getCemanagerPluginMainClass() {
        return cemanagerPluginMainClass;
    }

    public File getPolicyInfoPluginJar() {
        return policyInfoPluginJar;
    /**
     * <h1>setCemanagerPluginMainClass</h1>
     * @param cemanagerPluginMainClass
     */
    public void setCemanagerPluginMainClass(String cemanagerPluginMainClass) {
        this.cemanagerPluginMainClass = cemanagerPluginMainClass;
    }


    public String getPolicyInfoMainClass() {
        return policyInfoMainClass;
    }

    /**
     * <h1>parseCommandLineArguments</h1>
     * @param args
     * @throws ClusterElasticityAgentException
     */
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
