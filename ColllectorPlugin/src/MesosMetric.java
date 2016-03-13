import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by kovit on 3/9/2016.
 */

public final class MesosMetric implements ICollectorPluginByTable {

    private final static Logger LOGGER = Logger.getLogger(CollectorPluginFrameworkImpl.COLLECTOR_LOGGER_NAME);

    @Override
    public List<ITableInfo> fetch(List<Data> data, String masterAddr) {
        List<SlaveDetails> slaveLst = new ArrayList<>();
        List<FrameworkSlaveRelationship> runsOn = new ArrayList<>();
        List<FrameworkDetails> frameworkDetailsLst = new ArrayList<>();

        String stateSummary = null;
        String httpReq = "http://" + masterAddr + "/master/state-summary";
        try {
            stateSummary = HTTP.executeRequest(httpReq);
            LOGGER.log(Level.FINE, "[Collector Plugin] Successfully executed HTTP request " + httpReq );
            LOGGER.log(Level.FINE, "[Collector Plugin] State Summary:");
            LOGGER.log(Level.FINE, stateSummary);
        } catch (IOException e) {
            String errorMsg = "[Collector Plugin] Failed to execute HTTP request " + httpReq
                    + " .Reason: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg);
            LOGGER.log(Level.FINE, "[Collector Plugin] Retrying to execute HTTP request " + httpReq);
            try {
                stateSummary = HTTP.executeRequest(httpReq);
                LOGGER.log(Level.FINE, "[Collector Plugin] Successfully executed HTTP request " + httpReq );
                LOGGER.log(Level.FINE, "[Collector Plugin] State Summary:");
                LOGGER.log(Level.FINE, stateSummary);
            } catch (IOException e1) {
                errorMsg = "[Collector Plugin] Retry attempt 1 Failed to execute HTTP request " + httpReq
                        + " .Reason: " + e.getMessage();
                LOGGER.log(Level.SEVERE, errorMsg);
                // Not throwing any exception as the master must be temporary down and new master will be elected
                //throw new IllegalStateException(errorMsg, e1);
            }
        }
        if (stateSummary!= null) {
            parseStateSummary(stateSummary, slaveLst, frameworkDetailsLst, runsOn);
            populateSlaveUtilization(slaveLst);
            return convertIntoTableRows(slaveLst, frameworkDetailsLst, runsOn);
        }

        LOGGER.log(Level.SEVERE, "As master could not be reached nothing will be inserted in the db");
        return null;
    }

    private List<ITableInfo> convertIntoTableRows(List<SlaveDetails> slaveLst,
                                                  List<FrameworkDetails> frameworkDetailsLst,
                                                  List<FrameworkSlaveRelationship> runsOn) {
        List<ITableInfo> lst = new ArrayList<>();
        slaveTableRows(slaveLst, lst);
        frameworkTableRows(frameworkDetailsLst, lst);
        runsOnTableRows(runsOn, lst);
        LOGGER.log(Level.FINE, "[Collector Plugin] Total no of rows to be inserted " + lst.size());
        return lst;
    }

