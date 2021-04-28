package system;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static utils.ExchangeName.*;
import static utils.MessageType.find_spawn;
import static utils.MessageType.free_ID;
import static utils.QueueName.*;

/**
 * Manage Connection Portal
 */
public class Portail {

    private final Channel portail_request;
    private final Channel sys;
    private final Channel id_response;

    private ArrayList<String> listID;

    public Portail() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        portail_request = connection.createChannel();
        sys = connection.createChannel();
        id_response = connection.createChannel();

        portail_request.exchangeDeclare(ExchangePortailRequestName, BuiltinExchangeType.TOPIC, true);
        sys.exchangeDeclare(ExchangeSysName, BuiltinExchangeType.TOPIC, true);
        id_response.exchangeDeclare(ExchangeIDRespondName, BuiltinExchangeType.DIRECT, true);

        initIDList();
        initSysRecepteur();
        initRecepteurFull();
        initRecepteurIDRequest();
        initRecepteurSpawnRequest();

    }

    /**
     * initialise the queue to catch id request from new Player
     */
    private void initRecepteurIDRequest() {
        String queueSysName = queuePortailRequestIDName;
        try {
            portail_request.queueDeclare(queueSysName, true, false, false, null);
            portail_request.queueBind(queueSysName, ExchangePortailRequestName, "ID");
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String respond = getFreeID();
                id_response.basicPublish(ExchangeIDRespondName, "", null, respond.getBytes(StandardCharsets.UTF_8));

            };
            try {
                portail_request.basicConsume(queueSysName, true, deliverCallback, consumerTag -> {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * initialise queue to catch the fail connection message from player
     */
    private void initRecepteurFull() {
        String queueSysName = queuePortailRequestIDName;
        try {
            queueSysName = id_response.queueDeclare().getQueue();
            portail_request.queueBind(queueSysName, ExchangePortailRequestName, "Fail");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String finalQueueSysName = queueSysName;
        new Thread(() -> {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                String[] parser = message.split(" ");
                assert (parser.length == 2 && parser[0].equals(free_ID));
                addFreeID(parser[1]);
            };
            try {
                portail_request.basicConsume(finalQueueSysName, true, deliverCallback, consumerTag -> {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * initialise queue to catch spawn request from player after request id
     */
    private void initRecepteurSpawnRequest() {
        String queueSysName = queuePortailRequestSpawnName;
        try {
            portail_request.queueDeclare(queueSysName, true, false, false, null);
            portail_request.queueBind(queueSysName, ExchangePortailRequestName, "SPAWN");
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String id = new String(delivery.getBody(), StandardCharsets.UTF_8);
                int managerRDM = Integer.parseInt(id) % 4;
                String message = find_spawn + " " + id + " " + managerRDM + " " + 0;
                System.out.println("Sending message : " + message);
                sys.basicPublish(ExchangeSysName, "Chunk" + managerRDM, null, message.getBytes(StandardCharsets.UTF_8));
            };
            try {
                sys.basicConsume(queueSysName, true, deliverCallback, consumerTag -> {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * initialise queue to catch sys message from chunk
     * the chunk which frees the player ID when the player leaves the game
     */
    private void initSysRecepteur() {
        String queueSysName = queuePortailSysName;
        try {
            sys.queueDeclare(queueSysName, true, false, false, null);
            sys.queueBind(queueSysName, ExchangeSysName, "Portail");
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                String[] parser = message.split(" ");
                assert (parser.length == 2 && parser[0].equals(free_ID));
                addFreeID(parser[1]);
            };
            try {
                sys.basicConsume(queueSysName, true, deliverCallback, consumerTag -> {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * initialise the list of free id for player
     */
    private void initIDList() {
        listID = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            addFreeID(Integer.toString(i));
        }
    }

    /**
     * return free id and remove it from the listID
     * @return free id or -1 if the listID is empty
     */
    private String getFreeID() {
        if (listID.size() > 0) {
            return listID.remove(0);
        }
        return "-1";
    }

    /**
     * add new free id to listID
     * @param id the new id to add
     */
    private void addFreeID(String id) {
        System.out.println("Portal: got id : " + id);
        listID.add(id);

    }
}
