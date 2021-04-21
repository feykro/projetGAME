package client;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static utils.ExchangeName.*;
import static utils.QueueName.queuePortailRequestIDName;
import static utils.QueueName.queuePortailRequestSpawnName;


public class Player {
    private Channel portailRequest;
    private Channel id_response;
    private Channel chunk;

    private String finalQueueIDrespondName;

    private String pseudo;
    private int id;

    private Connection connection;

    private ArrayList<String> listID;

    public Player(String pseudo) throws IOException, TimeoutException {

        this.pseudo = pseudo;

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
        String finalQueueSysName = queueChunkName;
        new Thread() {
            public void run() {
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String paquet = new String(delivery.getBody(), StandardCharsets.UTF_8);
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

    private void setID(int id){
        this.id = id;
    }

    private int getID(){
        return id;
    }





}
