/**
 * Created by chemistry_sourabh on 3/12/16.
 */
public class Test {

    public static void main(String args[])
    {
        String s1 =  "sudo sed -i '1s/^/ 192.168.0.126 spark-slave-6\\n /' /etc/hosts";
        String s2 = "nohup ./hadoop-2.5.0-cdh5.2.0/bin/hadoop-daemon.sh start datanode &>/dev/null &";
        String s3 = "nohup mesos slave --master=192.168.0.103:5050 --no-hostname_lookup --quiet &>/dev/null &";
        SshProxy ssh = new SshProxy();
        try {
            ssh.executeCommand("192.168.0.126",s1);
            ssh.executeCommand("192.168.0.126",s2);
            ssh.executeCommand("192.168.0.126",s3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
