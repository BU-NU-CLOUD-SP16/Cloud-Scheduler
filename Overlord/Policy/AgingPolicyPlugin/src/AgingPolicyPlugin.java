import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by chemistry_sourabh on 4/14/16.
 */
public class AgingPolicyPlugin implements PolicyPlugin {


    private static final String CLUSTER_ID_JSON = "id";
    private static final String CLUSTER_PRIORITY_JSON = "priority";
    private static final String CLUSTER_MINIMUM_NODES_JSON = "minNodes";
    private static final String CLUSTER_PORT_JSON = "port";
    private static final String CLUSTER_NODES_JSON = "nodes";
    private static final String CLUSTER_STATUS_JSON = "status";
    private static final String NODE_NAME_JSON = "name";

    private static final String HTTP_CREATE_CODE = "200";
    private static final String HTTP_WAIT_CODE = "201";
    private static final String HTTP_DENY_CODE = "500";

    private static final int ACTIVE_STATUS = 0;
    private static final int CREATE_STATUS = 1;
    private static final int DELETE_STATUS = 2;

    private AgentList registeredAgents;
    private ArrayList<Agent> pendingNodeRequests;
    private ArrayList<OpenStackNode> nodeList;
    private int maxNodes;
    private Logger logger = GlobalLogger.globalLogger;

    public AgingPolicyPlugin() {
        registeredAgents = new AgentList();
        nodeList = new ArrayList<>();
        pendingNodeRequests = new ArrayList<>();
        nodeList = new ArrayList<>();
    }

    @Override
    public void setup(Config config)
    {
        maxNodes = Integer.parseInt(config.getValueForKey("Max-Nodes"));
        logger.log(Level.FINE,"Aging Policy Setup Done");
    }

    @Override
    public void registerAgent(String ip,String json) {
        Gson gson = new Gson();
        JsonObject object = gson.fromJson(json, JsonObject.class);

        logger.log(Level.FINE,"Json with Request = "+json);

        int ceAgentID = object.get(CLUSTER_ID_JSON).getAsInt();
        double priority = object.get(CLUSTER_PRIORITY_JSON).getAsDouble();
        int minFixedNodes = object.get(CLUSTER_MINIMUM_NODES_JSON).getAsInt();
        int port = object.get(CLUSTER_PORT_JSON).getAsInt();
        int status = object.get(CLUSTER_STATUS_JSON).getAsInt();


        JsonArray jsonArray = object.get(CLUSTER_NODES_JSON).getAsJsonArray();

        ArrayList<OpenStackNode> openStackNodes = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            OpenStackNode openStackNode = findNodeWithName(jsonElement.getAsString());
            if (openStackNode != null)
                openStackNodes.add(openStackNode);
        }


