import java.util.HashMap;

/**
 * Created by chemistry_sourabh on 3/22/16.
 */
public class Config {

    HashMap<String,String> configData;

    public Config() {
        configData = new HashMap<>();
    }

    public void addValueForKey(String key,String value)
    {
        configData.put(key,value);
    }

    public String getValueForKey(String key)
    {
        return configData.get(key);
    }
}
