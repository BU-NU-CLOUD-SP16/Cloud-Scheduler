import java.util.ArrayList;

/**
 * Created by Akshaya on 07-03-2016.
 */
import dnl.utils.text.table.TextTable;

public class DBOutput {
    public static void main(String args[]){
        String sql_f = "SELECT * FROM Framework;";
//        String sql_f_headers = "SELECT name, sql FROM sqlite_master WHERE type=\"table\";";// AND name = Framework;";
//        String sql_f_headers = "SELECT name, sql FROM sqlite_master;";
        String sql_s = "SELECT * FROM Slave;";
        SQLiteDBExecutor obj = new SQLiteDBExecutor();
        ArrayList<String []> framework = obj.executeSelect(sql_f);

        ArrayList<String []> slave = obj.executeSelect(sql_s);
//        System.out.format("%-15s%-15s%-15s%-15s%-15s%-15s","Frameworkid","FrameworkName","CPU","Memory","Active","Scheduled Tasks");
//        System.out.println();
//        for (int i = 0;i < framework.size();i++){
//            for (int j = 0;j < framework.get(i).length;j++)
//                System.out.format("%-15s",framework.get(i)[j]);
//            System.out.println();
//        }
//        System.out.println("\n");
//        for (int i = 0;i < slave.size();i++){
//            for (int j = 0;j < slave.get(i).length;j++)
//                System.out.format("%15s",slave.get(i)[j]);
//            System.out.println();
//        }

        String columnNames[] = {"Frameworkid","FrameworkName","CPU","Memory","Active","Scheduled Tasks","TimeStamp"};
        String [][] data = new String[framework.size()][7];
        if (!(framework.isEmpty())) {
            for (int i = 0;i < framework.get(0).length;i++){
                data[0][i] = framework.get(0)[i];
            }
        }

        TextTable tt = new TextTable(columnNames,data);
        tt.printTable();


        String slave_columnNames[] = {"SlaveID","Load_5min","Free Mem","Total Mem","CPU","Allocated_CPU","IP","HostName","TimeStamp"};
        String [][] slave_data = new String[slave.size()][9];
        if (!(slave.isEmpty())) {
            for (int i = 0; i < slave.get(0).length; i++) {
                slave_data[0][i] = slave.get(0)[i];
            }
        }
        TextTable tt2 = new TextTable(slave_columnNames, slave_data);
        tt2.printTable();
    }
}
