import java.util.HashMap;

/**
 * Created by Praveen on 3/26/2016.
 */
public class CEAgentList {

    private HashMap<Integer, CEAgentInfo> ceAgentMap;

    public CEAgentList(){
        this.ceAgentMap = new HashMap<>();
    }

    public CEAgentList(HashMap<Integer, CEAgentInfo> ceAgentMap) {
        this.ceAgentMap = ceAgentMap;
    }

    public CEAgentInfo AddNewCEAgent(Integer ceAgentID){
        CEAgentInfo newAgent = new CEAgentInfo();

        this.ceAgentMap.put(ceAgentID, newAgent);
        return newAgent;
    }

    public void DeleteCEAgent(Integer ceAgentID){

        this.ceAgentMap.remove(ceAgentID);
    }

    public CEAgentInfo getCEAgentDetails(Integer ceAgentID){
        return this.ceAgentMap.get(ceAgentID);
    }

    public boolean isRegisteredCEAgent( Integer ceAgentID ){
        return (this.ceAgentMap.get(ceAgentID) != null);
    }
}
