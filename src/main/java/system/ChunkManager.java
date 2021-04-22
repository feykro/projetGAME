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
    private int searchCounter = 0;

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
        chunkPlayers.exchangeDeclare(ExchangeChunkPlayerName, BuiltinExchangeType.TOPIC,true);

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
            chunkPlayers.queueDeclare(playerQueueName, true, false, false, null);
            chunkPlayers.queueBind(playerQueueName, ExchangeChunkPlayerName, chunkTopic);
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
                    chunkPlayers.basicConsume(finalQueuePlayerName, true, deliverCallback, consumerTag -> {
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

        if(parsedMsg[0].equals(find_spawn)){
            int playerID = Integer.parseInt(parsedMsg[1]);
            int nbChunkCounter = Integer.parseInt(parsedMsg[2]);

            if(nbChunkCounter >= 4) {
                //TODO : renvoyer au portail que c mor
                System.out.println("Pas de place disponible \n");
                return;
            }

            nbChunkCounter++;
            //rechercher une place
            int[] coor = playerSpawnFinder(playerID);
            if(coor[0] == -1){
                //TODO
                //Faut aller chercher une place ailleurs
                return;
            }else{
                //TODO : renvoyer au portail la position
                return;
            }
        }

        if(parsedMsg[0].compareTo(player_Enter) ==0){
            int playerID = Integer.parseInt(parsedMsg[1]);
            int x = Integer.parseInt(parsedMsg[2]);
            int y = Integer.parseInt(parsedMsg[3]);

            System.out.println("Player "+playerID+" is trying to enter\n");
            //gérer l'arrivée d'un nouveau joueur
            boolean canEnter = playerEnterTester(playerID, x, y);
            //todo: renvoyer ça au chunk dont le joueur est originaire
            return;
        }

        if(parsedMsg[0].compareTo(free_ID) == 0){
            int playerID = Integer.parseInt(parsedMsg[2]);
            //todo: libérer le joueur auprès du portail
            chunk.freeUserCase(playerID);
            return;
        }
        //return an error
        System.out.println("Error : unexpected system request received.\n");
    }

    /**
     * Function called to react to a player query
     */
    private void playerAction(String [] parsedMsg){
        if(parsedMsg[0].equals(hello_chunk)){
            int playerID = Integer.parseInt(parsedMsg[1]);
            String playerPseudo = parsedMsg[2];
            int x = Integer.parseInt(parsedMsg[3]);
            int y = Integer.parseInt(parsedMsg[4]);
            //spawn player position in the chunk I guess
            chunk.occupeCase(x, y,playerID, playerPseudo);
            return;
        }

        if(parsedMsg[0].compareTo(move) == 0){
            int playerID = Integer.parseInt(parsedMsg[1]);
            String direction = parsedMsg[2];
            //vérifier si la case résultante est libre
            boolean isValid = chunk.isValidMovement(playerID, direction);
            //todo: vérifier si on sort du chunk, si oui contacter le chunk approprié
            //todo: occuper la case résultante et libérer l'ancienne
            //todo: update player position for everyone

            return;
        }

        if(parsedMsg[0].compareTo(say) == 0){
            int playerID = Integer.parseInt(parsedMsg[1]);
            String direction = parsedMsg[2];
            String message = parsedMsg[3];
            //todo: check if there's a player in pointed direction
            int[] coordTalk = chunk.isValidTalk(playerID, direction);
            //todo: send message to player
            return;
        }
        if(parsedMsg[0].compareTo(leave) == 0){
            String playerID = parsedMsg[1];
            //je comprends pas a quoi sert free id dans sysAction
            return;
        }
        //return an error
        System.out.println("Error : unexpected player action received.\n");
    }

    /**
     * Trouver une place pour le joueur dans le chunk et renvoie la coordonnée
     * Sinon, renvoie -1, -1
     */
    private int[] playerSpawnFinder(int playerID){
        int[] pos = chunk.findFreeCase();
        if(pos[0] != -1){
            //On bloque la position du joueur dans la grille
            chunk.reserveCase(pos[0], pos[1], playerID);
        }
        return pos;
    }

    private boolean playerEnterTester(int playerID, int x, int y){
        //On occupe la case avec le joueur
        boolean test = !chunk.getCase(x, y).isOccupied();
        //todo : notify area that new player is arriving
        if(test){
            //on réserve l'arrivée
            chunk.reserveCase(x, y,playerID);
        }
        return test;
    }
}
