import java.sql.*;

public class SQLiteJDBC
{
    public static void main( String args[] )
    {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:cloudScheduler.db");

            System.out.println("Opened database successfully");
            stmt = c.createStatement();
            /*String sql = "CREATE TABLE COMPANY " +
                    "(ID INT PRIMARY KEY     NOT NULL," +
                    " NAME           TEXT    NOT NULL, " +
                    " AGE            INT     NOT NULL, " +
                    " ADDRESS        CHAR(50), " +
                    " SALARY         REAL)";*/
            String sql = "CREATE TABLE Framework" +
                    "(Framework_ID TEXT NOT NULL," +
                    "Name TEXT," +
                    "CPU INTEGER," +
                    "Memory REAL," +
                    "Active INTEGER DEFAULT FALSE NOT NULL," +
                    "Scheduled_Tasks INTEGER DEFAULT FALSE);";
            stmt.executeUpdate(sql);

            sql = "CREATE UNIQUE INDEX Framework_Framework_ID_uindex ON Framework (Framework_ID);";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE Slave" +
                    "(Slave_ID TEXT NOT NULL," +
                    "Load_5min REAL" +
                    "Free_Memory REAL," +
                    "Total_Memory REAL," +
                    "CPU INTEGER," +
                    "Allocated_CPU REAL," +
                    "IP TEXT NOT NULL," +
                    "Hostname TEXT);";
            stmt.executeUpdate(sql);

            sql = "CREATE UNIQUE INDEX Slave_Slave_ID_uindex ON Slave (Slave_ID);";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE Runs_On" +
                    "(Framework_ID TEXT," +
                    "Slave_ID TEXT," +
                    "FOREIGN KEY (Slave_ID) REFERENCES Slave (Slave_ID) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED," +
                    "FOREIGN KEY (Framework_ID) REFERENCES Framework (Framework_ID) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED);";
            stmt.executeUpdate(sql);

            stmt.close();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + 			e.getMessage() );
            System.exit(0);
        }
        System.out.println("Table Created dude!!!");
    }
}
/* executeQuery(String);
    executeUpdate();
    executeScript();
    class-nameSQLiteDBExecutor
    interface-DBExecutor
 */