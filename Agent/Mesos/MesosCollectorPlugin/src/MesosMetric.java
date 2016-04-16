import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <h1>MesosMetric</h1>
 * This class fetches, processes and inserts data
 * into respective tables from the data received from
 * the Mesos Slave Nodes.
 *
 * @author Kovit
 * @version 1.0
 * @since 2016-03-09
 */

public final class MesosMetric implements ICollectorPluginByTable {

    private final static Logger LOGGER = GlobalLogger.globalLogger;

    /**
     * <h1>fetch</h1>
     * fetches the data from Slave Nodes and
     * @param data type -> List<Data>
     * @param masterAddr type -> String
     * @return List<ITableInfo> Returns all the TableInfo from the slave
     */
    @Override
    public List<ITableInfo> fetch(List<Data> data, String masterAddr) {
        List<SlaveDetails> slaveLst = new ArrayList<>();
        List<FrameworkSlaveRelationship> runsOn = new ArrayList<>();
        List<FrameworkDetails> frameworkDetailsLst = new ArrayList<>();

        String stateSummary = null;
        String httpReq = "http://" + masterAddr + "/master/state-summary";
        try {
            stateSummary = HTTP.executeRequest(httpReq);
            LOGGER.log(Level.FINE, "[Collector Plugin] Successfully executed HTTP request " + httpReq,Constants.COLLECTOR_LOG_ID);
            LOGGER.log(Level.FINE, "[Collector Plugin] State Summary:",Constants.COLLECTOR_LOG_ID);
            LOGGER.log(Level.FINE, stateSummary,Constants.COLLECTOR_LOG_ID);
        } catch (IOException e) {
            String errorMsg = "[Collector Plugin] Failed to execute HTTP request " + httpReq
                    + " .Reason: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg,Constants.COLLECTOR_LOG_ID);
        }
        if (stateSummary!= null) {
            parseStateSummary(stateSummary, slaveLst, frameworkDetailsLst, runsOn);
            populateSlaveUtilization(slaveLst);
            return convertIntoTableRows(slaveLst, frameworkDetailsLst, runsOn);
        }

        LOGGER.log(Level.SEVERE, "As master could not be reached nothing will be inserted in the db",Constants.COLLECTOR_LOG_ID);
        return null;
    }

    /**
     * <h1>convertIntoTableRows</h1>
     * Converts the data into tables
     * @param slaveLst
     * @param frameworkDetailsLst
     * @param runsOn
     * @return List<ITableInfo>
     */
    private List<ITableInfo> convertIntoTableRows(List<SlaveDetails> slaveLst,
                                                  List<FrameworkDetails> frameworkDetailsLst,
                                                  List<FrameworkSlaveRelationship> runsOn) {
        List<ITableInfo> lst = new ArrayList<>();
        slaveTableRows(slaveLst, lst);
        frameworkTableRows(frameworkDetailsLst, lst);
        runsOnTableRows(runsOn, lst);
        LOGGER.log(Level.FINE, "[Collector Plugin] Total no of rows to be inserted " + lst.size(),Constants.COLLECTOR_LOG_ID);
        return lst;
    }

    /**
     * <h1>runsOnTableRows</h1>
     * Fills te runsOn Table
     * @param runsOn
     * @param lst
     */
    private void runsOnTableRows(List<FrameworkSlaveRelationship> runsOn, List<ITableInfo> lst) {
        for(FrameworkSlaveRelationship fsr: runsOn) {
            if (fsr.getSlave().isReachable()) {
                ITableInfo t = new TableInfo("Runs_On");
                t.addColName("Framework_ID").addColValue(fsr.getFrameworkId())
                        .addColName("Slave_ID").addColValue(fsr.getSlaveId());
                t.setPriority(2);
                LOGGER.log(Level.FINE, "[Collector Plugin] runs_on table row " + t.toString(),Constants.COLLECTOR_LOG_ID);
                lst.add(t);
            }
            else {
                LOGGER.log(Level.SEVERE, "Slave " + fsr.getSlave().getHostName() + " is unreachable, hence no rows for this slave will " +
                        "be inserted in the Runs_On table for framework " + fsr.getFrameworkId(),Constants.COLLECTOR_LOG_ID);
            }
        }
    }

