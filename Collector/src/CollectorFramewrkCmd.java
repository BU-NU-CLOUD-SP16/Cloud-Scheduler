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
    private List<ICollectorPlugin> collectorPluginCls;

    public CollectorFramewrkCmd(DBExecutor dbExec, List<ICollectorPlugin> cPluginClass) {
        this.dbExecutor = dbExec;
        this.collectorPluginCls = cPluginClass;
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
        Map<String, List<TableInfo>> tableMap = new HashMap<>(collectorPluginCls.size());
        for (ICollectorPlugin cpClass : collectorPluginCls) {
            processCollectorPluginClass(cpClass, tableMap);
        }

        for (String tableName : tableMap.keySet()) {
            for (TableInfo tInfo : tableMap.get(tableName)) {
                dbExecutor.executeUpdate(tInfo.query());
            }
        }
    }

    private void processCollectorPluginClass(ICollectorPlugin cpClass, Map<String, List<TableInfo>> tableMap) {
        Class cls = cpClass.getClass();
        if (cls.isAnnotationPresent(Table.class)) {
            TableInfo tInfo;
            String tableName = ((Table) cls.getAnnotation(Table.class)).name();
            tableMap.put(tableName, new ArrayList<>());
            int numOfIter = callFetch(cls);
            if (numOfIter > 0) {
                for (int iter = 0; iter <numOfIter; iter++) {
                    tInfo = new TableInfo(tableName);
                    processClassMethods(tInfo, cls);
                    if (tInfo.isTableValid()) {
                        tableMap.get(tableName).add(tInfo);
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

    private void processClassMethods(TableInfo tInfo, Class cls) {
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

    private void invokeMethod(boolean isString, TableInfo tInfo, Class cls, Method mthd) {
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
            fetchMthd = cls.getMethod("fetch", List.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        List<Data> results = processQueryAnnotation(cls, fetchMthd);
        int numOfIterations = 0;
        try {
            numOfIterations = (int) fetchMthd.invoke(results);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return numOfIterations;
    }
}

class TableInfo {
    private final String tableName;
    private final List<String> colName;
    private final List<String> colValue;
    private static final String INSERT_SQL_STMT = "INSERT INTO {0} ({1}) VALUES ({2})";

    public TableInfo(String tableName) {
        this.tableName = tableName;
        this.colName = new ArrayList<>(8);
        this.colValue = new ArrayList<>(8);
    }

    public List<String> addColName(String name) {
        colName.add(name);
        return colName;
    }

    public List<String> addColValue(String val) {
        colValue.add(val);
        return colValue;
    }

    private String colNameToString() {
        return lstToString(colName);
    }


    private String colValueToString() {
        return lstToString(colValue);
    }

    private String lstToString(List<String> lst) {
        StringJoiner joiner = new StringJoiner(",");
        for(String s: lst) {
            joiner.add(s);
        }
        return joiner.toString();
    }

    public boolean isTableValid() {
        boolean valid = false;
        if (colName.size() > 0) {
            valid = true;
        }
        return valid;
    }

    public String query() {
        return MessageFormat.format(INSERT_SQL_STMT, tableName, colNameToString(), colValueToString());
    }
}
