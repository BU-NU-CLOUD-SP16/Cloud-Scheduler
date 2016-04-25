/**
 * <h1>ITableInfo</h1>
 *
 * @author Kovit
 * @version 1.0
 * @since 2016-03-10
 */
public interface ITableInfo {
    String getTableName();
    String colNameToString();
    String colValueToString();

    /**
     * <h1>isTableValid</h1>
     * @return Boolean
     * True if table is valid
     */
    boolean isTableValid();

    /**
     * <h1>addColValue</h1>
     * Adds a String column value
     * @param val
     * @return ITableInfo
     */
    ITableInfo addColValue(String val);

    /**
     * <h1>addColValue</h1>
     * Adds a Int col value
     * @param val
     * @return ITableInfo
     */
    ITableInfo addColValue(int val);

    /**
     * <h1>addColValue</h1>
     * Adds a Float col value.
     * @param val
     * @return ITableInfo
     */
    ITableInfo addColValue(float val);
    /**
     * <h1>addColValue</h1>
     * Adds a double col value.
     * @param val
     * @return ITableInfo
     */
    ITableInfo addColValue(double val);
    /**
     * <h1>addColValue</h1>
     * Adds a short col value.
     * @param val
     * @return ITableInfo
     */
    ITableInfo addColValue(short val);
    /**
     * <h1>addColValue</h1>
     * Adds a Long col value.
     * @param val
     * @return ITableInfo
     */
    ITableInfo addColValue(long val);
    /**
     * <h1>addColValue</h1>
     * Adds a String col value.
     * @param name
     * @return ITableInfo
     */
    ITableInfo addColName(String name);
    /**
     * <h1>addColValue</h1>
     * Adds a String col value. with boolean
     * @param val
     * @param isString
     * @return ITableInfo
     */
    ITableInfo addColValue(String val, boolean isString);

    /**
     * <h1>setPriority</h1>
     * @param priority int
     */
    void setPriority(int priority);

    /**
     * <h1>getPriority</h1>
     * @return
     */
    int getPriority();
}
