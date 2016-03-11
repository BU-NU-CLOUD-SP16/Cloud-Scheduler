import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Logger;

/**
 * Created by kovit on 3/10/2016.
 */
public class TableInfo implements ITableInfo{
    private final String tableName;
    private final List<String> colName;
    private final List<String> colValue;
    private int priority;

    public TableInfo(String tableName) {
        this.tableName = tableName;
        this.colName = new ArrayList<>(8);
        this.colValue = new ArrayList<>(8);
    }

    @Override
    public ITableInfo addColName(String name) {
        colName.add(name);
        return this;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

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

    @Override
    public ITableInfo addColValue(String val) {
        colValue.add("'" + val + "'");
        return this;
    }

    @Override
    public ITableInfo addColValue(int val) {
        colValue.add(String.valueOf(val));
        return this;
    }

    @Override
    public ITableInfo addColValue(float val) {
        colValue.add(String.valueOf(val));
        return this;
    }

    @Override
    public ITableInfo addColValue(double val) {
        colValue.add(String.valueOf(val));
        return this;
    }

    @Override
    public ITableInfo addColValue(short val) {
        colValue.add(String.valueOf(val));
        return this;
    }

    @Override
    public String colNameToString() {
        return lstToString(colName);
    }


    @Override
    public String colValueToString() {
        return lstToString(colValue);
    }

    private String lstToString(List<String> lst) {
        StringJoiner joiner = new StringJoiner(",");
        for(String s: lst) {
            joiner.add(s);
        }
        return joiner.toString();
    }

    @Override
    public boolean isTableValid() {
        boolean valid = false;
        if (colName.size() > 0) {
            valid = true;
        }
        return valid;
    }

    @Override
    public String getTableName() {
        return tableName;
    }
}
