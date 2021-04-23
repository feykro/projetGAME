package client;

import UI.GraphiqueChunk;
import com.rabbitmq.client.*;
import utils.Chunk;
import utils.Direction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static utils.ExchangeName.*;
import static utils.MessageType.*;

/**
 * Manage Player distributes actions
 */
public class Player {

    private Chunk plateau;
    private GraphiqueChunk ui;

    private Channel portailRequest;
    private Channel id_response;
    private Channel chunk;

    private int currentChunkNumber;

    private String finalQueueIDrespondName = null;
    private String finalPersonalQueueName = null;
    private String finalQueuequeueChunkName = null;

    private String pseudo;
    private int id;

    private Connection connection;

    private ArrayList<String> listID;

    public Player(String pseudo) throws IOException, TimeoutException {

        this.pseudo = pseudo;

        this.plateau = new Chunk();

        this.ui = new GraphiqueChunk(plateau.getTaille(),this);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();

        portailRequest = connection.createChannel();
        chunk = connection.createChannel();
        id_response = connection.createChannel();

        portailRequest.exchangeDeclare(ExchangePortailRequestName, BuiltinExchangeType.TOPIC, true);
        id_response.exchangeDeclare(ExchangeIDRespondName, BuiltinExchangeType.DIRECT, true);
        chunk.exchangeDeclare(ExchangeChunkPlayerName, BuiltinExchangeType.TOPIC, true);
        initReciveID();
        idRequest();
    }

