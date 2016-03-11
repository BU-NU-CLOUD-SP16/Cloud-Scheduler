import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by kovit on 3/9/2016.
 */
public final class CollectorFramewrkCmd implements ClusterElasticityAgentCommand {
    private final static Logger LOGGER = Logger.getLogger(CollectorPluginFrameworkImpl.COLLECTOR_LOGGER_NAME);
    private DBExecutor dbExecutor;
    private List<Object> collectorPluginCls;
    private String masterAddr;
    private static final String INSERT_SQL_STMT = "INSERT INTO {0} ({1}) VALUES ({2})";

    public CollectorFramewrkCmd(DBExecutor dbExec, List<Object> cPluginClass, String masterAddr) {
        this.dbExecutor = dbExec;
        this.collectorPluginCls = cPluginClass;
        this.masterAddr = masterAddr;
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
                List<String[]> data = dbExecutor.executeSelect(query);
                LOGGER.log(Level.FINE, "Query [" + query + "] executed successfully. Returned " + data.size() + " rows");
                Data dataObject = new Data();
                dataObject.setData(data);
                dataObject.setQuery(query);
                resultLst.add(dataObject);
            }
        }
        return resultLst;
    }

    @Override
    public void execute() {
        List<ITableInfo> tableLst = new ArrayList<>();
        for (Object cpClassInstance : collectorPluginCls) {
            processCollectorPluginClass(cpClassInstance, tableLst);
        }

        Collections.sort(tableLst, new Comparator<ITableInfo>() {
            @Override
            public int compare(ITableInfo o1, ITableInfo o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });

        dbExecutor.clearDB();
        for (ITableInfo tInfo : tableLst) {
            String query = query(tInfo);
            LOGGER.log(Level.FINE, "[Collector Framework] Executing query " + query);
            dbExecutor.executeUpdate(query);
        }
    }

    private void processCollectorPluginClass(Object cpClassInstance, List<ITableInfo> tableLst) {
        Class cls = cpClassInstance.getClass();
        LOGGER.log(Level.FINE, "[Collector Plugin] Processing class " + cls);
        if (ICollectorPluginByTable.class.isAssignableFrom(cls)) {
            tableLst.addAll(callFetchByTable((ICollectorPluginByTable) cpClassInstance));
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
            return (List<ITableInfo>) fetchMthd.invoke(cpClassInstance, results, masterAddr);
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
            numOfIterations = (int) fetchMthd.invoke(cls, results, masterAddr);
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
}