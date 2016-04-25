import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.*;

/**
 * <h1>Collector</h1>
 * Collects the mesos slave metrics
 * and inserts into the SqliteDatabase.
 *
 * @author Kovit
 * @version 1.0
 * @since 2016-03-08
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
    private Logger logger = GlobalLogger.globalLogger;
    private final String masterIpAddress;

    private String databasePluginClassName;
    private String collectorPluginClassName;
    private List<Object> collectorPluginClasses;

    private DBExecutor database;

    private String id;

    /**
     * <h1>Collector</h1>
     * Constructor.
     * @param argumentList
     */
    public Collector(CommandLineArguments argumentList) {
        masterIpAddress = argumentList.getConfig().getValueForKey("Mesos-Master-Ip") + ":" + argumentList.getConfig().getValueForKey("Mesos-Master-Port");
        databasePluginClassName = argumentList.getDbExecutorPluginMainClass();
        collectorPluginClassName = argumentList.getCollectorPluginMainClass();
        id = argumentList.getConfig().getValueForKey("Id");
        createInstances();
    }

    /**
     * <h1>createInstances</h1>
     */
    public void createInstances() {
        try
        {
            Class databaseExecutorPluginClass = Class.forName(databasePluginClassName);
            database = (DBExecutor) databaseExecutorPluginClass.getConstructor(String.class).newInstance(id);
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

    /**
     * <h1>getCPluginClsIntances</h1>
     * @param collectorPluginMainClass
     * @return List<Object>
     */
    private List<Object> getCPluginClsIntances(String collectorPluginMainClass) {
        String[] classNamesLst = collectorPluginMainClass.split(",");
        List<Object> classInstance = new ArrayList<>(classNamesLst.length);
        try {
            for (String className : classNamesLst) {
                classInstance.add(Class.forName(className).getConstructor().newInstance());
                logger.log(Level.FINE, "Instance of class " + className + " created successfully",Constants.COLLECTOR_LOG_ID);
            }
        }
        catch(Exception ex) {
            String errorMsg = "[Collector Framework] Failed to create instances of collector plugin classes " + ex.getMessage();
            logger.log(Level.SEVERE, errorMsg,Constants.COLLECTOR_LOG_ID);
            throw new IllegalStateException(errorMsg, ex);
        }
        return classInstance;
    }

    /**
     * <h1>notifyTimerExpiry</h1>
     * @throws ClusterElasticityAgentException
     */
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
            logger.log(Level.FINE, "[Collector Framework] Executing query " + query,Constants.COLLECTOR_LOG_ID);
            database.executeUpdate(query);
        }
    }

    /**
     * <h1>processCollectorPluginClass</h1>
     * @param cpClassInstance
     * @param tableLst
     */
    private void processCollectorPluginClass(Object cpClassInstance, List<ITableInfo> tableLst) {
        Class cls = cpClassInstance.getClass();
        logger.log(Level.FINE, "[Collector Plugin] Processing class " + cls,Constants.COLLECTOR_LOG_ID);
        if (ICollectorPluginByTable.class.isAssignableFrom(cls)) {
            Collection<? extends ITableInfo> tableEntries = callFetchByTable((ICollectorPluginByTable) cpClassInstance);
            if (tableEntries != null) {
                tableLst.addAll(tableEntries);
            }
        } else if (ICollectorPluginByRow.class.isAssignableFrom(cls)) {
            collectorPluginByRow((ICollectorPluginByRow) cpClassInstance, tableLst);
        } else {
            logger.log(Level.WARNING, "[Collector Plugin] " + cls.getName() + " should only implement " +
                    "ICollectorPluginByRow or ICollectorPluginByTable. Ignoring this class",Constants.COLLECTOR_LOG_ID);
        }
    }

    /**
     * <h1>callFetchByTable</h1>
     * @param cpClassInstance
     * @return Collection<? extends ITableInfo>
     */
    private Collection<? extends ITableInfo> callFetchByTable(ICollectorPluginByTable cpClassInstance) {
        Method fetchMthd = null;
        Class cls = cpClassInstance.getClass();
        try {
            fetchMthd = cls.getMethod("fetch", List.class, String.class);
        } catch (NoSuchMethodException e) {
            String errorMsg = "[CollectorFramework] Failed to get method [fetch] on class " +
                    cls.getName() + ". Reason:" + e.getMessage();
            logger.log(Level.SEVERE, errorMsg,Constants.COLLECTOR_LOG_ID);
            throw new IllegalStateException(errorMsg, e);
        }
        List<Data> results = processQueryAnnotation(fetchMthd);
        try {
            return (List<ITableInfo>) fetchMthd.invoke(cpClassInstance, results, masterIpAddress);
        } catch (Exception e) {
            String errorMsg = "[CollectorFramework] Failed to invoke method [fetch] on class " +
                    cls.getName() + ". Reason:" + e.getMessage();
            logger.log(Level.SEVERE, errorMsg,Constants.COLLECTOR_LOG_ID);
            throw new IllegalStateException(errorMsg, e);
        }
    }

    /**
     * <h1>collectorPluginByRow</h1>
     * @param cpClassInstance
     * @param tableLst
     */
    private void collectorPluginByRow(ICollectorPluginByRow cpClassInstance, List<ITableInfo> tableLst) {
        Class cls = cpClassInstance.getClass();
        if (cls.isAnnotationPresent(Table.class)) {
            logger.log(Level.FINE, "[Collector Framework] Table annotation found",Constants.COLLECTOR_LOG_ID);
            ITableInfo tInfo;
            String tableName = ((Table) cls.getAnnotation(Table.class)).name();
            int numOfIter = callFetch(cls);
            logger.log(Level.FINE, "[Collector Framework] Number of Iterations " + numOfIter,Constants.COLLECTOR_LOG_ID);
            if (numOfIter > 0) {
                for (int iter = 0; iter <numOfIter; iter++) {
                    tInfo = new TableInfo(tableName);
                    processClassMethods(tInfo, cpClassInstance);
                    if (tInfo.isTableValid()) {
                        logger.log(Level.FINE, "[Collector Framework] Valid table " + tableName,Constants.COLLECTOR_LOG_ID);
                        tInfo.setPriority(((Table) cls.getAnnotation(Table.class)).priority());
                        tableLst.add(tInfo);
                    }
                    else {
                        logger.log(Level.WARNING, "[Collector Framework] Invalid table " + tableName
                                + " No further processing of this table",Constants.COLLECTOR_LOG_ID);
                    }
                }
            }
            else {
                logger.log(Level.WARNING, "[Collector Framework] Number of iterations is 0, hence ignoring table",Constants.COLLECTOR_LOG_ID);
            }
        }
        else {
            logger.log(Level.WARNING, "[Collector Framework] No @Table annotation was found in class " + cls.getName(),Constants.COLLECTOR_LOG_ID);
        }
    }

    /**
     * <h1>processClassMethods</h1>
     * @param tInfo
     * @param cpClassInstance
     */
    private void processClassMethods(ITableInfo tInfo, ICollectorPluginByRow cpClassInstance) {
        Class cls = cpClassInstance.getClass();
        for (Method m : cls.getMethods()) {
            logger.log(Level.FINE, "[Collector Framework] Processing method " + m.getName(),Constants.COLLECTOR_LOG_ID);
            if (m.isAnnotationPresent(Column.class)) {
                logger.log(Level.FINE, "[Collector Framework] Column annotation found",Constants.COLLECTOR_LOG_ID);
                String name = ((Column) m.getAnnotation(Column.class)).name();
                Type returnType = m.getReturnType();
                if (returnType.equals(Integer.TYPE) || returnType.equals(Float.TYPE) ||
                        returnType.equals(Double.TYPE) || returnType.equals(Short.TYPE)) {
                    logger.log(Level.FINE, "[Collector Framework] Numeric return type",Constants.COLLECTOR_LOG_ID);
                    tInfo.addColName(name);
                    invokeMethod(false, tInfo, cpClassInstance, m);
                }
                else if (returnType.equals(String.class)) {
                    logger.log(Level.FINE, "[Collector Framework] String return type",Constants.COLLECTOR_LOG_ID);
                    tInfo.addColName(name);
                    invokeMethod(true, tInfo, cpClassInstance, m);
                }
                else {
                    logger.log(Level.WARNING, "[Collector Framework] Invalid return type. Ignoring method. Return type should be " +
                            "String or numeric primitive",Constants.COLLECTOR_LOG_ID);
                }
            }
            else {
                logger.log(Level.WARNING, "[Collector Framework] Ignoring method as @Column annotation not found",Constants.COLLECTOR_LOG_ID);
            }
        }
    }

    /**
     * <h1>invokeMethod</h1>
     * @param isString
     * @param tInfo
     * @param cpClassInstance
     * @param mthd
     */
    private void invokeMethod(boolean isString, ITableInfo tInfo, ICollectorPluginByRow cpClassInstance, Method mthd) {
        try {
            logger.log(Level.FINE, "[CollectorFramework] Invoking method " + mthd.getName(),Constants.COLLECTOR_LOG_ID);
            tInfo.addColValue(String.valueOf(mthd.invoke(cpClassInstance)), isString);
        }
        catch (Exception e) {
            String errorMsg = "[CollectorFramework] Failed to invoke method " + mthd.getName() + " on class " +
                    cpClassInstance.getClass().getName() + ". Reason:" + e.getMessage();
            logger.log(Level.SEVERE, errorMsg,Constants.COLLECTOR_LOG_ID);
            throw new IllegalStateException(errorMsg, e);
        }
    }

    /**
     * <h1>callFetch</h1>
     * @param cls
     * @return int
     * number of iterations fetch has been called.
     */
    private int callFetch(Class cls) {
        Method fetchMthd = null;
        try {
            fetchMthd = cls.getMethod("fetch", List.class, String.class);
            logger.log(Level.FINE, "[CollectorFramework] retrieved method " + fetchMthd.getName(),Constants.COLLECTOR_LOG_ID);
        } catch (NoSuchMethodException e) {
            String errorMsg = "[CollectorFramework] Failed to get method [fetch] on class " +
                    cls.getName() + ". Reason:" + e.getMessage();
            logger.log(Level.SEVERE, errorMsg,Constants.COLLECTOR_LOG_ID);
            throw new IllegalStateException(errorMsg, e);
        }
        List<Data> results = processQueryAnnotation(fetchMthd);
        int numOfIterations = 0;
        try {
            logger.log(Level.FINE, "[CollectorFramework] Invoking method " + fetchMthd.getName(),Constants.COLLECTOR_LOG_ID);
            numOfIterations = (int) fetchMthd.invoke(cls, results, masterIpAddress);
        } catch (Exception e) {
            String errorMsg = "[CollectorFramework] Failed to invoke method [fetch] on class " +
                    cls.getName() + ". Reason:" + e.getMessage();
            logger.log(Level.SEVERE, errorMsg,Constants.COLLECTOR_LOG_ID);
            throw new IllegalStateException(errorMsg, e);
        }
        return numOfIterations;
    }

    /**
     * <h1>query</h1>
     * Insert query is returned as a message.
     * @param info
     * @return String
     */
    public String query(ITableInfo info) {
        return MessageFormat.format(INSERT_SQL_STMT, info.getTableName(), info.colNameToString(), info.colValueToString());
    }

    /**
     * <h1>processQueryAnnotation</h1>
     * Using the annotation, creates a query and runs it.
     * @param mthd
     * @return List<Data>
     */
    private List<Data> processQueryAnnotation(Method mthd) {
        if (mthd.isAnnotationPresent(DataQuery.class)) {
            logger.log(Level.FINE, "@DataQuery annotation found",Constants.COLLECTOR_LOG_ID);
            DataQuery dataQuery = mthd.getAnnotation(DataQuery.class);
            String[] queries = dataQuery.queries();
            List<Data> result = executeQueries(queries);
            return result;
        }
        logger.log(Level.FINE, "@DataQuery annotation not found",Constants.COLLECTOR_LOG_ID);
        return null;
    }

    /**
     * <h1>executeQueries</h1>
     * @param queries
     * @return List<Data>
     * returns the data received after running
     * the queries.
     */
    private List<Data> executeQueries(String[] queries) {
        List<Data> resultLst = new ArrayList<>();
        for (String query : queries) {
            if (!query.isEmpty()) {
                List<String[]> data = database.executeSelect(query);
                logger.log(Level.FINE, "Query [" + query + "] executed successfully. Returned " + data.size() + " rows",Constants.COLLECTOR_LOG_ID);
                Data dataObject = new Data();
                dataObject.setData(data);
                dataObject.setQuery(query);
                resultLst.add(dataObject);
            }
        }
        return resultLst;
    }

}