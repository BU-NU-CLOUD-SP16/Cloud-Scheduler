/**
 * Created by Akshaya on 06-03-2016.
 */
public class DBTest {
    public static void main(String args[]) {
        SQLiteDBExecutor obj = new SQLiteDBExecutor();
        SQLiteJDBC obj1 = new SQLiteJDBC();
        String str = "INSERT INTO Framework values ('2','Framework1',20,56.0,1,10);";
        String str2 = "SELECT * FROM Framework;";
        try {
            obj.executeSelect(str2);
//            obj.executeUpdate(str);
//            obj.clearDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
