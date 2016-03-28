/**
 * Created by Praveen on 3/26/2016.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.*;

import static java.lang.System.exit;

public class OverlordPolicyInfo {

    private String policyConfigFile;
    private CEAgentPolicy ceAgentPolicy;

    private static final String DEFAULT_CONFIG_FILE = "/overlordConfig.json";

    public OverlordPolicyInfo() {
        this.policyConfigFile = DEFAULT_CONFIG_FILE;
    }

    public OverlordPolicyInfo(String configFile){
        this.policyConfigFile = configFile;
    }

    public void LoadPolicyInfo(){

        BufferedReader br = null;

        try {
            InputStream in = getClass().getResourceAsStream(policyConfigFile);
            br = new BufferedReader(
                    new InputStreamReader(in));
        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(CEAgentPolicyList.class, new CEAgentPolicyListDeserializer());
        Gson gson = gsonBuilder.create();
        this.ceAgentPolicy = gson.fromJson(br, CEAgentPolicy.class);

        System.out.println("Json Policy" + this.ceAgentPolicy.toString() );
    }

    public String getPolicyConfigFile() {
        return policyConfigFile;
    }

    public void setPolicyConfigFile(String policyConfigFile) {
        this.policyConfigFile = policyConfigFile;
    }

    public boolean isPolicyConfigured(Integer ceAgentID){
        for( CEAgentPolicyInfo ceAgent: this.ceAgentPolicy.getAgentPolicyList().getCeAgentPolicyList()){
            if( ceAgent.getCeAgentID() == ceAgentID )
                return true;
        }

        return false;
    }

    public Integer getClusterPriority(Integer ceAgentID){
        for( CEAgentPolicyInfo ceAgent: this.ceAgentPolicy.getAgentPolicyList().getCeAgentPolicyList()){
            if( ceAgent.getCeAgentID() == ceAgentID ) {
                return ceAgent.getCeAgentPriority();
            }
        }
        // Should never reach this point
        return -1;
    }
}
