import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class CEAgentPolicyListDeserializer implements JsonDeserializer<CEAgentPolicyList>
{
    @Override
    public CEAgentPolicyList deserialize(JsonElement jsonCEAgentPolicyList, Type typeOfSrc, JsonDeserializationContext
            context)
    {
        JsonArray jArray = (JsonArray) jsonCEAgentPolicyList;

        ArrayList<CEAgentPolicyInfo> policyList = new ArrayList<>();

        for (int i=1; i<jArray.size(); i++) {

            JsonObject jObject = (JsonObject) jArray.get(i);
            policyList.add(new CEAgentPolicyInfo(jObject.get("ceAgentID").getAsInt(),
                    jObject.get("ceAgentPriority").getAsInt()));
        }

        CEAgentPolicyList ceAgentList = new CEAgentPolicyList();
        ceAgentList.setCeAgentPolicyList(policyList);

        return ceAgentList;
    }
}
