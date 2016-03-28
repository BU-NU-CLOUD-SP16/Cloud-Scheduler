import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by Praveen on 3/27/2016.
 */
public class CEAgentPolicyInfo {

    private int ceAgentID;
    private int ceAgentPriority;

    public CEAgentPolicyInfo(int ceAgentID, int ceAgentPriority) {
        this.ceAgentID = ceAgentID;
        this.ceAgentPriority = ceAgentPriority;
    }

    public int getCeAgentID() {
        return ceAgentID;
    }

    public void setCeAgentID(int ceAgentID) {
        this.ceAgentID = ceAgentID;
    }

    public int getCeAgentPriority() {
        return ceAgentPriority;
    }

    public void setCeAgentPriority(int ceAgentPriority) {
        this.ceAgentPriority = ceAgentPriority;
    }

    @Override
    public String toString(){

        return String.format("{ \"ceAgentID\":%d, \"ceAgentPriority\":%d}", new Object[]{ceAgentID, ceAgentPriority});
    }
}

