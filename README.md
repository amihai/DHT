# Distributed Hash Table (DHT)


## Main design decisions 

### Architecture

This Storage solution is very similar with a Java Hash Map in terms of organizing the data into Buckets based on the Key's hash code. <br />
We can start multiple instances of the application (Nodes) on the same machine or on the network. <br />
Except the first Node, each Node need to start with a list of master Nodes to synchronize with. A Node is referenced by Socket. <br />
In order to achieve Scalability and Performance each Node will store only a subset of Buckets. <br />
For Availability and Error Recovery, each Bucket is replicated on a configurable number of Nodes (replication factor). <br />
In addition to the Buckets, each node is storing also a `Buckets to Nodes Mapping`. The role of this mapping is described below. <br />
The storage on each Bucket and the `Buckets to Nodes Mapping` is periodically updated during the Gossip Sessions (described below). <br />

So, if we consider a Cluster of 4 Nodes, a Buckets size of 4 and a replication factor of 2 we can have a network like: <br />

![alt text](https://github.com/amihai/DHT/blob/master/docs/images/Cluster.png "Cluster")

<br />

### Buckets Balancing
Each Node is running a balancing session on startup and at a fixed rate, configurable in the `application.properties` <br />
The Balancing consists of next actions: <br />
* Copying the Buckets that are not replicated at least with the replication factor.
* Transferring Buckets from Nodes that are busy.
<br />
To know if a Node is busy, the Node Balancing process is calculating the number of Buckets that need to be stored on each Node:<br />
<p style="text-align: center;">Number Of Buckets Per Node = (Total Number of Buckets) * (Replication Factor) / (Number Of Nodes)</p> 
The Bucket Balancing session is using the `Buckets To Nodes Mapping` stored on the current Node. <br /> 

### Node Synchronization
The Node Synchronization consists in:
* Propagating any PUT/DELETE of a Key Value Pair into the whole network.
* Updating the `Buckets To Nodes Mapping` on all the nodes after a modification.
The Node Sync Process is done using Gossip strategy. <br />   
The Gossip Scheduler is configurable in the `application.properties` </br>
On each execution, the Gossip Scheduler select a Random list of Gossip Members from the network.  <br />
The Gossip Scheduler keep a history with last 1000 gossip already sent in order to avoid infinite loops of gossips. <br />
Any modification on the Buckets from the current Node or on the `Buckets To Nodes Mapping` is converted into a Gossip and send to the network </br> 

### Node Architecture

The current Node is forwarding any PUT/DELETE/GET of a Key that is not part of a owned Bucket.  </br>
A Remote Node that is owner of the Bucket is selected from the Buckets To Nodes mapping. </br>
If the value is stored on local Bucket we register a Gossip and asynchronously the Gossip Scheduler will send it to the network. <br /> 

![alt text](https://github.com/amihai/DHT/blob/master/docs/images/Put_Key_Value.png "Put Key Value Sequence Diagram") 

### Storage Size
Each Node store in-memory the size of each Bucket. </br>
The size of each bucket is updated by a BucketsSizeCache Scheduler (configurable in `application.properties`). <br />
The update of the buckets size is done in two ways:
* For the Buckets stored on local disk we just sum the keys stored.
* For the Buckets stored remote we compute a Gossip Members list and we check the size of each bucket remote. If the value returned by the remote node is old (based on lastUpdate field) we keep the value that we already have. 

### 3rd party dependencies
* Cucumber for BDD. 
* Swagger 2
* Spring Core for IoC.
* Spring REST.
* Spring Boot.
* Maven Assembly Plugin.
* SLF4J

### RESTful API
The Node expose a REST API that can be accessed on http://localhost:8001/swagger-ui.html (replace 8001 with node's port)<br />
The REST API Category that implement the Key Value CRUD operations is the last one (key-value-store-service : Key Value Store Service).<br />
The rest of the endpoints are use internally by the Nodes for Synchronization.<br />
  
![alt text](https://github.com/amihai/DHT/blob/master/docs/images/Swagger.png "Swagger")


### Data Model

![alt text](https://github.com/amihai/DHT/blob/master/docs/images/Data_Model.png "Data Model")

## Build / Run instructions

### Build The Project

```bash
mvn clean install
```

Assembly plugin will create the ZIP artefact that contains the fat jar, the config files and a bat to start a cluster of three nodes (`start_cluster_of_3_nodes.bat`): <br /> 

```bash
assembly\target\dht-assembly-0.0.1-SNAPSHOT-DHT-Assembly.zip
```

### Start The Storage in a Cluster of Nodes

To start the Storage you will need to unzip the artefact `assembly\target\dht-assembly-0.0.1-SNAPSHOT-DHT-Assembly.zip` <br />

You can start directly a Cluster of three nodes as sample using: <br />
```bash
start_cluster_of_3_nodes.bat
```

The Nodes started by this script can be accessed on URLS:
* http://localhost:8001/swagger-ui.html
* http://localhost:8002/swagger-ui.html
* http://localhost:8003/swagger-ui.html

You can check that all the Nodes started properly if you use the Health Service. You should see that all the nodes are BALANCED and store a similar number of nodes. <br />

![alt text](https://github.com/amihai/DHT/blob/master/docs/images/HealthCheck.png "Health Check")

You can start to use now the "key-value-store-service : Key Value Store Service"  from the bottom that expose the Storage API. <br />

### Check Logs

Each node is storing the logs into `logs/node_PORT.out`. For example we have after we started the above cluster:
```bash
logs/node_8001.out
logs/node_8002.out
logs/node_8003.out
```

### Start The Nodes One by One

You can start a single Node running the command:
```bash
java -jar dht-0.0.1-SNAPSHOT.jar --server.port=8004 --node.initializeFromNodes=localhost:8003
```
In the above command:
* `server.port` is the port of the current Node
* `node.initializeFromNodes` is a list with master nodes that are already started. The current node will import the bucketsToNodes mapping from a master node.


### Configure The Storage

You can configure the Node by modifying the `application.properties` file from the zip or by overriding properties when you start the node (like the above example). <br />

Except the properties already describe you may need to modify:
* `logging.file` - for the location where log4j is storing the logs
* `node.noOfBuckets` - This should be the same for all the Nodes so cannot be modified after you started the first Node.
* `bucketsToNodes.balancing.replicationFactor` - The number of copies of the same Bucket into the network.
* `bucketsToNodes.storeDirectory` is the location where we will store the bucketsToNodes mapping. Need to be unique if you start multiple nodes on the same machine.
* `keyValue.storeDirectory` similar with `bucketsToNodes.storeDirectory` but for key value pairs

If you want to fresh start the application you should remove the storage directories. In the default config they are located into the `target` directory.
 
### Tuning

#### Capacity
Each Node has a limited storing capacity (FileSystem). In order to increase the capacity per Node you can:
* Start more nodes - but this will lower the Consistency. 
* Lower the replication factor - but this will lower the Fault Tolerance.  

#### Performance
To increase the Performance you can:
* Store on the local Node more Buckets - this will lower the Capacity
* Lower the Gossip rate - this will lower the Consistency.
* Lower the replication factor - but this will lower the Fault Tolerance. 

#### Consistency
To increase the Consistency you can:  
* Run Gossip more often - this will lower the Performance
* Lower the number of Nodes - this will decrease the Capacity  

#### Fault Tolerance
To improve the Fault Tolerance you can:
* Increase the replication factor - this will lower the Performance and the Capacity
* Increase the number of Nodes - this will lower the Performance and Consistency	
	