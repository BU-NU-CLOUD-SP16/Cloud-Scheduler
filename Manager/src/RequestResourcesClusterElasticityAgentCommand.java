/**
 * Created by chemistry_sourabh on 3/4/16.
 */
public class RequestResourcesClusterElasticityAgentCommand implements ClusterElasticityAgentCommand {

    private ElasticityPlugin elasticityPlugin;
    private ClusterScalerPlugin clusterScalerPlugin;
    private String parameters;

    public RequestResourcesClusterElasticityAgentCommand(ElasticityPlugin elasticityPlugin, ClusterScalerPlugin clusterScalerPlugin,String parameters) {
        this.elasticityPlugin = elasticityPlugin;
        this.clusterScalerPlugin = clusterScalerPlugin;
        this.parameters = parameters;
    }

    @Override
    public void execute() {
        int newNodesCount = elasticityPlugin.requestResources(parameters);
        for(int i=0;i<newNodesCount;i++)
        {
            clusterScalerPlugin.createNewNode();
        }
    }

}
