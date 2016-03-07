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

    private String collectorPluginMainClass;

    private String cemanagerPluginMainClass = "MesosElasticityPlugin";

    private static final Integer NULL_PORT = 0;

    public CommandLineArguments() {

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
