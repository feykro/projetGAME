sudo rabbitmqctl stop_app
sudo rabbitmqctl reset
sudo rabbitmqctl start_app
mvn compile
xterm -T "chunk0" -geometry 100x31+600+0 -e  java -classpath target/classes:/home/etienne/.m2/repository/com/rabbitmq/amqp-client/5.7.1/amqp-client-5.7.1.jar:/home/etienne/.m2/repository/org/slf4j/slf4j-api/1.7.26/slf4j-api-1.7.26.jar:/home/etienne/.m2/repository/org/slf4j/slf4j-simple/1.7.21/slf4j-simple-1.7.21.jar system.startChunkMng 0 &
xterm -T "chunk1" -geometry 100x31+1200+0 -e  java -classpath target/classes:/home/etienne/.m2/repository/com/rabbitmq/amqp-client/5.7.1/amqp-client-5.7.1.jar:/home/etienne/.m2/repository/org/slf4j/slf4j-api/1.7.26/slf4j-api-1.7.26.jar:/home/etienne/.m2/repository/org/slf4j/slf4j-simple/1.7.21/slf4j-simple-1.7.21.jar system.startChunkMng 1 &
xterm -T "chunk2" -geometry 100x31+600+475 -e  java -classpath target/classes:/home/etienne/.m2/repository/com/rabbitmq/amqp-client/5.7.1/amqp-client-5.7.1.jar:/home/etienne/.m2/repository/org/slf4j/slf4j-api/1.7.26/slf4j-api-1.7.26.jar:/home/etienne/.m2/repository/org/slf4j/slf4j-simple/1.7.21/slf4j-simple-1.7.21.jar system.startChunkMng 2 &
xterm -T "chunk3" -geometry 100x31+1200+475 -e  java -classpath target/classes:/home/etienne/.m2/repository/com/rabbitmq/amqp-client/5.7.1/amqp-client-5.7.1.jar:/home/etienne/.m2/repository/org/slf4j/slf4j-api/1.7.26/slf4j-api-1.7.26.jar:/home/etienne/.m2/repository/org/slf4j/slf4j-simple/1.7.21/slf4j-simple-1.7.21.jar system.startChunkMng 3 &
xterm -geometry 100x31+0+0 -e java -classpath /home/etienne/travail/M1/IDS/projetGAME/target/classes:/home/etienne/.m2/repository/com/rabbitmq/amqp-client/5.7.1/amqp-client-5.7.1.jar:/home/etienne/.m2/repository/org/slf4j/slf4j-api/1.7.26/slf4j-api-1.7.26.jar:/home/etienne/.m2/repository/org/slf4j/slf4j-simple/1.7.21/slf4j-simple-1.7.21.jar system.StartPortail

read