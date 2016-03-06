/**
 * Created by Akshaya on 05-03-2016.
 */
import java.io.File;
import java.io.IOException;

public interface IDBExecutor {
    public String[][] executeSelect(String str)throws Exception;
    public void executeUpdate(String str) throws Exception;
    public void executeScript(File f) throws IOException;
    public void clearDB()throws Exception;
}
