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
            HttpResponse<String> response = Unirest.post("http://192.168.0.104:8000/execute").body("mesos slave --master=192.168.0.4:5050 --quiet --hadoop_home=/home/ubuntu/hadoop-2.5.0-cdh5.2.0").asString();
            System.out.println(response.getBody());
        }

        catch (UnirestException ex)
        {
            ex.printStackTrace();
        }
    }
}
