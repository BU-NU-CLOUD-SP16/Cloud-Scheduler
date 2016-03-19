import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.*;

/**
 * Created by Kovit on 3/8/2016.
 */


// TODO: self code review
// TODO : testing
// -----------------------------------------------------------------
// TODO: java doc
// TODO: package structure
// TODO: constants
// TODO: checking if method @DataQuery tag
// TODO: support bulk insert
// TODO: implementing cache

public final class Collector implements ClusterElasticityAgentFramework {

    private static final String INSERT_SQL_STMT = "INSERT INTO {0} ({1}) VALUES ({2})";
    public final static String COLLECTOR_LOGGER_NAME = "CollectorFramework";
    private final static Logger LOGGER = Logger.getLogger(COLLECTOR_LOGGER_NAME);
    private final String masterIpAddress;

    private int pollInterval;
    private String databasePluginClassName;
    private String collectorPluginClassName;
    private List<Object> collectorPluginClasses;

    private DBExecutor database;


    private String logDir;

    public Collector(CommandLineArguments argumentList) {
        masterIpAddress = argumentList.getMesosMasterIP() + ":" + argumentList.getMesosMasterPort();
        databasePluginClassName = argumentList.getDbExecutorPluginMainClass();
        collectorPluginClassName = argumentList.getCollectorPluginMainClass();
        pollInterval = argumentList.getPollInterval();
        logDir = argumentList.getLogDir();
        logSetup();
        createInstances();
    }

