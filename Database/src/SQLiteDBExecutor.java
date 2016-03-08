import java.io.File;
import java.sql.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by Akshaya on 05-03-2016.
 */
public class SQLiteDBExecutor implements DBExecutor {

    //Executes a Select statement and returns a Double Dimensional String array
    public ArrayList<String[]> executeSelect(String str){
        Connection c = null;
        Statement stmt = null;
//        String [][] table_values = new String[100][20];
        ArrayList<String[]> table_values = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(
                    "jdbc:sqlite:cloudScheduler.db");
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(str);
            ResultSetMetaData rsmd= rs.getMetaData();
            int colCount = rsmd.getColumnCount();
//            String [][] table_values = new String[100][colCount];
            while(rs.next()){
                String[] values = new String [colCount];
                for(int i = 1;i <= rsmd.getColumnCount();i++){
                    values[i-1] = rs.getObject(i).toString();
                }
                table_values.add(values);
            }
            rs.close();
            stmt.close();
            c.close();
//            System.out.println("table_values:"+table_values);
//            System.out.println(table_values.get(0)[1]);
        } catch(Exception e){
            e.printStackTrace();
            System.err.println( e.getClass().getName()
                    + ": " + e.getMessage() );
        }
        return table_values;
    }
    // Executes any DML statement
    public void executeUpdate(String str){
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
        }
    }

    //Under Construction
    public void executeScript(File f) throws Exception{
        /*Connection c = null;
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
        }*/
        Connection conn = null;
        Statement stmt = null;
        BufferedReader br = new BufferedReader(new FileReader(f));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            String all_Sql = sb.toString();
            String[] sql = all_Sql.split(";");

            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:cloudScheduler.db");
            System.out.println("Opened database successfully");

            stmt = conn.createStatement();
            for (int i = 0; i < sql.length; i++) {
                stmt.executeUpdate(sql[i]);
            }
            stmt.close();
            conn.close();
        }catch ( Exception e ) {
            System.out.println("Exception??????");
            e.printStackTrace();
            System.err.println( e.getClass().getName() + ": " + 			e.getMessage() );
            System.exit(0);
        }
    }

    //Deletes all the rows in all the tables in DB
    public void clearDB(){
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
        }
    }

}
