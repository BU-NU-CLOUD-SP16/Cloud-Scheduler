
Cloud Scheduler for Next Generation Data Centers
================================================


Vision and Goals Of The Project:
-------------------------------------
In the next generation data centers, the nodes should support multiple workloads in addition to the traditional web services which run today. These workloads have different resource requirements which must be addressed by the data centers. This can be achieved by introducing a new scheduling layer, which can effectively schedule different workloads by scaling the clusters dynamically.

A core part of the Massachusetts Open Cloud is a Hardware as a Service project to create a new fundamental layer in cloud datacenters. HaaS supports allocating a set of computer nodes (bare hardware) with network isolation between those nodes and the rest of the cloud. They can scale their deployments by allocating and freeing hardware nodes, without having to change any physical setup of the datacenter. HaaS interacts with Bare Metal Imaging (BMI) services, to deploy images on physical nodes. 

To scale the nodes based on resource requirement, we need an efficient distributed scheduling framework. We achieve this by introducing Apache Mesos on top of BMI to schedule multiple workloads. Apache Mesos is a stable scheduler of frameworks, which can efficiently allocate resources for different frameworks.

###Goals:
* Verify compatibility of different workloads/frameworks with Apache Mesos.
* Develop a simple scheduler to test the BMI-HaaS architecture.
* Integration of Apache Mesos on top of the BMI-HaaS architecture.
* Verify compatibility of different workloads/frameworks on the entire architecture.

###Users/Personas Of The Project:
The workloads will simulate the end users. The scheduling architecture will be used by application frameworks of different workloads to reserve resources and process their tasks.

2.   Scope and Features Of The Project:

Creation of a Bare Metal Image with Apache Mesos.
Integration of Apache Mesos with the BMI-HaaS architecture.
Testing of different workloads on the scheduling architecture.
o   	The frameworks/workloads we will be testing include at least two of the following: HPC (SLURM), Machine Learning (Spark), MapReduce (Hadoop).
o   	Scheduling policies for the different frameworks will also be tested.
Analysis of resource allocation for different workloads.
Deployment of a working model of the scheduling architecture on the MOC.
  
3.   Solution Concept

High Level Architecture:


4. Acceptance criteria
Development of a simple scheduler to test the BMI-HaaS architecture.
Working BMI-HaaS architecture along with the simple scheduler.
Integration of Apache Mesos into the BMI-HaaS architecture.

5.  Release Planning:
 
Release 1
Install and configure Mesos
Run test frameworks on Mesos
Explore allocation policy plugins in relation to test frameworks.


Release 2
Run Spark, SLURM and Hadoop frameworks.
Explore allocation policy plugins in relation to Spark, SLURM and Hadoop (as time permits).
Document the allocation policies
Run on a small cluster (stretch goal)

Release 3
Develop a simple scheduler which schedule batch processes with exponential ageing.
Test on BMI-HaaS architecture (Provided BMI-HaaS is ready)

Release 4
Integrate Mesos on BMI-HaaS
Test with a basic Mesos framework

Release 5
Test with different frameworks (Spark, Hadoop, SLURM)
 
Release 6
 Analyze scheduling performance for different workloads.
