ECHO Start Node 1

Start ""  java -jar dht-${project.version}.jar --server.port=8001 --bucketsToNodes.storeDirectory=target/node1/bucketsToNodes --keyValue.storeDirectory=target/node1/keyValue

timeout /t 15 /nobreak > NUL

ECHO Start Node 2

Start ""  java -jar dht-${project.version}.jar --server.port=8002 --bucketsToNodes.storeDirectory=target/node2/bucketsToNodes --keyValue.storeDirectory=target/node2/keyValue --node.initializeFromNodes=localhost:8001

timeout /t 30 /nobreak > NUL

ECHO Start Node 3

Start ""  java -jar dht-${project.version}.jar --server.port=8003 --bucketsToNodes.storeDirectory=target/node3/bucketsToNodes --keyValue.storeDirectory=target/node3/keyValue --node.initializeFromNodes=localhost:8002
