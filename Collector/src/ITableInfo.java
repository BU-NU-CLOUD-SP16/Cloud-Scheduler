/**
 * Created by kovit on 3/10/2016.
 */
public interface ITableInfo {
    String getTableName();
    String colNameToString();
    String colValueToString();
    boolean isTableValid();
    ITableInfo addColValue(String val);
    ITableInfo addColValue(int val);
    ITableInfo addColValue(float val);
    ITableInfo addColValue(double val);
    ITableInfo addColValue(short val);
    ITableInfo addColName(String name);
    void setPriority(int priority);
    int getPriority();
}
