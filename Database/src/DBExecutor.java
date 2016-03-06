/**
 * Created by Akshaya on 05-03-2016.
 */
import java.io.File;
import java.io.IOException;

public interface DBExecutor {
    public String[][] executeSelect(String str);
    public void executeUpdate(String str);
    public void executeScript(File f) throws IOException;
    public void clearDB();
}
