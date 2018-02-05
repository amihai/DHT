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
* Propagating any PUT/DELETE of a Key Value Pair into the whole network.
* Updating the Buckets To Nodes Mapping on all the nodes after a modification.
The Node Sync Process is done using Gossip strategy. <br />   
The Gossip Scheduler is configurable in the application.properties </br>
On each execution, the Gossip Scheduler select a Random list of Gossip Members from the network.  <br />
The Gossip Scheduler keep a history with last 1000 gossip already sent in order to avoid infinite loops of gossips. <br />
Any modification on the Buckets from the current Node or on the Buckets To Nodes mapping is converted into a Gossip and send to the network </br> 

### Node Architecture

The current Node is forwarding any PUT/DELETE/GET of a Key that is not part of Bucket owned.  </br>
The Remote Node that is owner of the Bucket is selected from the Buckets To Nodes mapping. </br>
If the value is stored on local Bucket we register a Gossip and asynchronously the Gossip Scheduler will send it to the network. <br /> 

![alt text](https://github.com/amihai/DHT/blob/master/docs/images/Put_Key_Value.png "Put Key Value Sequence Diagram") 

### Node Size
Each Node store in-memory the size of each Bucket. </br>
The size of each bucket is updated by a BucketsSizeCache Scheduler (configurable in application.properties). <br />
The update of the buckets size is done in two ways:
* For the Buckets stored on local disk we just some the keys stored.
* For the Buckets stored remote we compute a Gossip Members list and we check the size of each bucket remote. If the value returned by the remote node is old we keep the value that we already have. 

### 3rd party dependencies
* Cucumber for BDD. 
* Swagger 2
* Spring Core for IoC.
* Spring REST.
* Spring Boot.
* Maven Assembly Plugin.
* SLF4J

### RESTful API
The Node expose a REST API that can be accessed on http://localhost:8001/swagger-ui.html (replace 8001 with node port)<br />
The REST API that implement the Key Value CRUD operations is the last one (key-value-store-service : Key Value Store Service).<br />
The rest of the endpoints are use internally by the Nodes for Synchronization.<br />
  
![alt text](https://github.com/amihai/DHT/blob/master/docs/images/Swagger.png "Swagger")


### Data Model

![alt text](https://github.com/amihai/DHT/blob/master/docs/images/Data_Model.png "Data Model")

## Build / Run instructions

Project 

### Build The Project

```bash
mvn clean install
```

Assembly plugin will create the ZIP artefact that contains the fat jar, the config files and a bat to start a cluster of three nodes: <br /> 

```bash
assembly\target\dht-assembly-0.0.1-SNAPSHOT-DHT-Assembly.zip
```

### Start The Storage in a Cluster of nodes

### Configure The Storage
	
	