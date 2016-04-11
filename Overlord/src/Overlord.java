import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import static java.lang.Thread.sleep;

/**
 * Created by Praveen on 3/26/2016.
 */
public class Overlord {

    private static final int HTTP_SUCCESS_RESPONSE = 200;
    private static final int HTTP_CREATE_CODE = 201;
    private static final int HTTP_CLIENT_ERROR = 400;
    private static final int HTTP_SERVER_ERROR = 500;

    private HttpEndPoints httpHandle;
    private  ArrayList<Node> nodeList;
    private AgentList registeredAgents;
    private int maxNodes = 7;

    private ArrayList<Agent> pendingNodeRequests;

    public Overlord(){
        httpHandle = new HttpEndPoints(this);
        registeredAgents = new AgentList();
        nodeList = new ArrayList<>();
        pendingNodeRequests = new ArrayList<>();
    }


    public int registerCEAgent(String jsonString, String ceAgentIP) {

        Gson gson = new Gson();
        JsonObject object = gson.fromJson(jsonString, JsonObject.class);

        int ceAgentID = object.get("id").getAsInt();
        double priority = object.get("priority").getAsDouble();
        int minFixedNodes = object.get("minNodes").getAsInt();
        int port = object.get("port").getAsInt();

        JsonArray jsonArray = object.get("nodes").getAsJsonArray();

        ArrayList<Node> nodes = new ArrayList<>();

        for(JsonElement jsonElement : jsonArray)
        {
            Node node = findNodeWithName(jsonElement.getAsJsonObject().get("name").getAsString());
            if(node != null)
                nodes.add(node);
        }


        Agent ceAgent = new Agent();
        ceAgent.setId(ceAgentID);
        ceAgent.setPriority(priority);
        ceAgent.setIp(ceAgentIP);
        ceAgent.setPort(port);
        ceAgent.setMinFixedNodes(minFixedNodes);
        ceAgent.setNodeList(nodes);

        registeredAgents.add(ceAgent);
        return HTTP_SUCCESS_RESPONSE;
    }

    public String requestNode(String jsonString) {
        System.out.println("Got request for new node");
        Gson gson = new Gson();
        RequestNodeJSONObject request = gson.fromJson(jsonString, RequestNodeJSONObject.class);
        Integer ceAgentID = request.getCeAgentID();
        Integer number = request.getNumberOfNodes();

        if( registeredAgents.contains(ceAgentID)) {

                if(nodeList.size() < maxNodes)
                {
                    return "200";
                }

                else if(nodeList.size() == maxNodes)
                {
                    ArrayList<Agent> agents = registeredAgents.getLowerPriorityAgents(ceAgentID);
                    agents.sort((o1, o2) -> o1.getPriority() > o2.getPriority()?1:0);
                    AgentCommunicator communicator = new AgentCommunicator();
                    for(Agent agent : agents)
                    {
                        if(agent.getNodeList().size() > agent.getMinFixedNodes()) {
                            pendingNodeRequests.add(registeredAgents.get(ceAgentID));
                            communicator.sendReturnRevocableNodeSignal(agent, 1);
                            return "201";
                        }
                    }

                    return "";

                }
        }

        return "";
    }

    private Node findNodeWithName(String name)
    {
        for (Node node : nodeList)
        {
            if (node.getName().equalsIgnoreCase(name))
            {
                return node;
            }
        }
        return  null;
    }

    private Node findNodeWithId(String id)
    {
        for (Node node : nodeList)
        {
            if (node.getId().equalsIgnoreCase(id))
            {
                return node;
            }
        }
        return  null;
    }

    public static void main(String args[]) throws InterruptedException {

        Overlord overlordHandle = new Overlord();

        overlordHandle.getHttpHandle().configureHttpEndPoints();

        while(true) {
            try {
                OpenStackWrapper openStackWrapper = new OpenStackWrapper();
                OpenStackCommand command = new OpenStackCommand();
                command.setCommand("list");
                openStackWrapper.getWorkerQueue().add(command);
                Thread t = new Thread(openStackWrapper);
                t.start();
               ArrayList<Node> nodes = (ArrayList<Node>) openStackWrapper.getResponseQueue().take();
               overlordHandle.setNodeList(nodes);
                overlordHandle.remakeAgentsNodeList();
                overlordHandle.updateCloudState();
                System.out.println("Can start Agents");
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void updateCloudState() {
        AgentCommunicator agentCommunicator = new AgentCommunicator();
        ArrayList<Agent> agents = registeredAgents.getAll();

        for(Agent agent  : agents)
        {
            String state = agentCommunicator.getAgentState(agent);

            if(state.equals(""))
            {
                registeredAgents.remove(agent.getId());
                continue;
            }

            Gson gson = new Gson();

            JsonObject object = gson.fromJson(state,JsonObject.class);

            agent.setPriority(object.get("priority").getAsDouble());

            ArrayList<Node> nodes = new ArrayList<>();
            JsonArray array = object.get("nodes").getAsJsonArray();

            for(JsonElement element : array)
            {
               Node node = findNodeWithName(element.getAsString());
                if(node != null)
                    nodes.add(node);
            }

            if (nodes.size() < agent.getNodeList().size() && pendingNodeRequests.size() > 0)
            {
                Agent agent1 = pendingNodeRequests.remove(0);
                agentCommunicator.sendCreateNodeSignal(agent1);
                agent.setNodeList(nodes);
            }

            else
            {
                agent.setNodeList(nodes);
            }
        }
    }

    private void remakeAgentsNodeList() {
        for (Agent agent : registeredAgents.getAll())
        {
            ArrayList<Node> oldList = agent.getNodeList();
            ArrayList<Node> newList = new ArrayList<>();

            for (Node node : oldList)
            {
                Node node1 = findNodeWithName(node.getName());
                if(node1 != null) {
                    newList.add(node1);
                }

                else if(node.getIp() == null)
                {
                    newList.add(node);
                }
            }

            agent.setNodeList(newList);
        }
    }


    public HttpEndPoints getHttpHandle() {
        return httpHandle;
    }

    public void setHttpHandle(HttpEndPoints httpHandle) {
        this.httpHandle = httpHandle;
    }

    public AgentList getRegisteredAgents() {
        return registeredAgents;
    }

    public void setRegisteredAgents(AgentList registeredAgents) {
        this.registeredAgents = registeredAgents;
    }

    public ArrayList<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(ArrayList<Node> nodeList) {
        this.nodeList = nodeList;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }


}
