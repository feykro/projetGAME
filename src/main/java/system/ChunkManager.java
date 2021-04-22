package system;

import com.rabbitmq.client.*;
import utils.Chunk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import static utils.ExchangeName.*;
import static utils.MessageType.*;
import static utils.QueueName.queueChunkSysBasename;
import static utils.QueueName.queuePortailSysName;

public class ChunkManager {

    private int id;
    private Chunk chunk;

    private Channel chunkPlayers;
    private Channel sys;
    private String playerQueueName;
    private String chunkTopic;

    private Connection connection;

    public ChunkManager(Chunk chk, int ayedi) throws IOException, TimeoutException {
        this.id = ayedi;
        this.chunk = chk;
        playerQueueName = "chunk"+id+"PlayersQueue";
        chunkTopic = "Chunk"+id;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        sys = connection.createChannel();
        chunkPlayers = connection.createChannel();

        sys.exchangeDeclare(ExchangeSysName, BuiltinExchangeType.TOPIC,true);
        chunkPlayers.exchangeDeclare(playerQueueName, BuiltinExchangeType.DIRECT,true);

        initSysRecepteur();
        initPlayerRecepteur();
    }

    private void initSysRecepteur(){
        String queueSysName = queueChunkSysBasename;
        try {
            sys.queueDeclare(queueSysName, true, false, false, null);
            sys.queueBind(queueSysName, ExchangeSysName, chunkTopic);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String finalQueueSysName = queueSysName;
        new Thread() {
            public void run() {
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    String parsedMessage[] = message.split(" ");
                    //APPEL DE FONCTION POUR GÉRER CE QU'IL SE PASSE
                    sysAction(parsedMessage);
                };
                try {
                    sys.basicConsume(finalQueueSysName, true, deliverCallback, consumerTag -> {
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initPlayerRecepteur(){
        try {
            sys.queueDeclare(playerQueueName, true, false, false, null);
            sys.queueBind(playerQueueName, ExchangeSysName, chunkTopic);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String finalQueuePlayerName = playerQueueName;
        new Thread() {
            public void run() {
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    String parsedMessage[] = message.split(" ");
                    //APPEL DE FONCTION POUR GÉRER CE QU'IL SE PASSE
                    playerAction(parsedMessage);
                };
                try {
                    sys.basicConsume(finalQueuePlayerName, true, deliverCallback, consumerTag -> {
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Function called to react to a system query
     */
    private void sysAction(String [] parsedMsg){
        System.out.println("Coucou jean mich");

        if(parsedMsg[0].compareTo(player_Enter) ==0){
            int playerID = Integer.parseInt(parsedMsg[1]);
            int x = Integer.parseInt(parsedMsg[2]);
            int y = Integer.parseInt(parsedMsg[3]);

            System.out.println("Player "+playerID+" is trying to enter\n");
            //gérer l'arrivée d'un nouveau joueur
            return;
        }
        if(parsedMsg[0].compareTo(find_spawn) == 0){
            int playerID = Integer.parseInt(parsedMsg[1]);
            int firstChunkID = Integer.parseInt(parsedMsg[2]);
            if(firstChunkID == this.id){
                //renvoyer erreur
                System.out.println("Pas de place disponible \n");
                return;
            }
            return;
        }
        if(parsedMsg[0].compareTo(free_ID) == 0){
            int playerID = Integer.parseInt(parsedMsg[2]);
            //libérer le joueur
            return;
        }
        //return an error
        System.out.println("Error : unexpected system request received.\n");
    }

    /**
     * Function called to react to a player query
     */
    private void playerAction(String [] parsedMsg){
        if(parsedMsg[0].compareTo(hello_chunk) == 0){
            String playerID = parsedMsg[1];
            String playerPseydo = parsedMsg[2];
            String x = parsedMsg[3];
            String y = parsedMsg[4];
            //spawn player position in the chunk I guess
            return;
        }
        if(parsedMsg[0].compareTo(move) == 0){
            String playerID = parsedMsg[1];
            String direction = parsedMsg[2];
            //update player position in the chunk
            return;
        }
        if(parsedMsg[0].compareTo(say) == 0){
            String playerID = parsedMsg[1];
            String direction = parsedMsg[2];
            String message = parsedMsg[3];
            //say hello to another player
            return;
        }
        if(parsedMsg[0].compareTo(leave) == 0){
            String playerID = parsedMsg[1];
            //make player leave
            return;
        }
        //return an error
        System.out.println("Error : unexpected player action received.\n");
    }

}
