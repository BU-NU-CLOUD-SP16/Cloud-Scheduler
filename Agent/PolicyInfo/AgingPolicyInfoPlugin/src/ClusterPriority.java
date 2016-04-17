/**
 * Created by chemistry_sourabh on 4/11/16.
 */
public class ClusterPriority {

    private int basePriority;
    private int jobPriorities;
    private int compensation;
    private double agingPriority;
    private double preventOscillationPriority;

    private int nodesSurrendered = 0;
    private int requestsSent = 0;

    public ClusterPriority(int basePriority) {
        this.basePriority = basePriority;
    }

    public void setJobPriorities(int jobPriorities) {
        this.jobPriorities = jobPriorities;
    }

    public void incrementNodesSurrendered()
    {
        nodesSurrendered++;
        compensation = (int) Math.pow(2,nodesSurrendered) - 1;
    }

    public void decrementNodesSurrendered()
    {
        if(nodesSurrendered > 0)
            nodesSurrendered--;
        preventOscillationPriority += compensation - ((int) Math.pow(2,nodesSurrendered) - 1);
        compensation = (int) Math.pow(2,nodesSurrendered) - 1;
    }

    public void incrementRequestsSent()
    {
        requestsSent++;
        agingPriority = (requestsSent * requestsSent)/1200.0;
    }

    public void resetRequestsSent()
    {
        requestsSent = 0;
        preventOscillationPriority += agingPriority;
        agingPriority = 0;
    }

    public void decrementPOPBySeconds(double seconds)
    {
        if(preventOscillationPriority > 0)
            preventOscillationPriority -= (seconds * 1.0) / 60;

        if(preventOscillationPriority < 0)
            preventOscillationPriority = 0;
    }

    public void incrementPOP()
    {
        preventOscillationPriority += 5;
    }

    public double getClusterPriority()
    {
        return basePriority + jobPriorities + compensation + agingPriority + preventOscillationPriority;
    }

    @Override
    public String toString() {
        return ""+getClusterPriority();
    }
}
