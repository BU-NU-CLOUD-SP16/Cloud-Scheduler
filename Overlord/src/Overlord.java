import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

/**
 * Created by Praveen on 3/26/2016.
 */
public class Overlord {

    private static final int HTTP_SUCCESS_RESPONSE = 200;
    private static final int HTTP_CREATE_CODE = 201;
    private static final int HTTP_CLIENT_ERROR = 400;
    private static final int HTTP_SERVER_ERROR = 500;

    private OverlordPolicyInfo policyConfigHandle;
    private HttpEndPoints httpHandle;
    private  ArrayList<Node> nodeList;
    private AgentList registeredAgents;
    private int maxNodes = 5;

    private ArrayList<Agent> pendingNodeRequests;

    public Overlord(){
        policyConfigHandle = new OverlordPolicyInfo();
        httpHandle = new HttpEndPoints(this);
        registeredAgents = new AgentList();
        nodeList = new ArrayList<>();
        pendingNodeRequests = new ArrayList<>();
    }


    public int registerCEAgent(String jsonString, String ceAgentIP, int ceAgentPort) {

        Gson gson = new Gson();
        JsonObject object = gson.fromJson(jsonString, JsonObject.class);

        int ceAgentID = object.get("ceAgentID").getAsInt();

        JsonArray jsonArray = object.get("nodes").getAsJsonArray();

        ArrayList<Node> nodes = new ArrayList<>();

        for(JsonElement jsonElement : jsonArray)
        {
           nodes.add(findNodeWithName(jsonElement.getAsJsonObject().get("name").getAsString()));
        }

        if(!policyConfigHandle.isPolicyConfigured(ceAgentID)){
            return HTTP_CLIENT_ERROR;
        }

        registeredAgents.add(ceAgentID);

        Agent ceAgent = registeredAgents.get(ceAgentID);
        ceAgent.setId(ceAgentID);
        ceAgent.setPriority(policyConfigHandle.getClusterPriority(ceAgentID));
        ceAgent.setIp(ceAgentIP);
        ceAgent.setPort(ceAgentPort);
        ceAgent.setNodeList(nodes);
        return HTTP_SUCCESS_RESPONSE;
    }

