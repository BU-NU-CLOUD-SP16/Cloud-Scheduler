import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Created by chemistry_sourabh on 4/28/16.
 */
public class Testing {


    public static void main(String[] args) {


        try
        {
            HttpResponse<String> response = Unirest.post("http://129.10.3.91:8000/execute").body("hadoop-daemon.sh start datanode").asString();
            System.out.println(response.getBody());
        }

        catch (UnirestException ex)
        {
            ex.printStackTrace();
        }
    }
}
