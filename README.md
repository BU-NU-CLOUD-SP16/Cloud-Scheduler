Cloud Scheduler in next generation data centers.

INSTALLATION GUIDE:
tar -xzvf cloud-scheduler-<version>.tar.gz
cd cloud-scheduler-<version> 
sudo ./install.sh

CONFIGURATION PARAMETERS:
To run overlord agent we need to specify some config parameters in the file at /etc/overlord-agent/config.json.

Below are the parameters that are required to be provided:
  
  # IP of the overlord
  "Overlord-Ip": "localhost"
  
  # the port on which overlord will be running 
  "Overlord-Port": "6000"
  
  # cluster id. Each cluster should have unique id
  "Id": "1"
  
  # Priority of the cluster
  "Base-Priority": "1"
  
  # IP of HDFS Namenode
  "Mesos-HDFS-Master": "192.168.0.128"
  
  # path to private key
  "SSH-Private-Key": "/Users/chemistry_sourabh/.ssh/id_rsa"
  
  # private key name
  "Key-Name": "Sourabh-OSX"
  
  # kaizen user name
  "Username": "Enter here"
  
  # kaizen password
  "Password": "Enter here"
  
  # mesos master ip address
  "Mesos-Master-Ip": "129.10.3.91"
  
  # mesos master port
  "Mesos-Master-Port": "5050"
  
  # framework priorities
  "Framework-Priorities": [{"name":"PageRank","priority":2},{"name":"Hadoop","priority":1}]
  
  # slave node prefix 
  "Node-Name-Prefix": "Mesos-Slave"
  
  # each node in the cluster should have this suffix
  "Node-Name-Suffix": ".cloud",
  
Below are some optional parameters:
  
  # specify the polling intervals in seconds
  "Poll-Interval": "5000"
  
  # the port on which the agent will be started
  "Port": "4500"

  # minimum number of nodes per cluster. This are irrevocable nodes
  "Min-Nodes": "2"
  
  # specify the list of irrevocable nodes
  "No-Delete-Slaves": ["Mesos-Slave-1-1.cloud","Mesos-Slave-1-2.cloud"]
  
  # security group this cluster belongs to
  "Cluster-Security-Group": "Cluster-1"
  
  # Cluster netowrk id
  "Cluster-Network-Id": "87286d17-9092-47ee-a284-4056065ae508"
  
  # new node flavor
  "New-Node-Flavor": "dcc95f79-1f29-49c3-a44c-2f915c4cf44e"
  
  # image id
  "Image-Name": "0418168d-724a-4517-b96c-9d627c64b17d"
  
  # if the cluster load goes beyond this point the cluster may scale up
  "Scale-Up-Cluster-Load": 0.85
  
  # if the cluster load goes beyond this point the cluster may scale down
  "Scale-Down-Cluster-Load": 0.8
  
  # if the cluster memory goes below this point the cluster may scale up
  "Scale-Up-Cluster-Memory": 0.0001
  
  # if the cluster memory goes above this point the cluster may scale down
  "Scale-Down-Cluster-Memory": 0.3
  
  # if the any of the slave load goes above this point the cluster may scale up
  "Scale-Up-Slave-Load": 0.85
  
  # if the any of the slave load goes below this point the cluster may scale down
  "Scale-Down-Slave-Load": 0.3
  
  # if the any of the slave memory goes below this point the cluster may scale up
  "Scale-Up-Slave-Memory": 0.1
  
  # if the cluster memory goes above this point the cluster may scale down
  "Scale-Down-Slave-Memory": 0.7
  
  # mesos elasticity plugin jar location (This jar has the class to scale the cluster up and down)
  "Manager-Plugin": "./Release/MesosElasticityPlugin.jar"
  
  # collector plugin jar location (This jar has the classes to collect the necessary metrics)
  "Collector-Plugin": "./Release/MesosCollectorPlugin.jar"
  
  # cluster scaler plugin jar location (This jar creates new node in the cluster)
  "Cluster-Scaler-Plugin": "./Release/OpenStackClusterScalerPlugin.jar"
  
  # DB executor plugin location jar (This jar configures the database)
  "DB-Executor-Plugin": "./Release/SQLiteDBExecutorPlugin.jar"
  
  # policy info plugin jar location (This jar maintains policies)
  "Policy-Info-Plugin": "./Release/AgingPolicyInfoPlugin.jar"
  
  
CLUSTER SETUP:
For our testing we use a specific set up for our cluster so that we can test various scenarios.
So every cluster will have one master and remaining will be considered slaves. There are a group of security protocols which will be followed for master. For Ingress, the IP Protocol will be TCP and the port range is 5050 and 8080 for being a Mesos Master. An Ingress of TCP and 22 Port Range for successfully SSHing.
While setting up the cluster, we need to make sure that the master and all its slave nodes are in the same network.

The below line sets up the mesos master:
Sudo mesos master --ip=<ip>* --work_dir=/var/lib/mesos

*The IP should be of the current VM which is considered to be the master.

All the slaves machine should set up the mesos slave in the below manner:
mesos slave –master=<master’s-ip>:5050

If we are setting up Hadoop in this cluster, the master should be set up using the below steps:
1.       Hadoop namenode –format

2.       hadoop-daemon.sh start namenode

For the slave machine slaves:
1.       Hadoop-daemon.sh start datanode

Then we can put any files in the hdfs using the below command:
Hadoop dfs –put /<stuff you want to put in hdfs> /<target directory in HDFS>
Any job run with Mesos will be distributed across using Dynamic Partitioning.
More details for setting up HDFS cluster and run Hadoop jobs on mesos cluster is given here:
http://kovit-nisar-it.blogspot.com/


RUNNING THE OVERLORD:
sudo service overlord start

RUNNING THE CLUSTER ELASTICITY AGENT:
sudo service overlord-agent start
