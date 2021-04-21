package client;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static utils.ExchangeName.*;
import static utils.QueueName.queuePortailRequestIDName;

public class Player {
    private Channel id_request;
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
        id_request = connection.createChannel();
        chunk = connection.createChannel();
        id_response = connection.createChannel();

        id_request.exchangeDeclare(ExchangeIDRequestName, BuiltinExchangeType.DIRECT, true);
        id_response.exchangeDeclare(ExchangeIDRespondName, BuiltinExchangeType.DIRECT, true);
        chunk.exchangeDeclare(ExchangeChunkPlayerName, BuiltinExchangeType.TOPIC, true);
        iniReciveID();
        idRequest();
    }

    public void idRequest(){
        try {
            id_request.queueBind(queuePortailRequestIDName, ExchangeIDRequestName, "");
            id_request.basicPublish(ExchangeIDRequestName, "", null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void iniReciveID(){
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

    private boolean checkID(String id) {
        System.out.println("mon id est " + id);
        int tmp = Integer.parseInt(id);
        if (tmp >= 0) {
            setID(tmp);
            System.out.println("je passe");
            return true;
        }
        return false;
    }

    private void setID(int id){
        this.id = id;
    }






}
