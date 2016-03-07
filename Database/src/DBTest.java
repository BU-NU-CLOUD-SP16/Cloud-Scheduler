/**
 * Created by Akshaya on 06-03-2016.
 */
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

public class DBTest {
    public static void main(String args[]) throws IOException{
        SQLiteDBExecutor obj = new SQLiteDBExecutor();
        SQLiteJDBC obj1 = new SQLiteJDBC();
        String str = "INSERT INTO Framework values ('2','Framework1',20,56.0,1,10);";
        String str2 = "SELECT * FROM Framework;";
//        File f = new File("TestScript.sql");
        try {
//            obj.executeSelect(str2);
//            obj.executeUpdate(str);
            obj.clearDB();
//            obj.executeScript(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