    /**
     * <h1>frameworkTableRows</h1>
     * Fills the Framework Table
     * @param frameworkDetailsLst
     * @param lst
     */
    private void frameworkTableRows(List<FrameworkDetails> frameworkDetailsLst, List<ITableInfo> lst) {
        for(FrameworkDetails f: frameworkDetailsLst) {
            ITableInfo t = new TableInfo("Framework");
            t.addColName("Framework_ID").addColValue(f.getFrameworkId())
                    .addColName("Name").addColValue(f.getName())
                    .addColName("CPU").addColValue(f.getCpu())
                    .addColName("Memory").addColValue(f.getMemory())
                    .addColName("Active").addColValue(f.getActive())
                    .addColName("Scheduled_Tasks").addColValue(f.getScheduledTasks())
                    .addColName("TimeStamp").addColValue(new Date().toString());
            t.setPriority(1);
            LOGGER.log(Level.FINE, "[Collector Plugin] framework table row " + t.toString(),Constants.COLLECTOR_LOG_ID);
            lst.add(t);
        }
    }

    /**
     * <h1>slaveTableRows</h1>
     * Fills the Slave Table
     * @param slaveLst
     * @param lst
     */
    private void slaveTableRows(List<SlaveDetails> slaveLst, List<ITableInfo> lst) {
        for(SlaveDetails s: slaveLst) {
            if (s.isReachable()) {
                ITableInfo t = new TableInfo("Slave");
                t.addColName("Slave_ID").addColValue(s.getSlaveId())
                        .addColName("Load_5min").addColValue(s.getLoad5Min())
                        .addColName("Free_Memory").addColValue(s.getFreeMemory())
                        .addColName("Total_Memory").addColValue(s.getTotalMemory())
                        .addColName("CPU").addColValue(s.getCpu())
                        .addColName("Allocated_CPU").addColValue(s.getAllocatedCpu())
                        .addColName("Hostname").addColValue(s.getHostName())
                        .addColName("IP").addColValue(s.getIp())
                        .addColName("TimeStamp").addColValue(new Date().toString());
                t.setPriority(0);
                LOGGER.log(Level.FINE, "[Collector Plugin] slave table row " + t.toString(),Constants.COLLECTOR_LOG_ID);
                lst.add(t);
            }
        }
    }

    /**
     * <h1>populateSlaveUtilization</h1>
     * Populate All the Slave Objects
     * @param slaveLst
     */
    private void populateSlaveUtilization(List<SlaveDetails> slaveLst) {
        for(SlaveDetails slave: slaveLst) {
            String slaveMetrics = null;
            String httpReq = "http://" + slave.getIpNPort() + "/metrics/snapshot";
            try {
                slaveMetrics = HTTP.executeRequest(httpReq);
                LOGGER.log(Level.FINE, "[Collector Plugin] Successfully executed HTTP request " + httpReq,Constants.COLLECTOR_LOG_ID);
                LOGGER.log(Level.FINE, "[Collector Plugin] Slave Metrics:",Constants.COLLECTOR_LOG_ID);
                LOGGER.log(Level.FINE, slaveMetrics,Constants.COLLECTOR_LOG_ID);
            }
            catch (IOException e) {
                String errorMsg = "[Collector Plugin] Failed to execute HTTP request " + httpReq
                        + " .Reason: " + e.getMessage();
                LOGGER.log(Level.SEVERE, errorMsg,Constants.COLLECTOR_LOG_ID);
            }
            if (slaveMetrics != null) {
                parseSlaveMetrics(slave, slaveMetrics);
            }
            else {
                slave.setReachable(false);
                LOGGER.log(Level.SEVERE, "Unreachable slave " + slave.getHostName() +
                        ". Hence no rows will be inserted for this slave",Constants.COLLECTOR_LOG_ID);
            }
        }
    }

