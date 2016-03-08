/**
 * Created by Praveen on 3/2/2016.
 */

import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.CmdLineException;
import java.lang.*;
import java.io.File;

public class CommandLineArguments {

    private String mesosMasterIP;

    private Integer mesosMasterPort;

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

    private static final Integer NULL_PORT = 0;

    private static final Integer DEFAULT_INTERVAL = 5000;

    private static final String DEFAULT_DB_EXECUTOR = "SQLiteDBExecutor";

    public CommandLineArguments() {

        pollInterval = DEFAULT_INTERVAL;
        dbExecutorPluginJar = null;
        dbExecutorPluginMainClass = new String(DEFAULT_DB_EXECUTOR);
    }

    public File getClusterScalerPluginJar() {
        return clusterScalerPluginJar;
    }

    @Option(name="-cluster-scaler-plugin",usage="sets cluster scaler plugin jar file name")
    public void setClusterScalerPluginJar(File clusterScalerPluginJar) throws ClusterElasticityAgentException {
        if(clusterScalerPluginJar.exists())
            this.clusterScalerPluginJar = clusterScalerPluginJar;
        else
            throw new ClusterElasticityAgentException("Cluster Scaler Plugin Jar File Not available!!");
    }

    public File getDbExecutorPluginJar() {
        return dbExecutorPluginJar;
    }

    @Option(name="-db-executor-plugin",usage="sets db executor plugin jar file name")
    public void setDbExecutorPluginJar(File dbExecutorPluginJar) throws ClusterElasticityAgentException {
        if(dbExecutorPluginJar.exists())
            this.dbExecutorPluginJar = dbExecutorPluginJar;
        else
            throw new ClusterElasticityAgentException("DB Executor Plugin Jar Not Available!!");
    }

    public String getClusterScalerPluginMainClass() {
        return clusterScalerPluginMainClass;
    }

    @Option(name="-cluster-scaler-mainclass",usage="sets cluster scaler main class name")
    public void setClusterScalerPluginMainClass(String clusterScalerPluginMainClass) {
        this.clusterScalerPluginMainClass = clusterScalerPluginMainClass;
    }

    public String getDbExecutorPluginMainClass() {
        return dbExecutorPluginMainClass;
    }

    @Option(name="-db-executor-mainclass",usage="sets db executor main class name")
    public void setDbExecutorPluginMainClass(String dbExecutorPluginMainClass) {
        this.dbExecutorPluginMainClass = dbExecutorPluginMainClass;
    }

    public Integer getPollInterval() {
        return pollInterval;
    }

    @Option(name="-poll-interval",usage="Sets Polling Interval in milli seconds")
    public void setPollInterval(Integer pollInterval) {
        this.pollInterval = pollInterval;
    }

    public File getDdlFile() {
        return ddlFile;
    }

    @Option(name="-db-schema",usage="sets DB Schema File")
    public void setDdlFile(File ddlFile) throws ClusterElasticityAgentException {
        if(ddlFile.exists())
            this.ddlFile = ddlFile;
        else
            throw new ClusterElasticityAgentException("DB Schema File doesn't exist");
    }

    public String getMesosMasterIP() {
        return mesosMasterIP;
    }

    @Option(name="-mesos-master-ip",usage="sets mesos master ip")
    public void setMesosMasterIP(String mesosMasterIP) throws ClusterElasticityAgentException {
        if(mesosMasterIP.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")){
            this.mesosMasterIP = mesosMasterIP;
        }
        else{
            throw new ClusterElasticityAgentException("Wrong Mesos Master IP Format!!");
        }
    }

    public Integer getMesosMasterPort() {
        return mesosMasterPort;
    }

    @Option(name="-mesos-master-port",usage="sets mesos master port")
    public void setMesosMasterPort(Integer mesosMasterPort) throws ClusterElasticityAgentException {
        if(mesosMasterPort > NULL_PORT)
            this.mesosMasterPort = mesosMasterPort;
        else
            throw new ClusterElasticityAgentException("Invalid Port specified!!");
    }

    public File getCollectorPluginJar() {
        return collectorPluginJar;
    }

    @Option(name="-collector-plugin",usage="sets collector plugin jar name")
    public void setCollectorPluginJar(File collectorPluginJar) throws ClusterElasticityAgentException {
        if(collectorPluginJar.exists())
            this.collectorPluginJar = collectorPluginJar;
        else
            throw new ClusterElasticityAgentException("Collector Plugin Jar Doesn't Exit!!");
    }

    public File getCemanagerPluginJar() {
        return cemanagerPluginJar;
    }

    @Option(name="-cemanager-plugin",usage="sets CEManager plugin jar name")
    public void setCemanagerPluginJar(File cemanagerPluginJar) throws ClusterElasticityAgentException {
        if(cemanagerPluginJar.exists())
            this.cemanagerPluginJar = cemanagerPluginJar;
        else
            throw new ClusterElasticityAgentException("CEManager Plugin Jar not found!!");
    }

    public String getCollectorPluginMainClass() {
        return collectorPluginMainClass;
    }

    @Option(name="-collector-mainclass",usage="sets Collector plugin main class")
    public void setCollectorPluginMainClass(String collectorPluginMainClass) {
        this.collectorPluginMainClass = collectorPluginMainClass;
    }

    public String getCemanagerPluginMainClass() {
        return cemanagerPluginMainClass;
    }

    @Option(name="-cemanager-mainclass",usage="sets CEManager plugin main class")
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
