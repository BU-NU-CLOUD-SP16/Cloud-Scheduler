/**
 * Created by Praveen on 3/27/2016.
 */

import java.util.ArrayList;

public class CEAgentPolicyList {

    private ArrayList<CEAgentPolicyInfo> ceAgentPolicyList;

    public ArrayList<CEAgentPolicyInfo> getCeAgentPolicyList() {
        return this.ceAgentPolicyList;
    }

    public void setCeAgentPolicyList(ArrayList<CEAgentPolicyInfo> ceAgentPolicyList) {
        this.ceAgentPolicyList = ceAgentPolicyList;
    }

    @Override
    public String toString(){
        String jsonString = "{ \"ClusterPolicy\": [ ";

        for ( CEAgentPolicyInfo ceAgent: this.ceAgentPolicyList ) {
            jsonString += ceAgent.toString();
        }

        return jsonString += "] }";
    }
}