    /**
     * Initialise the queue to take respond of the request id from portal
     */
    public void initReciveID() {
        String queueSysName = null;
        try {
            queueSysName = id_response.queueDeclare().getQueue();
            id_response.queueBind(queueSysName, ExchangeIDRespondName, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        finalQueueIDrespondName = queueSysName;
        new Thread() {
            public void run() {
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String id = new String(delivery.getBody(), "UTF-8");
                    if (checkID(id)) {
                        unbindQueue(finalQueueIDrespondName, ExchangeIDRespondName, "");
                        initPersonalQueueReceiver();
                        spawnRequest();
                    }
                };
                try {
                    id_response.basicConsume(finalQueueIDrespondName, true, deliverCallback, consumerTag -> {
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Initialise personal queue to sub on his id to take specific message from chunk/portal
     */
    public void initPersonalQueueReceiver() {
        String queueChunkName = null;
        try {
            String key = String.valueOf(getID());

            queueChunkName = chunk.queueDeclare().getQueue();
            chunk.queueBind(queueChunkName, ExchangeChunkPlayerName, key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finalPersonalQueueName = queueChunkName;
        new Thread() {
            public void run() {
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    manageMessage(message);
                };
                try {
                    chunk.basicConsume(finalPersonalQueueName, true, deliverCallback, consumerTag -> {
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Initialise chunk queue  to have message of the chunk where it is currently
     */
    public void initChunkQueueReceiver(int chunkNumber) {
        //unbind old chunk queue
        if(finalQueuequeueChunkName != null){
            unbindQueue(finalQueuequeueChunkName, ExchangeChunkPlayerName, "Chunk" + currentChunkNumber+"Players");
        }

        System.out.println("mon chunk est " + chunkNumber);
        currentChunkNumber = chunkNumber;
        String queueChunkName = null;
        try {
            String key = "Chunk" + chunkNumber + "Players";
            queueChunkName = chunk.queueDeclare().getQueue();
            //chunk.queueDeclare(queueChunkName, true, false, false, null);
            chunk.queueBind(queueChunkName, ExchangeChunkPlayerName, key);
            System.out.println("Je suis abonné au chunk "+chunkNumber+" avec la clef "+key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finalQueuequeueChunkName = queueChunkName;
        new Thread() {
            public void run() {
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    manageMessage(message);
                };
                try {
                    chunk.basicConsume(finalQueuequeueChunkName, true, deliverCallback, consumerTag -> {
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Initialise the queue to take respond of the request spawn from portal
     */
    public void spawnRequest() {
        try {
            portailRequest.basicPublish(ExchangePortailRequestName, "SPAWN", null, Integer.toString(id).getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * First Request to get an game id to communicate with chunk/portal
     */
    public void idRequest() {
        try {
            portailRequest.basicPublish(ExchangePortailRequestName, "ID", null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Request to chunk when Player enter in new chunk to signal it
     *
     * @param x coordonate to initial position in the chunk
     * @param y coordonate to initial position in the chunk
     */
    public void requestHelloChunk(int x, int y) {
        String message = hello_chunk + " " + String.valueOf(getID()) + " " + getPseudo() + " " + x + " " + y;
        requestToChunk(message);
    }

    /**
     * Request to chunk when Player would move in the chunk
     *
     * @param direction the direction of the movement
     */
    public void requestMove(Direction direction) {
        System.out.println("Je demande a bouger vers "+direction);
        String message = move + " " + String.valueOf(getID()) + " " + direction;
        requestToChunk(message);
    }

    /**
     * Request to chunk when player would say something to neighbour
     *
     * @param direction direction of the message
     * @param msg
     */
    public void requestSay(Direction direction, String msg) {
        String message = say + " " + String.valueOf(getID()) + " " + direction + " " + msg;
        requestToChunk(message);
    }

    /**
     * Request to chunk when Player would leave the game
     */
    public void requestLeaveGame() {
        unbindQueue(finalPersonalQueueName, ExchangeChunkPlayerName, Integer.toString(getID()));
        unbindQueue(finalQueuequeueChunkName, ExchangeChunkPlayerName, "Chunk"+currentChunkNumber+"Players");

        String message = leave + " " + String.valueOf(getID());
        requestToChunk(message);
    }

    /**
     * general methode to send message to current chunk
     *
     * @param message
     */
    private void requestToChunk(String message) {
        try {
            chunk.basicPublish(ExchangeChunkPlayerName, "Chunk" + currentChunkNumber, null, message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * unbind queue in parameter
     *
     * @param queueName queue name
     * @param chanel    exchange who have the queue
     * @param key       key of the queue
     * @return true if succes
     */
    private boolean unbindQueue(String queueName, String chanel, String key) {
        try {
            id_response.queueUnbind(queueName, chanel, key);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * check if id is a real id (>-1 <16) and add save it if it s real
     *
     * @param id
     * @return true if id is real
     */
    private boolean checkID(String id) {
        System.out.println("mon id est " + id);
        int tmp = Integer.parseInt(id);
        if (tmp >= 0) {
            setID(tmp);
            return true;
        }
        return false;
    }

    /**
     * Manage message from chunk and update Ui
     *
     * @param message
     */
    private void manageMessage(String message) {
        System.out.println("Received message : \n"+message);
        String parser[] = message.split(" ");
        assert (parser.length > 0);
        String type = parser[0];
        if (type.equals(info_chunk)) {
            assert (parser.length > 5);
            int nb_update = Integer.parseInt(parser[1]);
            for (int i = 2; (i-2)/4 < nb_update; i += 4) {
                if(parser[i].equals("-1")){
                    plateau.addObstacle(Integer.parseInt(parser[i + 2]), Integer.parseInt(parser[i + 3]));
                }
                else{
                    plateau.occupeCase(Integer.parseInt(parser[i + 2]), Integer.parseInt(parser[i + 3]),Integer.parseInt(parser[i]), parser[i+1]);
                }
            }
        } else if (type.equals(update)) {
            assert (parser.length == 5);
            plateau.freeUserCase(Integer.parseInt(parser[1]));
            //si il etait deja sur le plateau
            plateau.occupeCase(Integer.parseInt(parser[3]),Integer.parseInt(parser[4]),Integer.parseInt(parser[1]),parser[2]);
        } else if (type.equals(leaving_player)) {
            assert (parser.length == 2);
            if(!plateau.freeUserCase(Integer.parseInt(parser[1]))){
                System.out.println("fail to purge "+parser[1]);
            }
        } else if (type.equals(message_from)) {
            assert (parser.length == 3);
            System.out.println(parser[1] + " : " + parser[2]);
            //todo graphique part
        } else if (type.equals(hello_player)) {
            assert (parser.length == 4);
            plateau.tabInit();
            plateau.reserveCase(Integer.parseInt(parser[2]),Integer.parseInt(parser[3]),getID());
            initChunkQueueReceiver(Integer.parseInt(parser[1]));
            requestHelloChunk(Integer.parseInt(parser[2]), Integer.parseInt(parser[3]));
        } else {
            System.out.println("Error : In Player Message Type unknown");
        }
        ui.drawChunk(plateau);
    }


    private void setID(int id) {
        this.id = id;
    }

    private int getID() {
        return id;
    }

    private String getPseudo() {
        return pseudo;
    }


}
