import java.io.BufferedReader;
import java.io.IOException;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by Akshaya on 07-03-2016.
 */
import dnl.utils.text.table.TextTable;

public class DBOutput {
    public static void main(String args[])throws IOException{
        String sql_f = "SELECT * FROM Framework;";
//        String sql_f_headers = "SELECT name, sql FROM sqlite_master WHERE type=\"table\";";// AND name = Framework;";
//        String sql_f_headers = "SELECT name, sql FROM sqlite_master;";
        String sql_s = "SELECT * FROM Slave;";

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String dbLocation = br.readLine();
        SQLiteDBExecutor obj = new SQLiteDBExecutor(dbLocation);
        while(1==1) {
            try
            {
                Thread.sleep(1000);
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();

            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
            ArrayList<String[]> framework = obj.executeSelect(sql_f);

            ArrayList<String[]> slave = obj.executeSelect(sql_s);

            String columnNames[] = {"Frameworkid", "FrameworkName", "CPU", "Memory", "Active", "Scheduled Tasks", "TimeStamp"};
            String[][] data = new String[framework.size()][7];
            if (!(framework.isEmpty())) {
                for (int i = 0; i < framework.size();i++)
                    for (int j = 0; j < framework.get(i).length; j++) {
                        data[i][j] = framework.get(i)[j];
                    }
            }

            TextTable tt = new TextTable(columnNames, data);
            tt.printTable();


            String slave_columnNames[] = {"SlaveID", "Load_5min", "Free Mem", "Total Mem", "CPU", "Allocated_CPU", "IP", "HostName", "TimeStamp"};
            String[][] slave_data = new String[slave.size()][9];
            if (!(slave.isEmpty())) {
                for (int i = 0; i < slave.size();i++)
                    for (int j = 0; j < slave.get(i).length; j++) {
                        slave_data[i][j] = slave.get(i)[j];
                    }
            }
            TextTable tt2 = new TextTable(slave_columnNames, slave_data);
            tt2.printTable();

        }
    }
}
