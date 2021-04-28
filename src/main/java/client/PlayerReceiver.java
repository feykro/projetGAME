package client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static utils.ExchangeName.ExchangeChunkPlayerName;
import static utils.ExchangeName.ExchangeIDRespondName;
import static utils.MessageType.*;


/**
 * Manage player queue
 */
public class PlayerReceiver {

    private final Channel id_response;
    private final Channel chunk;
    private String finalQueueIDrespondName = null;
    private String finalPersonalQueueName = null;
    private String finalQueuequeueChunkName = null;
    private final Player player;

    public PlayerReceiver(Player player, Channel id_response, Channel chunk) {
        this.id_response = id_response;
        this.chunk = chunk;
        this.player = player;
    }

    /**
     * Initialise the queue to take respond of the request id from portal
     */
    public void initReciveID() {
        String queueSysName = null;
        try {
            queueSysName = id_response.queueDeclare().getQueue();
            id_response.queueBind(queueSysName, ExchangeIDRespondName, "");
            System.out.println("je m'abonne a la queue "+queueSysName);
        } catch (IOException e) {
            System.out.println("ERROR : j'ai echoué a m'abonner a la queue "+queueSysName);
        }
        finalQueueIDrespondName = queueSysName;
        new Thread(() -> {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String id = new String(delivery.getBody(), StandardCharsets.UTF_8);
                if (player.checkID(id)) {
                    unbindQueue(finalQueueIDrespondName, ExchangeIDRespondName, "");
                    finalQueueIDrespondName = null;
                    player.enterPseudo();
                    initPersonalQueueReceiver();
                    player.spawn();
                }
            };
            try {
                id_response.basicConsume(finalQueueIDrespondName, true, deliverCallback, consumerTag -> {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Initialise personal queue to sub on his id to take specific message from chunk/portal
     */
    public void initPersonalQueueReceiver() {

        String queuePersonalName = null;
        try {
            String key = String.valueOf(player.getID());

            queuePersonalName = chunk.queueDeclare().getQueue();
            chunk.queueBind(queuePersonalName, ExchangeChunkPlayerName, key);
            System.out.println("je m'abonne a la queue "+queuePersonalName);
            System.out.println("Je suis abonné au jeu sur la clé " + key);

        } catch (IOException e) {
            System.out.println("ERROR : j'ai echoué à m'abonner a la queue "+queuePersonalName);
        }
        finalPersonalQueueName = queuePersonalName;
        new Thread(() -> {
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
        }).start();
    }

    /**
     * Initialise chunk queue to have message of the chunk where it is currently
     */
    public void initChunkQueueReceiver(int chunkNumber) {
        //unbind old chunk queue
        if (finalQueuequeueChunkName != null) {
            unbindQueue(finalQueuequeueChunkName, ExchangeChunkPlayerName, "Chunk" + player.getCurrentChunkNumber() + ".Players");
        }
        player.setCurrentChunkNumber(chunkNumber);
        String queueChunkName = null;
        try {
            String key = "Chunk" + chunkNumber + ".Players";
            queueChunkName = chunk.queueDeclare().getQueue();
            chunk.queueBind(queueChunkName, ExchangeChunkPlayerName, key);
            System.out.println("je m'abonne a la queue "+queueChunkName);
            System.out.println("Je suis abonné au chunk " + chunkNumber + " avec la clef " + key);
        } catch (IOException e) {
            System.out.println("ERROR : j'ai echoué à m'abonner a la queue "+queueChunkName);
        }
        finalQueuequeueChunkName = queueChunkName;
        new Thread(() -> {
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
        }).start();
    }


    /**
     * Manage message from chunk and update Ui
     *
     * @param message
     */
    public void manageMessage(String message) {
        System.out.println("J ai recu un message");
        String[] parser = message.split(" ");
        assert (parser.length > 0);
        String type = parser[0];
        if (type.equals(info_chunk)) {
            player.manageInfoChunkMessage(parser);
        } else if (type.equals(update)) {
            player.manageUpdateMessage(parser);
        } else if (type.equals(leaving_player)) {
            player.manageLeavingPeopleMessage(parser);
        } else if (type.equals(turn)) {
            player.manageTurnPeopleMessage(parser);
        } else if (type.equals(message_from)) {
            player.manageSayFromAnotherPeopleMessage(parser, message);
        } else if (type.equals(hello_player)) {
            player.manageHelloPlayerMessage(parser);
        } else if (type.equals(fail)) {
            player.manageFailMessage(parser);
        } else {
            System.out.println("Error : In Player Message Type unknown");
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
            System.out.println("je me desabonne de la queue "+queueName);
            return true;
        } catch (IOException e) {
            System.out.println("ERROR : j'ai echoué a me desabonner de la queue "+queueName);
            return false;
        }
    }

    /**
     * disconnect queue
     *
     * @return true if chunk queue was active
     */
    public boolean disconnect() {
        System.out.println("Je me desabonne de toutes mes queues");
        if (finalPersonalQueueName != null)
            unbindQueue(finalPersonalQueueName, ExchangeChunkPlayerName, Integer.toString(player.getID()));
        if (finalQueueIDrespondName != null)
            unbindQueue(finalQueueIDrespondName, ExchangeIDRespondName, "");
        if (finalQueuequeueChunkName != null) {
            unbindQueue(finalQueuequeueChunkName, ExchangeChunkPlayerName, "Chunk" + player.getCurrentChunkNumber() + ".Players");
            return true;
        }
        return false;
    }


}
