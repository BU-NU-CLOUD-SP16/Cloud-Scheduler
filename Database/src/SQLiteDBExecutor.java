import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import com.ibatis.common.jdbc.ScriptRunner;

/**
 * Created by Akshaya on 05-03-2016.
 */
public class SQLiteDBExecutor implements IDBExecutor {

    //Executes a Select statement and returns a Double Dimensional String array
    public String[][] executeSelect(String str)throws Exception{
        Connection c = null;
        Statement stmt = null;
        String [][] table_values = new String[100][20];
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:cloudScheduler.db");
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(str);
            ResultSetMetaData rsmd= rs.getMetaData();
            int colCount = rsmd.getColumnCount();
//            String [][] table_values = new String[100][colCount];
            int j = 0;
            while(rs.next()){
                String[] values = new String [colCount];
                for(int i = 1;i <= rsmd.getColumnCount();i++){
                    values[i-1] = rs.getObject(i).toString();
                }
                table_values [j] = values;
                j++;
            }
            rs.close();
            stmt.close();
            c.close();
            System.out.println("table_values:"+table_values);
            System.out.println(table_values[0][1]);
        } catch(Exception e){
            e.printStackTrace();
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return table_values;
    }
    // Executes any DML statement
    public void executeUpdate(String str) throws Exception{
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:cloudScheduler.db");

            System.out.println("Opened database successfully");
            String sql = str;
            //stmt.executeUpdate(sql);
            stmt = c.createStatement();
//            String sql2 = "INSERT INTO Framework values ('1','Framework1',20,56.0,1,10)";
            stmt.executeUpdate(sql);
            stmt.close();
            //c.commit();
            c.close();
        } catch ( Exception e ) {
            System.out.println("here??????");
            e.printStackTrace();
            System.err.println( e.getClass().getName() + ": " + 			e.getMessage() );
            System.exit(0);
        }
    }

    //Under Construction
    public void executeScript(File f) throws IOException{
        /*Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:cloudScheduler.db");

            System.out.println("Opened database successfully");
            ScriptRunner sr = new ScriptRunner();
            String sql = str;
            //stmt.executeUpdate(sql);
            stmt = c.createStatement();
//            String sql2 = "INSERT INTO Framework values ('1','Framework1',20,56.0,1,10)";
            stmt.executeUpdate(sql);
            stmt.close();
            //c.commit();
            c.close();
        } catch ( Exception e ) {
            System.out.println("here??????");
            e.printStackTrace();
            System.err.println( e.getClass().getName() + ": " + 			e.getMessage() );
            System.exit(0);
        }*/
    }

    //Deletes all the rows in all the tables in DB
    public void clearDB()throws Exception{
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:cloudScheduler.db");

            System.out.println("Opened Database Successfully");
            //stmt.executeUpdate(sql);
            stmt = c.createStatement();
            String sql = "Delete from Framework;";
            stmt.executeUpdate(sql);

            sql = "Delete from Slave;";
            stmt.executeUpdate(sql);
            stmt.close();
            //c.commit();
            c.close();
        } catch ( Exception e ) {
            System.out.println("here??????");
            e.printStackTrace();
            System.err.println( e.getClass().getName() + ": " + 			e.getMessage() );
            System.exit(0);
        }
    }

}
