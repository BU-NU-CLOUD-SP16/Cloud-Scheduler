import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Created by chemistry_sourabh on 3/28/16.
 */
public class AgentCommunicator {


    public void sendReturnRevocableNodeSignal(Agent agent,int  numberOfNodes)
    {
        JsonObject object = new JsonObject();
        object.addProperty("number",""+numberOfNodes);

        try {
            HttpResponse<String> response = Unirest.post("http://"+agent.getIp()+":"+agent.getPort()+"/releaseNode").body(object.toString()).asString();

        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public void sendCreateNodeSignal(Agent agent) {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("number",1);
            HttpResponse<String> response = Unirest.post("http://"+agent.getIp()+":"+agent.getPort()+"/createNode").body(jsonObject.toString()).asString();
        }
        catch (UnirestException e)
        {

        }
    }
}
