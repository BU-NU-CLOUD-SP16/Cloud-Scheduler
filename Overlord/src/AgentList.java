import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Praveen on 3/26/2016.
 */
public class AgentList {

    private HashMap<Integer, Agent> ceAgentMap;

    public AgentList(){
        this.ceAgentMap = new HashMap<>();
    }

    public AgentList(HashMap<Integer, Agent> ceAgentMap) {
        this.ceAgentMap = ceAgentMap;
    }

    public Agent add(Integer ceAgentID){
        Agent newAgent = new Agent();

        this.ceAgentMap.put(ceAgentID, newAgent);
        return newAgent;
    }

    public void remove(Integer ceAgentID){

        this.ceAgentMap.remove(ceAgentID);
    }

    public ArrayList<Agent> getAll()
    {
        return new ArrayList<>(ceAgentMap.values());
    }

    public Agent get(Integer ceAgentID){
        return this.ceAgentMap.get(ceAgentID);
    }

    public boolean contains(Integer ceAgentID ){
        return (this.ceAgentMap.get(ceAgentID) != null);
    }

    public ArrayList<Agent> getLowerPriorityAgents(int id)
    {
        ArrayList<Agent> agents = new ArrayList<>(ceAgentMap.values());

        Agent mainAgent = get(id);

        agents.remove(mainAgent);

        ArrayList<Agent> lowAgents = new ArrayList<>();

        for(Agent agent : agents)
        {
            if(agent.getPriority() < mainAgent.getPriority())
            {
                lowAgents.add(agent);
            }
        }

        return lowAgents;
    }
}
