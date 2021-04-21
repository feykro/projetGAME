package client;

import com.rabbitmq.client.*;
import utils.Chunk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static utils.ExchangeName.*;
import static utils.QueueName.queuePortailRequestIDName;
import static utils.QueueName.queuePortailRequestSpawnName;
import static utils.MessageType.*;


public class Player {

    private Chunk plateau;

    private Channel portailRequest;
    private Channel id_response;
    private Channel chunk;

    private String finalQueueIDrespondName;
    private String finalQueueSysName;

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

        portailRequest.exchangeDeclare(ExchangeIDRequestName, BuiltinExchangeType.DIRECT, true);
        id_response.exchangeDeclare(ExchangeIDRespondName, BuiltinExchangeType.DIRECT, true);
        chunk.exchangeDeclare(ExchangeChunkPlayerName, BuiltinExchangeType.TOPIC, true);
        initReciveID();
        idRequest();
    }

    public void idRequest(){
        try {
            portailRequest.queueBind(queuePortailRequestIDName, ExchangeIDRequestName, "");
            portailRequest.basicPublish(ExchangeIDRequestName, "", null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initReciveID(){
        String queueSysName = null;
        try {
            queueSysName = id_response.queueDeclare().getQueue();
            id_response.queueBind(queueSysName, ExchangeIDRespondName,"");
        } catch (IOException e) {
            e.printStackTrace();
        }
        finalQueueIDrespondName = queueSysName;
        new Thread() {
            public void run() {
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String id = new String(delivery.getBody(), "UTF-8");
                    if(checkID(id)) {
                        try {
                            id_response.queueUnbind(finalQueueIDrespondName, ExchangeIDRespondName, "");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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

    public void spawnRequest(){
        try {
            portailRequest.queueBind(queuePortailRequestSpawnName, ExchangeIDRequestName, "");
            portailRequest.basicPublish(ExchangeIDRequestName, "", null, Integer.toString(id).getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void initPersonalQueueReciever(){
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

    public void initChunkQueueReciever(int chunkNumber){

        String queueChunkName = null;
        try {
            String key = "Chunk"+chunkNumber;
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


    private boolean checkID(String id) {
        System.out.println("mon id est " + id);
        int tmp = Integer.parseInt(id);
        if (tmp >= 0) {
            setID(tmp);
            return true;
        }
        return false;
    }

    private void manageMessage(String message){
        String parter[] = message.split(" ");
        assert(parter.length > 0);
        String type = parter[0];
        if(type.equals(update)){
            assert(parter.length > 6);
            int nb_update = Integer.parseInt(parter[1]);
            for(int i=2;i<nb_update;i+=5){
                plateau.freeCase(Integer.parseInt(parter[i+1]),Integer.parseInt(parter[i+2]));
                plateau.occupeCase(Integer.parseInt(parter[i+3]), Integer.parseInt(parter[i+4]), parter[i]);
            }
        } else if(type.equals(leaving_player)){
            assert(parter.length == 2);
            plateau.freeCase(Integer.parseInt(parter[1]),Integer.parseInt(parter[2]));
        } else if(type.equals(message_from)){
            assert(parter.length == 2);
            System.out.println(parter[1]+" : "+parter[2]);
            //todo graphique part
        } else {
            System.out.println("Error : In Player Message Type unknown");
        }
    }


    private void setID(int id){
        this.id = id;
    }

    private int getID(){
        return id;
    }





}
