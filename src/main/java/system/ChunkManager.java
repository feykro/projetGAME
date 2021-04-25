package system;

import com.rabbitmq.client.*;
import utils.Chunk;
import utils.Direction;
import utils.MessageType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import static utils.Direction.getDirection;
import static utils.ExchangeName.*;
import static utils.MessageType.*;
import static utils.QueueName.queueChunkSysBasename;

public class ChunkManager {

    private int id;
    private Chunk chunk;
    private int searchCounter = 0;
    private HashMap<String, String> pseudoIDmap;

    private Channel chunkPlayers;
    private Channel sys;
    private String playerQueueName;
    private String chunkTopic;

    private Connection connection;

    public ChunkManager(Chunk chk, int ayedi) throws IOException, TimeoutException {
        this.id = ayedi;
        this.chunk = chk;
        //HashMap - clef: ID | value: pseudo
        this.pseudoIDmap = new HashMap<>();
        playerQueueName = "chunk" + id + "PlayersQueue";
        chunkTopic = "Chunk" + id;

        System.out.println("mon chunkTopic : " + chunkTopic);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        sys = connection.createChannel();
        chunkPlayers = connection.createChannel();

        sys.exchangeDeclare(ExchangeSysName, BuiltinExchangeType.TOPIC, true);
        chunkPlayers.exchangeDeclare(ExchangeChunkPlayerName, BuiltinExchangeType.TOPIC, true);

        initSysRecepteur();
        initPlayerRecepteur();
    }

