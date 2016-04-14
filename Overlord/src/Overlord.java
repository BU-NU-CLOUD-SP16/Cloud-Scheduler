import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import static java.lang.Thread.sleep;
import static spark.Spark.port;
import static spark.Spark.post;

/**
 * Created by Praveen on 3/26/2016.
 */
public class Overlord {

    public static final int ACTIVE_STATUS = 0;
    public static final int CREATE_STATUS = 1;
    public static final int DELETE_STATUS = 2;


    private HttpEndPoints httpHandle;
    private ArrayList<Node> nodeList;
    private CommandLineArguments commandLineArguments;
    private int maxNodes = 7;


    private ArrayList<Agent> pendingNodeRequests;

    public Overlord() {
        nodeList = new ArrayList<>();
        pendingNodeRequests = new ArrayList<>();
        commandLineArguments = new CommandLineArguments();
    }

    public static void main(String args[]) throws InterruptedException {


        CommandLineArguments commandLineArguments = new CommandLineArguments();
        commandLineArguments.parseCommandLineArguments(args);

        Overlord overlordHandle = new Overlord(commandLineArguments);


        port(commandLineArguments.getPort());
        post("/registerCEAgent", (request, response) -> {
            overlordHandle.registerCEAgent(request.body());
            System.out.println("Got register");
            return SUCCESS;
        });

        post("/requestNode", (request, response) -> {
            String decision = overlordHandle.requestNode(request.body());
            response.type("application/json");

            switch (decision) {
                case Constants.HTTP_CREATE_CODE:
                    response.status(Integer.parseInt(Constants.HTTP_CREATE_CODE));
                    break;
                case Constants.HTTP_WAIT_CODE:
                    response.status(Integer.parseInt(Constants.HTTP_WAIT_CODE));
                    break;
                default:
                    response.status(Integer.parseInt(Constants.HTTP_DENY_CODE));
            }

            return decision;
        });


        while (true) {
            try {
                commandLineArguments.updateConfig();
                overlordHandle.setMaxNodes(Integer.parseInt(commandLineArguments.getConfig().getValueForKey("Max-Nodes")));
                OpenStackWrapper openStackWrapper = new OpenStackWrapper(commandLineArguments.getConfig().getValueForKey("Username"),commandLineArguments.getConfig().getValueForKey("Password"));
                openStackWrapper.getWorkerQueue().add(new ListCommand());
                Thread t = new Thread(openStackWrapper);
                t.start();
                ArrayList<Node> nodes = (ArrayList<Node>) openStackWrapper.getResponseQueue().take();
                overlordHandle.setNodeList(nodes);
                overlordHandle.remakeAgentsNodeList();
                overlordHandle.updateCloudState();
                sleep(commandLineArguments.getPollInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void registerCEAgent(String jsonString) {


    }

    public String requestNode(String jsonString) {
        System.out.println("Got request for new node");
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(jsonString, JsonObject.class);
        Integer ceAgentID = json.get("ceAgentId").getAsInt();
        Integer number = json.get("numberOfNodes").getAsInt();


        if (registeredAgents.contains(ceAgentID)) {


            if (registeredAgents.get(ceAgentID).getStatus() == CREATE_STATUS)
                return Constants.HTTP_DENY_CODE;

            if (nodeList.size() < maxNodes) {
                registeredAgents.get(ceAgentID).setStatus(CREATE_STATUS);
                return Constants.HTTP_CREATE_CODE;
            } else if (nodeList.size() == maxNodes) {
                ArrayList<Agent> agents = registeredAgents.getLowerPriorityAgents(ceAgentID);
                agents.sort((o1, o2) -> o1.getPriority() < o2.getPriority() ? 1 : 0);
                AgentCommunicator communicator = new AgentCommunicator();
                for (Agent agent : agents) {
                    if (agent.getNodeList().size() > agent.getMinFixedNodes()) {
                        pendingNodeRequests.add(registeredAgents.get(ceAgentID));
                        System.out.println("Sent Return Node Signal to Cluster " + agent.getId());
                        communicator.sendReturnRevocableNodeSignal(agent, 1);
                        return Constants.HTTP_WAIT_CODE;
                    }
                }

                return Constants.HTTP_DENY_CODE;

            }
        }

        return Constants.HTTP_DENY_CODE;
    }

    private Node findNodeWithName(String name) {
        for (Node node : nodeList) {
            if (node.getName().equalsIgnoreCase(name)) {
                return node;
            }
        }
        return null;
    }

    private void updateCloudState() {
        AgentCommunicator agentCommunicator = new AgentCommunicator();
        ArrayList<Agent> agents = registeredAgents.getAll();

        for (Agent agent : agents) {
            String state = agentCommunicator.getAgentState(agent);

            if (state.equals("")) {
                registeredAgents.remove(agent.getId());
                continue;
            }

            Gson gson = new Gson();

            JsonObject object = gson.fromJson(state, JsonObject.class);

            agent.setPriority(object.get(CLUSTER_PRIORITY_JSON).getAsDouble());
            agent.setStatus(object.get(CLUSTER_STATUS_JSON).getAsInt());
            ArrayList<Node> nodes = new ArrayList<>();
            JsonArray array = object.get(CLUSTER_NODES_JSON).getAsJsonArray();

            for (JsonElement element : array) {
                Node node = findNodeWithName(element.getAsString());
                if (node != null)
                    nodes.add(node);
            }

            if (nodes.size() < agent.getNodeList().size() && pendingNodeRequests.size() > 0) {
                Agent agent1 = pendingNodeRequests.remove(0);
                System.out.println("Sent Create Signal to Cluster " + agent1.getId());
                agent.setStatus(CREATE_STATUS);
                agentCommunicator.sendCreateNodeSignal(agent1);
                agent.setNodeList(nodes);
            }
            else {
                agent.setNodeList(nodes);
            }
        }
    }

    private void remakeAgentsNodeList() {
        for (Agent agent : registeredAgents.getAll()) {
            ArrayList<Node> oldList = agent.getNodeList();
            ArrayList<Node> newList = new ArrayList<>();

            for (Node node : oldList) {
                Node node1 = findNodeWithName(node.getName());
                if (node1 != null) {
                    newList.add(node1);
                } else if (node.getIp() == null) {
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

    public Overlord(CommandLineArguments commandLineArguments) {
        this.commandLineArguments = commandLineArguments;
    }
}
