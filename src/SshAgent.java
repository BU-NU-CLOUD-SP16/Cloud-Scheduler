import com.jcraft.jsch.*;

import java.io.*;
import java.util.Properties;

/**
 * Created by chemistry_sourabh on 3/11/16.
 */
    public class SshAgent {
    public void execute(String[] commands) {

        String host = "129.10.3.91";
        String user = "ubuntu";

        //your paraphrase here
        String paraphrase = "***********";


        //private key location
        String privateKey = "/Users/chemistry_sourabh/.ssh/id_rsa";

        JSch jsch = new JSch();


        try {
            jsch.addIdentity(privateKey);

            Session session = jsch.getSession(user, host, 22);
            Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
//            config.put("PreferredAuthentications",
//                    "publickey");

            session.setConfig(config);
            session.connect(20000);

            Channel channel = session.openChannel("shell");

//            ((ChannelExec) channel).setCommand(command);

            // this is the key line that sets AgentForwading to true
            ((ChannelShell) channel).setAgentForwarding(true);

//            channel.setInputStream(null);

            InputStream in = ((ChannelShell) channel).getInputStream();

            BufferedReader bi = new BufferedReader(new InputStreamReader(in));

            OutputStream outputStream = ((ChannelShell) channel).getOutputStream();

            DataOutputStream bo = new DataOutputStream(outputStream);

            channel.connect(10000);
            String s;
            for(String command : commands)
            {
                bo.writeChars(command);

                while ((s = bi.readLine()) != null) {
                    System.out.println(s);
                }
            }

//            Thread.sleep(5000);

            channel.disconnect();
            session.disconnect();
            System.out.println("DONE");

        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
