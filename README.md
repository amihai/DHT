# Distributed Hash Table (DHT)


## Main design decisions 

### Architecture

This Storage solution is very similar with a Java Hash Map in terms of organizing the data into Buckets based on the Key's hash code. +
We can start multiple instances of the application (Nodes) on the same machine or on the network. +
Except the first Node, each Node need to start with a list of master Nodes to synchronize with. A Node is referenced by Socket.+  
In order to achieve Scalability and Performance each Node will store only a subset of Buckets. +
For Availability and Error Recovery, each Bucket is replicated on a configurable number of Nodes (replication factor). + 
In addition to the Buckets, each node is storing also a Buckets to Nodes Mapping. The role of this mapping is described below. +
The storage on each Bucket and the Buckets to Nodes Mapping mapping is periodically updated during the Gossip Sessions (described below). +

So, if we consider a Cluster of 4 Nodes, a Buckets size of 4 and a replication factor of 2 we can have a network like:

![alt text](https://github.com/amihai/DHT/blob/master/docs/asciidoctor/images/Cluster.png "Cluster")

### Buckets balancing

### Node Synchronization

### Node Architecture

### 3rd party dependencies

### RESTful API

### Data Model

### Health Check and Monitoring

## Build / Run instructions

### Build The Project

### Start The Storage in a Cluster of nodes

### Configure The Storage
	
	