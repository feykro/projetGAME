sudo rabbitmqctl stop_app
sudo rabbitmqctl reset
sudo rabbitmqctl start_app
mvn compile 2> /dev/null
xterm -T "chunk0" -geometry 100x31+600+0 -e  mvn org.codehaus.mojo:exec-maven-plugin:1.5.0:java -Dexec.mainClass="system.StartChunkMng" -Dexec.args="0" &
xterm -T "chunk1" -geometry 100x31+1200+0 -e  mvn org.codehaus.mojo:exec-maven-plugin:1.5.0:java -Dexec.mainClass="system.StartChunkMng" -Dexec.args="1" &
xterm -T "chunk2" -geometry 100x31+600+475 -e  mvn org.codehaus.mojo:exec-maven-plugin:1.5.0:java -Dexec.mainClass="system.StartChunkMng" -Dexec.args="2" &
xterm -T "chunk3" -geometry 100x31+1200+475 -e  mvn org.codehaus.mojo:exec-maven-plugin:1.5.0:java -Dexec.mainClass="system.StartChunkMng" -Dexec.args="3" &
xterm -geometry 100x31+0+0 -e mvn org.codehaus.mojo:exec-maven-plugin:1.5.0:java -Dexec.mainClass="system.StartPortail" &
echo -e "Ctrl^C to stop all system \n"
read