        Agent ceAgent = new Agent();
        ceAgent.setId(ceAgentID);
        ceAgent.setPriority(priority);
        ceAgent.setIp(ip);
        ceAgent.setPort(port);
        ceAgent.setMinFixedNodes(minFixedNodes);
        ceAgent.setOpenStackNodeList(openStackNodes);
        ceAgent.setStatus(status);
        registeredAgents.add(ceAgent);
    }

    @Override
    public String requestNode(String jsonString) {
        System.out.println("Got request for new node");
        Gson gson = new Gson();

        logger.log(Level.FINE,"Json with Request = "+jsonString);

        JsonObject json = gson.fromJson(jsonString, JsonObject.class);
        Integer ceAgentID = json.get("ceAgentId").getAsInt();
        Integer number = json.get("numberOfNodes").getAsInt();

        if (registeredAgents.contains(ceAgentID)) {


            if (registeredAgents.get(ceAgentID).getStatus() == CREATE_STATUS)
                return HTTP_DENY_CODE;

            if (nodeList.size() + numberOfCreatingAgents() < maxNodes) {
                registeredAgents.get(ceAgentID).setStatus(CREATE_STATUS);
                return HTTP_CREATE_CODE;
            } else if (nodeList.size() + numberOfCreatingAgents() == maxNodes) {
                ArrayList<Agent> agents = registeredAgents.getLowerPriorityAgents(ceAgentID);
                agents.sort((o1, o2) -> o1.getPriority() < o2.getPriority() ? 1 : 0);
                AgentCommunicator communicator = new AgentCommunicator();
                for (Agent agent : agents) {
                    if (agent.getOpenStackNodeList().size() > agent.getMinFixedNodes()) {
                        pendingNodeRequests.add(registeredAgents.get(ceAgentID));
                        communicator.sendReturnRevocableNodeSignal(agent, 1);
                        logger.log(Level.INFO,"Sent Signal to Return 1 node to Agent "+agent.getId());
                        return HTTP_WAIT_CODE;
                    }
                }

                return HTTP_DENY_CODE;

            }
        }

        return HTTP_DENY_CODE;
    }

    private int numberOfCreatingAgents()
    {
        ArrayList<Agent> agents = registeredAgents.getAll();

        int count = 0;

        for (Agent agent : agents)
        {
            if(agent.getStatus() == CREATE_STATUS)
                count++;
        }

        return  count;
    }

    @Override
    public HashMap<String,String> getAgentHostnames()
    {
        ArrayList<Agent> agents = registeredAgents.getAll();
        HashMap<String,String> hostnames = new HashMap<>();

        for (Agent agent : agents)
        {
           hostnames.put(""+agent.getId(),agent.getIp()+":"+agent.getPort());
        }

        return hostnames;
    }


    private void remakeAgentsNodeList() {
        for (Agent agent : registeredAgents.getAll()) {
            ArrayList<OpenStackNode> oldList = agent.getOpenStackNodeList();
            ArrayList<OpenStackNode> newList = new ArrayList<>();

            for (OpenStackNode node : oldList) {
                OpenStackNode node1 = findNodeWithName(node.getName());
                if (node1 != null) {
                    newList.add(node1);
                } else if (node.getIp() == null) {
                    newList.add(node);
                }
            }

            agent.setOpenStackNodeList(newList);
            logger.log(Level.INFO,"Cluster "+agent.getId()+" has "+newList.size()+" nodes");
        }
    }

    @Override
    public void updateState(HashMap<String,String> clusterStates,ArrayList<Node> nodesInCloud) {
        ArrayList<Agent> agents = registeredAgents.getAll();

        for (Node node : nodesInCloud)
        {
            nodeList.add((OpenStackNode) node);
        }


        remakeAgentsNodeList();

        AgentCommunicator agentCommunicator = new AgentCommunicator();
        for (String id : clusterStates.keySet()) {

            Agent agent = registeredAgents.get(Integer.parseInt(id));

            String state = clusterStates.get(id);

            if (state.equals("")) {
                registeredAgents.remove(agent.getId());
                continue;
            }

            Gson gson = new Gson();

            JsonObject object = gson.fromJson(state, JsonObject.class);

            agent.setPriority(object.get(CLUSTER_PRIORITY_JSON).getAsDouble());
            agent.setStatus(object.get(CLUSTER_STATUS_JSON).getAsInt());
            ArrayList<OpenStackNode> nodes = new ArrayList<>();
            JsonArray array = object.get(CLUSTER_NODES_JSON).getAsJsonArray();

            for (JsonElement element : array) {
                OpenStackNode node = findNodeWithName(element.getAsString());
                if (node != null)
                    nodes.add(node);
            }

            if (nodes.size() < agent.getOpenStackNodeList().size() && pendingNodeRequests.size() > 0) {
                Agent agent1 = pendingNodeRequests.remove(0);
                agent.setStatus(CREATE_STATUS);
                agentCommunicator.sendCreateNodeSignal(agent1);
                logger.log(Level.INFO,"Sent Create Node Signal to Agent "+agent1);
                agent.setOpenStackNodeList(nodes);
            }
            else {
                agent.setOpenStackNodeList(nodes);
            }
        }
    }

    private OpenStackNode findNodeWithName(String name) {
        for (OpenStackNode openStackNode : nodeList) {
            if (openStackNode.getName().equalsIgnoreCase(name)) {
                return openStackNode;
            }
        }
        return null;
    }
}
