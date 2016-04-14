/**
 * Created by Praveen on 3/2/2016.
 */

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.Set;

public class CommandLineArguments {

    private static final Integer DEFAULT_INTERVAL = 1000;
    private static final int DEFAULT_PORT = 6000;

    private static final String POLL_INTERVAL = "Poll-Interval";
    private static final String LOG = "Log";
    private static final String PORT = "Port";

    private Integer pollInterval;
    private int port;
    private String logDir = System.getProperty("user.dir");
    private boolean verbose = false;

    private String configFile;
    private  Config config;

    public CommandLineArguments() {

        pollInterval = DEFAULT_INTERVAL;
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
            pollInterval = obj.get(POLL_INTERVAL).getAsInt();
            port = obj.get(POLL_INTERVAL).getAsInt();
            if(obj.has(LOG)) {
                logDir = obj.get(LOG).getAsString();
                obj.remove(LOG);
            }

            obj.remove(POLL_INTERVAL);
            obj.remove(PORT);

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
            port = obj.get(POLL_INTERVAL).getAsInt();
            if(obj.has(LOG)) {
                logDir = obj.get(LOG).getAsString();
                obj.remove(LOG);
            }

            obj.remove(POLL_INTERVAL);
            obj.remove(PORT);

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


    public Integer getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Integer pollInterval) {
        this.pollInterval = pollInterval;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void parseCommandLineArguments(String[] args) {

        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            // handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }
    }
}