    private void runsOnTableRows(List<FrameworkSlaveRelationship> runsOn, List<ITableInfo> lst) {
        for(FrameworkSlaveRelationship fsr: runsOn) {
            ITableInfo t = new TableInfo("Runs_On");
            t.addColName("Framework_ID").addColValue(fsr.getFrameworkId())
                    .addColName("Slave_ID").addColValue(fsr.getSlaveId());
            t.setPriority(2);
            LOGGER.log(Level.FINE, "[Collector Plugin] runs_on table row " + t.toString());
            lst.add(t);
        }
    }

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
            LOGGER.log(Level.FINE, "[Collector Plugin] framework table row " + t.toString());
            lst.add(t);
        }
    }

    private void slaveTableRows(List<SlaveDetails> slaveLst, List<ITableInfo> lst) {
        for(SlaveDetails s: slaveLst) {
            if (s.isReachable()) {
                ITableInfo t = new TableInfo("Slave");
                t.addColName("Slave_ID").addColValue(s.getSlaveId())
                        .addColName("Load_5min").addColValue(s.getLoad5Min())
                        .addColName("Free_Memory").addColValue(s.getTotalMemory() - s.getFreeMemory())
                        .addColName("Total_Memory").addColValue(s.getTotalMemory())
                        .addColName("CPU").addColValue(s.getCpu())
                        .addColName("Allocated_CPU").addColValue(s.getAllocatedCpu())
                        .addColName("Hostname").addColValue(s.getHostName())
                        .addColName("IP").addColValue(s.getIp())
                        .addColName("TimeStamp").addColValue(new Date().toString());
                t.setPriority(0);
                LOGGER.log(Level.FINE, "[Collector Plugin] slave table row " + t.toString());
                lst.add(t);
            }
        }
    }

    private void populateSlaveUtilization(List<SlaveDetails> slaveLst) {
        for(SlaveDetails slave: slaveLst) {
            String slaveMetrics = null;
            String httpReq = "http://" + slave.getIpNPort() + "/metrics/snapshot";
            try {
                slaveMetrics = HTTP.executeRequest(httpReq);
                LOGGER.log(Level.FINE, "[Collector Plugin] Successfully executed HTTP request " + httpReq);
                LOGGER.log(Level.FINE, "[Collector Plugin] Slave Metrics:");
                LOGGER.log(Level.FINE, slaveMetrics);
            }
            catch (IOException e) {
                String errorMsg = "[Collector Plugin] Failed to execute HTTP request " + httpReq
                        + " .Reason: " + e.getMessage();
                LOGGER.log(Level.SEVERE, errorMsg);
                LOGGER.log(Level.FINE, "[Collector Plugin] Retrying to execute HTTP request " + httpReq);
                try {
                    slaveMetrics = HTTP.executeRequest(httpReq);
                    LOGGER.log(Level.FINE, "[Collector Plugin] Successfully executed HTTP request " + httpReq );
                    LOGGER.log(Level.FINE, "[Collector Plugin] Slave Metric:");
                    LOGGER.log(Level.FINE, slaveMetrics);
                } catch (IOException e1) {
                    errorMsg = "[Collector Plugin] Retry attempt 1 Failed to execute HTTP request " + httpReq
                            + " .Reason: " + e.getMessage();
                    LOGGER.log(Level.SEVERE, errorMsg);
                    //not throwing ann exception as slave must be temporary down
                    //throw new IllegalStateException(errorMsg, e1);
                }
            }
            if (slaveMetrics != null) {
                parseSlaveMetrics(slave, slaveMetrics);
            }
            else {
                slave.setReachable(false);
                LOGGER.log(Level.SEVERE, "Unreachable slave " + slave.getHostName() +
                        ". Hence no rows will be inserted for this slave");
            }
        }
    }

    private void parseSlaveMetrics(SlaveDetails slave, String slaveMetricsResp) {
        JsonElement slaveMetrics = new JsonParser().parse(slaveMetricsResp);
        JsonObject smObj = slaveMetrics.getAsJsonObject();
        slave.setAllocatedCpu(smObj.get("slave/cpus_used").getAsInt())
                .setCpu(smObj.get("slave/cpus_total").getAsInt())
                .setFreeMemory(smObj.get("system/mem_free_bytes").getAsLong())
                .setTotalMemory(smObj.get("system/mem_total_bytes").getAsLong())
                .setLoad5Min(smObj.get("system/load_5min").getAsFloat());
    }

    private static void parseStateSummary(String stateSummaryResp, List<SlaveDetails> slaveLst,
                                          List<FrameworkDetails> frameworkDetailsLst,
                                          List<FrameworkSlaveRelationship> runsOn) {
        JsonElement stateSummary = new JsonParser().parse(stateSummaryResp);
        JsonArray slaves = stateSummary.getAsJsonObject().getAsJsonArray("slaves");
        JsonArray frameworks = stateSummary.getAsJsonObject().getAsJsonArray("frameworks");

        processSlaveDetails(slaves, slaveLst, runsOn);
        processFrameworkDetails(frameworks, frameworkDetailsLst);
    }

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

    private static int getScheduledTasks(JsonObject fObj) {
        return fObj.get("TASK_STAGING").getAsInt() + fObj.get("TASK_STARTING").getAsInt() + fObj.get("TASK_RUNNING").getAsInt();
    }

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
                runsOn.add(new FrameworkSlaveRelationship().setFrameworkId(fid.getAsString()).setSlaveId(s.getSlaveId()));
            }
        }
    }
}
