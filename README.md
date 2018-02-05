# Distributed Hash Table (DHT)


## Main design decisions 

### Architecture

This Storage solution is very similar with a Java Hash Map in terms of organizing the data into Buckets based on the Key's hash code. <br />
We can start multiple instances of the application (Nodes) on the same machine or on the network. <br />
Except the first Node, each Node need to start with a list of master Nodes to synchronize with. A Node is referenced by Socket. <br />
In order to achieve Scalability and Performance each Node will store only a subset of Buckets. <br />
For Availability and Error Recovery, each Bucket is replicated on a configurable number of Nodes (replication factor). <br />
In addition to the Buckets, each node is storing also a Buckets to Nodes Mapping. The role of this mapping is described below. <br />
The storage on each Bucket and the Buckets to Nodes Mapping mapping is periodically updated during the Gossip Sessions (described below). <br />

So, if we consider a Cluster of 4 Nodes, a Buckets size of 4 and a replication factor of 2 we can have a network like: <br />

![alt text](https://github.com/amihai/DHT/blob/master/docs/images/Cluster.png "Cluster")

<br />

### Buckets Balancing
Each Node is running a balancing session on startup and at a fixed rate, configurable in the application.properties <br />
The Balancing consists of next actions: <br />
* Copying the Buckets that are not replicated at least with the replication factor.
* Transferring Buckets from Nodes that are busy.
<br />
To know if a Node is busy, the Node Balancing process is calculating the number of Buckets that need to be stored on each Node:<br />
<p style="text-align: center;">Number Of Buckets Per Node = (Total Number of Buckets) * (Replication Factor) / (Number Of Nodes)</p> 
The Bucket Balancing session is using the Buckets To Nodes mapping stored on the current node. <br /> 

### Node Synchronization
The Node Synchronization consists in:
* Propagating any ADD/REMOVE of a Key Value Pair into the whole network.
* Updating the Buckets To Nodes Mapping on all the nodes after a modification.
The Node Sync Process is done using Gossip strategy. <br />   
The Gossip Scheduler is configurable in the application.properties </br>
On each execution, the Gossip Scheduler select a Random list of Gossip Members from the network. </br>
The Gossip Scheduler keep a history with last 1000 gossip already sent in order to avoid infinite loops of gossips. <br />
Any modification on the Buckets from the current Node or on the Buckets To Nodes mapping is converted into a Gossip and send to the network </br> 
This strategy allow us to propagate modifications into the network very efficient. We gossip with only half of the nodes and is working event if some of them are down. <br />  

### Node Architecture

### Node Size

### 3rd party dependencies

### RESTful API

### Data Model

### Health Check and Monitoring

## Build / Run instructions

### Build The Project

### Start The Storage in a Cluster of nodes

### Configure The Storage
	
	