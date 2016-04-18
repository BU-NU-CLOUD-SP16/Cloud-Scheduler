import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * <h1>ITableInfo</h1>
 *
 * @author Kovit
 * @version 1.0
 * @since 2016-03-10
 */
public class TableInfo implements ITableInfo{
    private final String tableName;
    private final List<String> colName;
    private final List<String> colValue;
    private int priority;

    /**
     * <h1>TableInfo</h1>
     * Constructor
     * @param tableName
     */
    public TableInfo(String tableName) {
        this.tableName = tableName;
        this.colName = new ArrayList<>(8);
        this.colValue = new ArrayList<>(8);
    }

    /**
     * <h1>addColValue</h1>
     * Adds a String col value.
     * @param name
     * @return ITableInfo
     */
    @Override
    public ITableInfo addColName(String name) {
        colName.add(name);
        return this;
    }

    /**
     * <h1>setPriority</h1>
     * @return ITableInfo
     */
    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * <h1>getPriority</h1>
     * @return int
     */
    @Override
    public int getPriority() {
        return priority;
    }

    /**
     * <h1>addColValue</h1>
     * Adds a String col value. with boolean
     * @param val
     * @param isString
     * @return ITableInfo
     */
    @Override
    public ITableInfo addColValue(String val, boolean isString) {
        if (isString) {
            addColValue(val);
        }
        else {
            colValue.add(val);
        }
        return this;
    }
    /**
     * <h1>addColValue</h1>
     * @param val String
     * @return ITableInfo
     */
    @Override
    public ITableInfo addColValue(String val) {
        colValue.add("'" + val + "'");
        return this;
    }
    /**
     * <h1>addColValue</h1>
     * @param val long
     * @return ITableInfo
     */
    @Override
    public ITableInfo addColValue(long val) {
        colValue.add(""+val);
        return this;
    }
    /**
     * <h1>addColValue</h1>
     * @param val int
     * @return ITableInfo
     */
    @Override
    public ITableInfo addColValue(int val) {
        colValue.add(String.valueOf(val));
        return this;
    }

    /**
     * <h1>addColValue</h1>
     * @param val float
     * @return ITableInfo
     */
    @Override
    public ITableInfo addColValue(float val) {
        colValue.add(String.valueOf(val));
        return this;
    }

    /**
     * <h1>addColValue</h1>
     * @param val double
     * @return ITableInfo
     */
    @Override
    public ITableInfo addColValue(double val) {
        colValue.add(String.valueOf(val));
        return this;
    }

    /**
     * <h1>addColValue</h1>
     * @param val short
     * @return ITableInfo
     */
    @Override
    public ITableInfo addColValue(short val) {
        colValue.add(String.valueOf(val));
        return this;
    }

    /**
     * <h1>colNameToString</h1>
     * @return String
     */
    @Override
    public String colNameToString() {
        return lstToString(colName);
    }

    /**
     * <h1>colValueToString</h1>
     * @return String
     */
    @Override
    public String colValueToString() {
        return lstToString(colValue);
    }

    /**
     * <h1>lstToString</h1>
     * Converts the lits to String.
     * @param lst
     * @return String
     */
    private String lstToString(List<String> lst) {
        StringJoiner joiner = new StringJoiner(",");
        for(String s: lst) {
            joiner.add(s);
        }
        return joiner.toString();
    }

    /**
     * <h1>isTableValid</h1>
     * @return Boolean
     */
    @Override
    public boolean isTableValid() {
        boolean valid = false;
        if (colName.size() > 0) {
            valid = true;
        }
        return valid;
    }

    /**
     * <h1>getTableName</h1>
     * @return String
     */
    @Override
    public String getTableName() {
        return tableName;
    }

    /**
     * <h1>toString</h1>
     * @return String
     */
    @Override
    public String toString() {
        return "TableInfo{" +
                "tableName='" + tableName + '\'' +
                ", colName=" + colName +
                ", colValue=" + colValue +
                ", priority=" + priority +
                '}';
    }
}
