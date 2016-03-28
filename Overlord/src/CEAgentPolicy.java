/**
 * Created by Praveen on 3/27/2016.
 */
public class CEAgentPolicy {

    private CEAgentPolicyList agentPolicyList;

    public CEAgentPolicy(){
        this.agentPolicyList = new CEAgentPolicyList();
    }

    public CEAgentPolicyList getAgentPolicyList() {
        return this.agentPolicyList;
    }

    public void setAgentPolicyList(CEAgentPolicyList agentPolicyList) {
        this.agentPolicyList = agentPolicyList;
    }

    @Override
    public String toString(){
        return agentPolicyList.toString();
    }

}