    /**
     * <h1>parseSlaveMetrics</h1>
     * Parse the Mesos Slave nodes and
     * sets it into Slave objects
     * @param slave
     * @param slaveMetricsResp
     */
    private void parseSlaveMetrics(SlaveDetails slave, String slaveMetricsResp) {
        JsonElement slaveMetrics = new JsonParser().parse(slaveMetricsResp);
        JsonObject smObj = slaveMetrics.getAsJsonObject();
        slave.setAllocatedCpu(smObj.get("slave/cpus_used").getAsInt())
                .setCpu(smObj.get("slave/cpus_total").getAsInt())
                .setFreeMemory(smObj.get("system/mem_free_bytes").getAsLong())
                .setTotalMemory(smObj.get("system/mem_total_bytes").getAsLong())
                .setLoad5Min(smObj.get("system/load_5min").getAsFloat());
    }

    /**
     * <h1>parseStateSummary</h1>
     * gets the Framework and Slave details
     * @param stateSummaryResp
     * @param slaveLst
     * @param frameworkDetailsLst
     * @param runsOn
     */
    private static void parseStateSummary(String stateSummaryResp, List<SlaveDetails> slaveLst,
                                          List<FrameworkDetails> frameworkDetailsLst,
                                          List<FrameworkSlaveRelationship> runsOn) {
        JsonElement stateSummary = new JsonParser().parse(stateSummaryResp);
        JsonArray slaves = stateSummary.getAsJsonObject().getAsJsonArray("slaves");
        JsonArray frameworks = stateSummary.getAsJsonObject().getAsJsonArray("frameworks");

        processSlaveDetails(slaves, slaveLst, runsOn);
        processFrameworkDetails(frameworks, frameworkDetailsLst);
    }

    /**
     * <h1>processFrameworkDetails</h1>
     * processes the Framework Details.
     * @param frameworks
     * @param frameworkDetailsLst
     */
    private static void processFrameworkDetails(JsonArray frameworks, List<FrameworkDetails> frameworkDetailsLst) {
        for(final JsonElement framework : frameworks) {
            JsonObject fObj = framework.getAsJsonObject();
            JsonObject usedResourcesObj = fObj.getAsJsonObject("used_resources");
            frameworkDetailsLst.add(new FrameworkDetails().setFrameworkId(fObj.get("id").getAsString())
                    .setName(fObj.get("name").getAsString()).setCpu(usedResourcesObj.get("cpus").getAsInt())
                    .setMemory(usedResourcesObj.get("mem").getAsInt())
                    .setScheduledTasks(getScheduledTasks(fObj)).setActive(fObj.get("active").getAsBoolean()));
        }
    }

    /**
     * <h1>getScheduledTasks</h1>
     * Provides the Total number of tasks.
     * @param fObj
     * @return Tasks type Int
     */
    private static int getScheduledTasks(JsonObject fObj) {
        return fObj.get("TASK_STAGING").getAsInt() + fObj.get("TASK_STARTING").getAsInt() + fObj.get("TASK_RUNNING").getAsInt();
    }

    /**
     * <h1>processSlaveDetails</h1>
     * Processes all the Slave details.
     * @param slaves
     * @param slaveLst
     * @param runsOn
     */
    private static void processSlaveDetails(JsonArray slaves, List<SlaveDetails> slaveLst, List<FrameworkSlaveRelationship> runsOn) {
        for(final JsonElement slave : slaves) {
            SlaveDetails s = new SlaveDetails();
            slaveLst.add(s);
            JsonObject slaveObj = slave.getAsJsonObject();
            s.setSlaveId(slaveObj.get("id").getAsString());
            s.setHostName(slaveObj.get("hostname").getAsString());
            String pid = slaveObj.get("pid").getAsString();
            int startIndexOfIp = pid.lastIndexOf("@") + 1;
            s.setIp(pid.substring(startIndexOfIp, pid.lastIndexOf(":")));
            s.setIpNPort(pid.substring(startIndexOfIp));

            JsonArray fIds = slaveObj.getAsJsonArray("framework_ids");
            for (final JsonElement fid: fIds) {
                runsOn.add(new FrameworkSlaveRelationship().setFrameworkId(fid.getAsString()).setSlaveId(s.getSlaveId()).setSlave(s));
            }
        }
    }
}
