import java.io.File;
import java.sql.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by Akshaya on 05-03-2016.
 */
public class SQLiteDBExecutor implements DBExecutor {

    private Connection c =null;
    private Statement stmt = null;

    private String dblocation = "";
    private String id;


    private void createConnection()
    {
        try {
        Class.forName("org.sqlite.JDBC");
            if (!dblocation.equals(""))
                c = DriverManager.getConnection("jdbc:sqlite:" + dblocation);
            else
                c = DriverManager.getConnection("jdbc:sqlite:cloudScheduler"+id+".db");

    } catch (Exception e){
        e.printStackTrace();
        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
    }
    }

    //Executes a Select statement and returns a Double Dimensional String array
    public ArrayList<String[]> executeSelect(String str){

        ArrayList<String[]> table_values = new ArrayList<>();
        try {
            createConnection();
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(str);
            ResultSetMetaData rsmd= rs.getMetaData();
            int colCount = rsmd.getColumnCount();
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
        } catch(Exception e){
            e.printStackTrace();
            System.err.println( e.getClass().getName()
                    + ": " + e.getMessage() );
        }
        return table_values;
    }
    // Executes any DML statement
    public void executeUpdate(String str){
        try{
            String sql = str;
            createConnection();
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            System.out.println("here??????");
            e.printStackTrace();
            System.err.println( e.getClass().getName() + ": " + 			e.getMessage() );
        }
    }

    //Executes a list of DML statements
    public void executeScript(File f) throws Exception{
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

            createConnection();
            stmt = c.createStatement();
            for (int i = 0; i < sql.length; i++) {
                stmt.executeUpdate(sql[i]);
            }
            stmt.close();
            c.close();
        }catch ( Exception e ) {
            System.out.println("Exception??????");
            e.printStackTrace();
            System.err.println( e.getClass().getName() + ": " + 			e.getMessage() );
            System.exit(0);
        }
    }

    //Deletes all the rows in all the tables in DB
    public void clearDB(){
        try {
            createConnection();
            stmt = c.createStatement();
            String sql = "Delete from Framework;";
            stmt.executeUpdate(sql);

            sql = "Delete from Slave;";
            stmt.executeUpdate(sql);

            sql = "Delete from Runs_On";
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            System.out.println("here??????");
            e.printStackTrace();
            System.err.println( e.getClass().getName() + ": " + 			e.getMessage() );
        }
    }


//    public SQLiteDBExecutor(){
//        try {
//            Class.forName("org.sqlite.JDBC");
//            c = DriverManager.getConnection("jdbc:sqlite:cloudScheduler.db");
//        } catch (Exception e){
//            e.printStackTrace();
//            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
//        }
//
//    }


    public SQLiteDBExecutor(String id){
       this.id = id;

    }

    public SQLiteDBExecutor(String id,String dbLocation){
        this.id = id;
        this.dblocation = dbLocation;
    }

}