    public void createInstances() {
        try
        {
            Class databaseExecutorPluginClass = Class.forName(databasePluginClassName);
            database = (DBExecutor) databaseExecutorPluginClass.getConstructor().newInstance();
            collectorPluginClasses = getCPluginClsIntances(collectorPluginClassName);
        }

        catch (ClassNotFoundException ex)
        {

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }



    private List<Object> getCPluginClsIntances(String collectorPluginMainClass) {
        String[] classNamesLst = collectorPluginMainClass.split(",");
        List<Object> classInstance = new ArrayList<>(classNamesLst.length);
        try {
            for (String className : classNamesLst) {
                classInstance.add(Class.forName(className).getConstructor().newInstance());
                LOGGER.log(Level.FINE, "Instance of class " + className + " created successfully");
            }
        }
        catch(Exception ex) {
            String errorMsg = "[Collector Framework] Failed to create instances of collector plugin classes " + ex.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new IllegalStateException(errorMsg, ex);
        }
        return classInstance;
    }

    @Override
    public void notifyTimerExpiry() throws ClusterElasticityAgentException {
        List<ITableInfo> tableLst = new ArrayList<>();
        for (Object cpClassInstance : collectorPluginClasses) {
            processCollectorPluginClass(cpClassInstance, tableLst);
        }

        Collections.sort(tableLst, new Comparator<ITableInfo>() {
            @Override
            public int compare(ITableInfo o1, ITableInfo o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });

        database.clearDB();
        for (ITableInfo tInfo : tableLst) {
            String query = query(tInfo);
            LOGGER.log(Level.FINE, "[Collector Framework] Executing query " + query);
            database.executeUpdate(query);
        }
    }

    private void logSetup() {
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(COLLECTOR_LOGGER_NAME);
        logger.setLevel(Level.FINE);
        FileHandler logFileHandler = null;
        try {
            logFileHandler = new FileHandler(logDir + File.separator + "Collector_Plugin.log");
            SimpleFormatter formatterTxt = new SimpleFormatter();
            logFileHandler.setFormatter(formatterTxt);
            logger.addHandler(logFileHandler);
        }
        catch (Exception e) {
            System.err.print("[Collector Framework] Could not create log file at " + logDir);
        }
    }

    private void processCollectorPluginClass(Object cpClassInstance, List<ITableInfo> tableLst) {
        Class cls = cpClassInstance.getClass();
        LOGGER.log(Level.FINE, "[Collector Plugin] Processing class " + cls);
        if (ICollectorPluginByTable.class.isAssignableFrom(cls)) {
            Collection<? extends ITableInfo> tableEntries = callFetchByTable((ICollectorPluginByTable) cpClassInstance);
            if (tableEntries != null) {
                tableLst.addAll(tableEntries);
            }
        } else if (ICollectorPluginByRow.class.isAssignableFrom(cls)) {
            collectorPluginByRow((ICollectorPluginByRow) cpClassInstance, tableLst);
        } else {
            LOGGER.log(Level.WARNING, "[Collector Plugin] " + cls.getName() + " should only implement " +
                    "ICollectorPluginByRow or ICollectorPluginByTable. Ignoring this class");
        }
    }

    private Collection<? extends ITableInfo> callFetchByTable(ICollectorPluginByTable cpClassInstance) {
        Method fetchMthd = null;
        Class cls = cpClassInstance.getClass();
        try {
            fetchMthd = cls.getMethod("fetch", List.class, String.class);
        } catch (NoSuchMethodException e) {
            String errorMsg = "[CollectorFramework] Failed to get method [fetch] on class " +
                    cls.getName() + ". Reason:" + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new IllegalStateException(errorMsg, e);
        }
        List<Data> results = processQueryAnnotation(fetchMthd);
        try {
            return (List<ITableInfo>) fetchMthd.invoke(cpClassInstance, results, masterIpAddress);
        } catch (Exception e) {
            String errorMsg = "[CollectorFramework] Failed to invoke method [fetch] on class " +
                    cls.getName() + ". Reason:" + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new IllegalStateException(errorMsg, e);
        }
    }

    private void collectorPluginByRow(ICollectorPluginByRow cpClassInstance, List<ITableInfo> tableLst) {
        Class cls = cpClassInstance.getClass();
        if (cls.isAnnotationPresent(Table.class)) {
            LOGGER.log(Level.FINE, "[Collector Framework] Table annotation found");
            ITableInfo tInfo;
            String tableName = ((Table) cls.getAnnotation(Table.class)).name();
            int numOfIter = callFetch(cls);
            LOGGER.log(Level.FINE, "[Collector Framework] Number of Iterations " + numOfIter);
            if (numOfIter > 0) {
                for (int iter = 0; iter <numOfIter; iter++) {
                    tInfo = new TableInfo(tableName);
                    processClassMethods(tInfo, cpClassInstance);
                    if (tInfo.isTableValid()) {
                        LOGGER.log(Level.FINE, "[Collector Framework] Valid table " + tableName);
                        tInfo.setPriority(((Table) cls.getAnnotation(Table.class)).priority());
                        tableLst.add(tInfo);
                    }
                    else {
                        LOGGER.log(Level.WARNING, "[Collector Framework] Invalid table " + tableName
                                + " No further processing of this table");
                    }
                }
            }
            else {
                LOGGER.log(Level.WARNING, "[Collector Framework] Number of iterations is 0, hence ignoring table");
            }
        }
        else {
            LOGGER.log(Level.WARNING, "[Collector Framework] No @Table annotation was found in class " + cls.getName());
        }
    }

    private void processClassMethods(ITableInfo tInfo, ICollectorPluginByRow cpClassInstance) {
        Class cls = cpClassInstance.getClass();
        for (Method m : cls.getMethods()) {
            LOGGER.log(Level.FINE, "[Collector Framework] Processing method " + m.getName());
            if (m.isAnnotationPresent(Column.class)) {
                LOGGER.log(Level.FINE, "[Collector Framework] Column annotation found");
                String name = ((Column) m.getAnnotation(Column.class)).name();
                Type returnType = m.getReturnType();
                if (returnType.equals(Integer.TYPE) || returnType.equals(Float.TYPE) ||
                        returnType.equals(Double.TYPE) || returnType.equals(Short.TYPE)) {
                    LOGGER.log(Level.FINE, "[Collector Framework] Numeric return type");
                    tInfo.addColName(name);
                    invokeMethod(false, tInfo, cpClassInstance, m);
                }
                else if (returnType.equals(String.class)) {
                    LOGGER.log(Level.FINE, "[Collector Framework] String return type");
                    tInfo.addColName(name);
                    invokeMethod(true, tInfo, cpClassInstance, m);
                }
                else {
                    LOGGER.log(Level.WARNING, "[Collector Framework] Invalid return type. Ignoring method. Return type should be " +
                            "String or numeric primitive");
                }
            }
            else {
                LOGGER.log(Level.WARNING, "[Collector Framework] Ignoring method as @Column annotation not found");
            }
        }
    }

    private void invokeMethod(boolean isString, ITableInfo tInfo, ICollectorPluginByRow cpClassInstance, Method mthd) {
        try {
            LOGGER.log(Level.FINE, "[CollectorFramework] Invoking method " + mthd.getName());
            tInfo.addColValue(String.valueOf(mthd.invoke(cpClassInstance)), isString);
        }
        catch (Exception e) {
            String errorMsg = "[CollectorFramework] Failed to invoke method " + mthd.getName() + " on class " +
                    cpClassInstance.getClass().getName() + ". Reason:" + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new IllegalStateException(errorMsg, e);
        }
    }

    private int callFetch(Class cls) {
        Method fetchMthd = null;
        try {
            fetchMthd = cls.getMethod("fetch", List.class, String.class);
            LOGGER.log(Level.FINE, "[CollectorFramework] retrieved method " + fetchMthd.getName());
        } catch (NoSuchMethodException e) {
            String errorMsg = "[CollectorFramework] Failed to get method [fetch] on class " +
                    cls.getName() + ". Reason:" + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new IllegalStateException(errorMsg, e);
        }
        List<Data> results = processQueryAnnotation(fetchMthd);
        int numOfIterations = 0;
        try {
            LOGGER.log(Level.FINE, "[CollectorFramework] Invoking method " + fetchMthd.getName());
            numOfIterations = (int) fetchMthd.invoke(cls, results, masterIpAddress);
        } catch (Exception e) {
            String errorMsg = "[CollectorFramework] Failed to invoke method [fetch] on class " +
                    cls.getName() + ". Reason:" + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new IllegalStateException(errorMsg, e);
        }
        return numOfIterations;
    }

    public String query(ITableInfo info) {
        return MessageFormat.format(INSERT_SQL_STMT, info.getTableName(), info.colNameToString(), info.colValueToString());
    }

    private List<Data> processQueryAnnotation(Method mthd) {
        if (mthd.isAnnotationPresent(DataQuery.class)) {
            LOGGER.log(Level.FINE, "@DataQuery annotation found");
            DataQuery dataQuery = mthd.getAnnotation(DataQuery.class);
            String[] queries = dataQuery.queries();
            List<Data> result = executeQueries(queries);
            return result;
        }
        LOGGER.log(Level.FINE, "@DataQuery annotation not found");
        return null;
    }

    private List<Data> executeQueries(String[] queries) {
        List<Data> resultLst = new ArrayList<>();
        for (String query : queries) {
            if (!query.isEmpty()) {
                List<String[]> data = database.executeSelect(query);
                LOGGER.log(Level.FINE, "Query [" + query + "] executed successfully. Returned " + data.size() + " rows");
                Data dataObject = new Data();
                dataObject.setData(data);
                dataObject.setQuery(query);
                resultLst.add(dataObject);
            }
        }
        return resultLst;
    }
}