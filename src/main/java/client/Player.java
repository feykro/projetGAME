package client;

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

    private Channel portailRequest;
    private Channel id_response;
    private Channel chunk;

    private int currentChunkNumber;

    private String finalQueueIDrespondName = null;
    private String finalQueueSysName = null;
    private String finalQueuequeueChunkName = null;

    private String pseudo;
    private int id;

    private Connection connection;

    private ArrayList<String> listID;

    public Player(String pseudo) throws IOException, TimeoutException {

        this.pseudo = pseudo;

        this.plateau = new Chunk();

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
                        initPersonalQueueReciever();
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
     * Initialise personal queue to sub on his id to take specifique message from chunk/portal
     */
    public void initPersonalQueueReciever() {
        String queueChunkName = null;
        try {
            String key = String.valueOf(getID());

            queueChunkName = chunk.queueDeclare().getQueue();
            chunk.queueBind(queueChunkName, ExchangeChunkPlayerName, key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finalQueueSysName = queueChunkName;
        new Thread() {
            public void run() {
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    manageMessage(message);
                };
                try {
                    chunk.basicConsume(finalQueueSysName, true, deliverCallback, consumerTag -> {
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
    public void initChunkQueueReciever(int chunkNumber) {
        //unbind old chunk queue
        unbindQueue(finalQueuequeueChunkName, ExchangeChunkPlayerName, "Chunk" + currentChunkNumber);

        System.out.println("mon chunk est " + chunkNumber);
        currentChunkNumber = chunkNumber;
        String queueChunkName = null;
        try {
            String key = "Chunk" + chunkNumber;
            queueChunkName = chunk.queueDeclare().getQueue();
            chunk.queueBind(queueChunkName, ExchangeChunkPlayerName, key);
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
                    chunk.basicConsume(finalQueueSysName, true, deliverCallback, consumerTag -> {
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
            chunk.basicPublish(ExchangeChunkPlayerName, "ChunkManager" + currentChunkNumber, null, message.getBytes(StandardCharsets.UTF_8));
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
            e.printStackTrace();
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
     * manage message from chunk
     *
     * @param message
     */
    private void manageMessage(String message) {
        String parser[] = message.split(" ");
        assert (parser.length > 0);
        String type = parser[0];
        if (type.equals(update)) {
            assert (parser.length > 7);
            int nb_update = Integer.parseInt(parser[1]);
            for (int i = 2; i < nb_update; i += 5) {
                plateau.freeCase(Integer.parseInt(parser[i + 2]), Integer.parseInt(parser[i + 3]));
                plateau.occupeCase(Integer.parseInt(parser[i + 4]), Integer.parseInt(parser[i + 5]),Integer.parseInt(parser[i]), parser[i+1]);
            }
            //todo graphique part
        } else if (type.equals(leaving_player)) {
            assert (parser.length == 3);
            plateau.freeCase(Integer.parseInt(parser[1]), Integer.parseInt(parser[2]));
            //todo graphique part
        } else if (type.equals(message_from)) {
            assert (parser.length == 3);
            System.out.println(parser[1] + " : " + parser[2]);
            //todo graphique part
        } else if (type.equals(hello_player)) {
            assert (parser.length == 4);
            initChunkQueueReciever(Integer.parseInt(parser[1]));
            requestHelloChunk(Integer.parseInt(parser[2]), Integer.parseInt(parser[3]));
            //todo graphique part
        } else {
            System.out.println("Error : In Player Message Type unknown");
        }
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