    private void initSysRecepteur() {
        String queueSysName = queueChunkSysBasename + id;
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
                    System.out.println("je recois du systeme : " + message);
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

    private void initPlayerRecepteur() {
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
                    System.out.println("je recois des joueurs : " + message);
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
    private void sysAction(String[] parsedMsg) {
        System.out.println("Coucou jean mich");

        if (parsedMsg[0].equals(find_spawn)) {
            int playerID = Integer.parseInt(parsedMsg[1]);
            int nbChunkCounter = Integer.parseInt(parsedMsg[2]);



            //rechercher une place
            int[] coor = playerSpawnFinder(playerID);
            if (coor[0] == -1) {
                //Faut aller chercher une place ailleurs
                if (nbChunkCounter >= 3) {
                    System.out.println("Pas de place disponible \n");
                    try {
                        String message = fail + " connect";
                        chunkPlayers.basicPublish(ExchangeChunkPlayerName, Integer.toString(playerID), null, message.getBytes("UTF-8"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                else{
                    nbChunkCounter++;
                    String message = find_spawn + " " + playerID + " " + nbChunkCounter;
                    System.out.println("Sending message : "+message);
                    try {
                        sys.basicPublish(ExchangeSysName, "Chunk"+((id+1)%4), null, message.getBytes("UTF-8"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return;
            } else {
                System.out.println("On renvoit au joueur la coordonnée " + coor[0] + " : " + coor[1]);
                sendHelloPlayer(playerID, coor);
                return;
            }
        }

        if (parsedMsg[0].compareTo(player_Enter) == 0) {
            int chunkID = Integer.parseInt(parsedMsg[1]);
            int playerID = Integer.parseInt(parsedMsg[2]);
            int coor[] = {Integer.parseInt(parsedMsg[3]), Integer.parseInt(parsedMsg[4])};

            System.out.println("Player " + playerID + " is trying to enter\n");
            //gérer l'arrivée d'un nouveau joueur
            boolean canEnter = playerEnterTester(playerID, coor[0], coor[1]);
            if (canEnter) {
                sendHelloPlayer(playerID, coor);
                sendCanEnter(chunkID, playerID, "1");
            } else {
                sendCanEnter(chunkID, playerID, "0");
            }
            return;
        }

        if (parsedMsg[0].equals(can_enter)) {
            System.out.println(parsedMsg[0] + " " + parsedMsg[1] + " " + parsedMsg[2]);
            String playerID = parsedMsg[1];
            if (!pseudoIDmap.containsKey(playerID)) {
                System.out.println("Player not present : can't withdraw him.");
                return;
            }
            if (parsedMsg[2].equals("0")) {
                System.out.println("Fail to transfert " + playerID);
                sendFailToMove(Integer.parseInt(playerID));
                return;
            }
            pseudoIDmap.remove(playerID);
            sendLeave(playerID);
            if (!chunk.freeUserCase(Integer.parseInt(playerID))) {
                System.out.println("fail to purge " + playerID);
            }
            return;
        }

        //return an error
        System.out.println("Error : unexpected system request received.\n");
    }

    /**
     * Function called to react to a player query
     */
    private void playerAction(String[] parsedMsg) {

        if(parsedMsg[0].equals(turn)){
            if(!chunk.updateDirection(Integer.parseInt(parsedMsg[1]),getDirection(parsedMsg[2]))){
                System.out.println("fail to turn "+parsedMsg[1]);
            }
            return;
        }
        if (parsedMsg[0].equals(hello_chunk)) {
            String playerID = parsedMsg[1];
            String playerPseudo = parsedMsg[2];
            Direction playerDirection = getDirection(parsedMsg[5]);
            int x = Integer.parseInt(parsedMsg[3]);
            int y = Integer.parseInt(parsedMsg[4]);
            //spawn player position in the chunk I guess
            chunk.occupeCase(x, y, Integer.parseInt(playerID), playerPseudo,playerDirection);
            this.pseudoIDmap.put(playerID, playerPseudo);
            if (sendInfoChunk(playerID)) {
                //On a envoyé les informations au joueur, il faut maintenant tenir informé les joueurs de notre
                //chunk
                sendUpdate(playerID, playerPseudo, x, y,parsedMsg[5]);
            }
            return;
        }



        if (parsedMsg[0].equals(move)) {
            int playerID = Integer.parseInt(parsedMsg[1]);
            String direction = parsedMsg[2];
            //vérifier si la case résultante est libre
            //todo: vérifier si on sort du chunk, si oui contacter le chunk approprié
            int[] coor = chunk.getNewCoor(playerID, direction);
            if (coor[0] >= 5 || coor[0] < 0 || coor[1] >= 5 || coor[1] < 0) {
                sendTransfertPlayer(playerID, coor);
                return;
            }

            //todo: occuper la case résultante et libérer l'ancienne
            if (!chunk.isValidMovement(playerID, direction)) {
                System.out.println(playerID + " cant move toward " + direction);
                sendFailToMove(playerID);
                return;
            }
            int newCoor[] = chunk.moveTo(playerID, direction);
            sendUpdate(parsedMsg[1], pseudoIDmap.get(parsedMsg[1]), newCoor[0], newCoor[1],direction);
            chunk.showChunk();
            //todo: update player position for everyone

            return;
        }

        if (parsedMsg[0].equals(say)) {
            int playerID = Integer.parseInt(parsedMsg[1]);
            String direction = parsedMsg[2];
            String message = parsedMsg[3];
            //todo: check if there's a player in pointed direction
            int[] coordTalk = chunk.isValidTalk(playerID, direction);
            //todo: send message to player
            return;
        }

        if (parsedMsg[0].equals(leave)) {
            String playerID = parsedMsg[1];
            if (!pseudoIDmap.containsKey(playerID)) {
                System.out.println("Player not present : can't withdraw him.");
                return;
            }
            pseudoIDmap.remove(playerID);
            sendLeave(playerID);
            sendFreeID(playerID);
            if (!chunk.freeUserCase(Integer.parseInt(playerID))) {
                System.out.println("fail to purge " + playerID);
            }
            return;
        }
        //return an error
        System.out.println("Error : unexpected player action received :" + parsedMsg[0]);
    }

    /**
     * Trouver une place pour le joueur dans le chunk et renvoie la coordonnée
     * Sinon, renvoie -1, -1
     */
    private int[] playerSpawnFinder(int playerID) {
        int[] pos = chunk.findFreeCase();
        if (pos[0] != -1) {
            //On bloque la position du joueur dans la grille
            chunk.reserveCase(pos[0], pos[1], playerID);
        }
        return pos;
    }

    private boolean playerEnterTester(int playerID, int x, int y) {
        //On occupe la case avec le joueur
        boolean test = !chunk.getCase(x, y).isOccupied();
        if (test) {
            //on réserve l'arrivée
            chunk.reserveCase(x, y, playerID);
        }
        return test;
    }

    private boolean sendInfoChunk(String playerID) {
        String msg = chunk.getInfochunk(Integer.parseInt(playerID));
        System.out.println("Send msg : \n" + msg);
        try {
            chunkPlayers.basicPublish(ExchangeChunkPlayerName, playerID, null, msg.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void sendUpdate(String playerID, String playerPseudo, int playerX, int playerY,String direction) {
        String msg = update + " " + playerID + " " + playerPseudo + " " + Integer.toString(playerX) + " " + Integer.toString(playerY) + " " +direction;

        try {
            chunkPlayers.basicPublish(ExchangeChunkPlayerName, "Chunk" + id + ".Players", null, msg.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Sent update msg: \n" + msg);
    }

    private void sendLeave(String playerID) {
        String msg = leaving_player + " " + playerID;
        try {
            chunkPlayers.basicPublish(ExchangeChunkPlayerName, "Chunk" + id + ".Players", null, msg.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Sent player leaving msg: \n" + msg);
    }

    private void sendFreeID(String playerID) {
        String msgPortal = MessageType.free_ID + " " + playerID;
        try {
            sys.basicPublish(ExchangeSysName, "Portail", null, msgPortal.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }


    private void sendHelloPlayer(int playerID, int[] coor) {
        String msg = hello_player + " " + Integer.toString(this.id) + " " + Integer.toString(coor[0]) + " " + Integer.toString(coor[1]);
        try {
            chunkPlayers.basicPublish(ExchangeChunkPlayerName, Integer.toString(playerID), null, msg.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFailToMove(int playerID) {
        String msg = fail + " move";
        try {
            chunkPlayers.basicPublish(ExchangeChunkPlayerName, Integer.toString(playerID), null, msg.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendTransfertPlayer(int playerID, int[] coor) {
        int id_transfert_chunk = determineTransfertChunkID(coor);

        coor[0] = (coor[0] + chunk.getTaille()) % chunk.getTaille();
        coor[1] = (coor[1] + chunk.getTaille()) % chunk.getTaille();
        String msg = player_Enter + " " + id + " " + playerID + " " + coor[0] + " " + coor[1];
        try {
            sys.basicPublish(ExchangeSysName, "Chunk" + id_transfert_chunk, null, msg.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void sendCanEnter(int chunkID, int playerID, String result) {
        String msg = can_enter + " " + playerID + " " + result;
        try {
            sys.basicPublish(ExchangeSysName, "Chunk" + chunkID, null, msg.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private int determineTransfertChunkID(int[] coor) {
        if (coor[0] >= 5 || coor[0] < 0) {
            if (this.id % 2 == 0) {
                return this.id + 1;
            } else {
                return this.id - 1;
            }
        }
        if (coor[1] >= 5 || coor[1] < 0) {
            if (this.id < 2) {
                return this.id + 2;
            } else {
                return this.id - 2;
            }
        }
        assert (false);
        return -1;
    }
}