    public String requestNode(String jsonString) {

        Gson gson = new Gson();
        Integer ceAgentID = gson.fromJson(jsonString, JSONRequestNode.class).getCeAgentID();

        if( registeredAgents.contains(ceAgentID)) {
            int slaveCount = getLargestSlaveNumber() + 1;
            String serverID = null;
            try {
//                OpenStackCommand command = new OpenStackCommand();
//                command.setCommand("create");
//                command.setNodeName("Spark-Slave-"+
//                        slaveCount + "" +
//                        ".cloud");
//                overlordHandle.getOpenStackHandle().getWorkerQueue().put(command);
                if(nodeList.size() < maxNodes)
                {
                    OpenStackWrapper openStackWrapper = new OpenStackWrapper();
                    OpenStackCommand command = new OpenStackCommand();
                    command.setCommand("create");
                    command.setNodeName("Spark-Slave-"+slaveCount+".cloud");
                    openStackWrapper.getWorkerQueue().add(command);
                    Thread t = new Thread(openStackWrapper);
                    t.start();
                    Node node = (Node) openStackWrapper.getResponseQueue().take();
                    nodeList.add(node);
                    registeredAgents.get(ceAgentID).addNodeToList(node);
                    return "{\"name\":\""+node.getName()+"\"}";
                }

                else if(nodeList.size() == maxNodes)
                {
                    ArrayList<Agent> agents = registeredAgents.getLowerPriorityAgents(ceAgentID);
                    AgentCommunicator communicator = new AgentCommunicator();
                    boolean foundAgent = false;
                    for(Agent agent : agents)
                    {
                        if(agent.getNodeList().size() > agent.getMinFixedNodes()) {
                            communicator.sendReturnRevocableNodeSignal(agent, 1);
                            pendingNodeRequests.add(registeredAgents.get(ceAgentID));
                            foundAgent = true;
                            break;
                        }
                    }


                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            try {
//                serverID = httpHandle.getResponseQueue().take();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

//            registeredAgents.get(ceAgentID).addNodeToList(serverID);
            return "";
        }
        else
            return "";
    }

    public void releaseNode(String jsonString) {

        Gson gson = new Gson();
//        JSONReleaseNode releaseNodeDetails = gson.fromJson(jsonString, JSONReleaseNode.class);


        JsonObject object = new Gson().fromJson(jsonString,JsonObject.class);

        if(registeredAgents.contains(object.get("ceAgentID").getAsInt())) {

//            OpenStackCommand command = new OpenStackCommand();
//            command.setCommand("delete");
//            command.setNodeName(releaseNodeDetails.getServerID());
//
//            try {
//                openStackHandle.getWorkerQueue().put(command);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            if(pendingNodeRequests.size() > 0)
            {
                Agent pendingAgent = pendingNodeRequests.remove(0);
                Node node = findNodeWithName(object.get("name").getAsString());
                pendingAgent.addNodeToList(node);
            }

            else {
//                openStackHandle.deleteNode(findNodeWithName(object.get("name").getAsString()).getId());
            }

//            registeredAgents.get(releaseNodeDetails.getCeAgentID()).removeNodeFromList(releaseNodeDetails.getServerID());
        }

    }

    public String getNodeList(String jsonString) {

        Gson gson = new Gson();
        JsonObject object = gson.fromJson(jsonString, JsonObject.class);

        int ceAgentID = object.get("ceAgentID").getAsInt();
        JsonElement nameElement = object.get("name");
        String name = null;
        if(nameElement != null)
        {
            name = nameElement.getAsString();
        }
        JsonObject obj = new JsonObject();
        JsonArray array = new JsonArray();
        if( registeredAgents.contains(ceAgentID)) {

            if(name == null) {

                for (Node node : registeredAgents.get(ceAgentID).getNodeList()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("name", node.getName());
                    jsonObject.addProperty("flavor", node.getFlavor());
                    jsonObject.addProperty("ip", node.getIp());
                    jsonObject.addProperty("id", node.getId());
                    jsonObject.addProperty("status", node.getStatus());
                    array.add(jsonObject);
                }
            }

            else {
                for (Node node : registeredAgents.get(ceAgentID).getNodeList()) {
                    if(node.getName().equalsIgnoreCase(name)) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("name", node.getName());
                        jsonObject.addProperty("flavor", node.getFlavor());
                        jsonObject.addProperty("ip", node.getIp());
                        jsonObject.addProperty("id", node.getId());
                        jsonObject.addProperty("status", node.getStatus());
                        array.add(jsonObject);
                        break;
                    }
                }
            }
        }

        obj.add("nodes",array);

        return obj.toString();
    }

    private int getLargestSlaveNumber()
    {
        String firstHost = null;
        try {
            firstHost = nodeList.get(0).getName();
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }

        int max = Integer.parseInt(""+firstHost.charAt(firstHost.length() - 7));

        ArrayList<Node> slaves1 = new ArrayList<>(nodeList);
        slaves1.remove(0);

        for(Node slave : slaves1)
        {
            String hostname = slave.getName();
            int num = Integer.parseInt(""+hostname.charAt(hostname.length() - 7));
            if(num > max)
            {
                max = num;
            }
        }

        return max;
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



    public static void main(String args[]) throws InterruptedException {

        Overlord overlordHandle = new Overlord();

        overlordHandle.getPolicyConfigHandle().LoadPolicyInfo();



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
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
                newList.add(findNodeWithName(node.getName()));
            }

            agent.setNodeList(newList);
        }
    }


    public OverlordPolicyInfo getPolicyConfigHandle() {
        return policyConfigHandle;
    }

    public void setPolicyConfigHandle(OverlordPolicyInfo policyConfigHandle) {
        this.policyConfigHandle = policyConfigHandle;
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
