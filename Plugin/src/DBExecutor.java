/**
 * Created by Akshaya on 05-03-2016.
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public interface DBExecutor {
    public ArrayList<String[]> executeSelect(String str);
    public void executeUpdate(String str);
    public void executeScript(File f) throws Exception;
    public void clearDB();
}
