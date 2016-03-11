import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by kovit on 3/9/2016.
 */
public final class CollectorFramewrkCmd implements ClusterElasticityAgentCommand {

    private DBExecutor dbExecutor;
    private List<Object> collectorPluginCls;
    private String masterAddr;
    private static final String INSERT_SQL_STMT = "INSERT INTO {0} ({1}) VALUES ({2})";

    public CollectorFramewrkCmd(DBExecutor dbExec, List<Object> cPluginClass, String masterAddr) {
        this.dbExecutor = dbExec;
        this.collectorPluginCls = cPluginClass;
        this.masterAddr = masterAddr;
    }

    private List<Data> processQueryAnnotation(Class cls, Method mthd) {
        DataQuery dataQuery = mthd.getAnnotation(DataQuery.class);
        String[] queries  = dataQuery.queries();
        List<Data> result = executeQueries(queries);
        return result;
    }

    private List<Data> executeQueries(String[] queries) {
        List<Data> resultLst = new ArrayList<>();
        for (String query : queries) {
            if (!query.isEmpty()) {
                List<String[]> data = dbExecutor.executeSelect(query);
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
        for (Object cpClass : collectorPluginCls) {
            processCollectorPluginClass(cpClass, tableLst);
        }

        Collections.sort(tableLst, new Comparator<ITableInfo>() {
            @Override
            public int compare(ITableInfo o1, ITableInfo o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });

        dbExecutor.clearDB();
        for (ITableInfo tInfo : tableLst) {
            dbExecutor.executeUpdate(query(tInfo));
        }
    }

    private void processCollectorPluginClass(Object cpClass, List<ITableInfo> tableLst) {
        Class cls = cpClass.getClass();
        if (ICollectorPluginByTable.class.isAssignableFrom(cls)) {
            tableLst.addAll(callFetchByTable(cls));
        } else if (ICollectorPluginByRow.class.isAssignableFrom(cls)) {
            collectorPluginByRow(cls, tableLst);
        } else {
            // warning
        }
    }

    private Collection<? extends ITableInfo> callFetchByTable(Class cls) {
        Method fetchMthd = null;
        try {
            fetchMthd = cls.getMethod("fetch", List.class, String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        List<Data> results = processQueryAnnotation(cls, fetchMthd);
        try {
            return (List<ITableInfo>) fetchMthd.invoke(cls, results, masterAddr);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        // TODO remove this handle exceptions
        return null;
    }

    private void collectorPluginByRow(Class cls, List<ITableInfo> tableLst) {
        if (cls.isAnnotationPresent(Table.class)) {
            ITableInfo tInfo;
            String tableName = ((Table) cls.getAnnotation(Table.class)).name();
            int numOfIter = callFetch(cls);
            if (numOfIter > 0) {
                for (int iter = 0; iter <numOfIter; iter++) {
                    tInfo = new TableInfo(tableName);
                    processClassMethods(tInfo, cls);
                    if (tInfo.isTableValid()) {
                        tInfo.setPriority(((Table) cls.getAnnotation(Table.class)).priority());
                        tableLst.add(tInfo);
                    }
                    else {
                        // log warning
                    }
                }
            }
            else {
                // log warning
            }
        }
        else {
            // log warning
        }
    }

    private void processClassMethods(ITableInfo tInfo, Class cls) {
        for (Method m : cls.getMethods()) {
            if (m.isAnnotationPresent(Column.class)) {
                String name = ((Column) m.getAnnotation(Column.class)).name();
                Type returType = m.getReturnType();
                if (returType.equals(Integer.TYPE) || returType.equals(Float.TYPE) ||
                        returType.equals(Double.TYPE) || returType.equals(Short.TYPE)) {
                    tInfo.addColName(name);
                    invokeMethod(false, tInfo, cls, m);
                }
                else if (returType.equals(String.class)) {
                    tInfo.addColName(name);
                    invokeMethod(true, tInfo, cls, m);
                }
                else {
                    // log warning unsupportable return type
                }
            }
            else {
                // log log level message
            }
        }
    }

    private void invokeMethod(boolean isString, ITableInfo tInfo, Class cls, Method mthd) {
        try {
            if (isString) {
                tInfo.addColValue("'" + mthd.invoke(cls) + "'");
            }
            else {
                tInfo.addColValue(String.valueOf(mthd.invoke(cls)));
            }
        }
        catch (Exception e) {
            // throw exception
        }
    }

    private int callFetch(Class cls) {
        Method fetchMthd = null;
        try {
            fetchMthd = cls.getMethod("fetch", List.class, String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        List<Data> results = processQueryAnnotation(cls, fetchMthd);
        int numOfIterations = 0;
        try {
            numOfIterations = (int) fetchMthd.invoke(cls, results, masterAddr);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return numOfIterations;
    }

    public String query(ITableInfo info) {
        return MessageFormat.format(INSERT_SQL_STMT, info.getTableName(), info.colNameToString(), info.colValueToString());
    }
}