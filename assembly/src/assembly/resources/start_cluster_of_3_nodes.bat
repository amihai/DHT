ECHO Start Node 1

Start ""  java -jar dht-${project.version}.jar --server.port=8001

timeout /t 15 /nobreak > NUL

ECHO Start Node 2

Start ""  java -jar dht-${project.version}.jar --server.port=8002 --node.initializeFromNodes=localhost:8001

timeout /t 30 /nobreak > NUL

ECHO Start Node 3

Start ""  java -jar dht-${project.version}.jar --server.port=8003 --node.initializeFromNodes=localhost:8002
