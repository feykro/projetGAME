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


public class Portail {

    private Channel portail_request;
    private Channel sys;
    private Channel id_response;

    private Connection connection;

    private ArrayList<String> listID;

    public Portail() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        portail_request = connection.createChannel();
        sys = connection.createChannel();
        id_response = connection.createChannel();

        portail_request.exchangeDeclare(ExchangePortailRequestName, BuiltinExchangeType.TOPIC,true);
        sys.exchangeDeclare(ExchangeSysName, BuiltinExchangeType.TOPIC,true);
        id_response.exchangeDeclare(ExchangeIDRespondName, BuiltinExchangeType.DIRECT,true);

        initIDList();
        initRecepteurIDRequest();
        initRecepteurSpawnRequest();
        initSysRecepteur();

    }


    private void initRecepteurIDRequest(){
        String queueSysName = queuePortailRequestIDName;
        try {
            portail_request.queueDeclare(queueSysName, true, false, false, null);
            portail_request.queueBind(queueSysName, ExchangePortailRequestName, "ID");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String finalQueueSysName = queueSysName;
        new Thread() {
            public void run() {
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String respond = getFreeID();
                    id_response.basicPublish(ExchangeIDRespondName, "", null, respond.getBytes("UTF-8"));

                };
                try {
                    portail_request.basicConsume(finalQueueSysName, true, deliverCallback, consumerTag -> {
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initRecepteurSpawnRequest(){
        String queueSysName = queuePortailRequestSpawnName;
        try {
            portail_request.queueDeclare(queueSysName, true, false, false, null);
            portail_request.queueBind(queueSysName, ExchangePortailRequestName, "SPAWN");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String finalQueueSysName = queueSysName;
        new Thread() {
            public void run() {
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String id = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    int managerRDM = Integer.parseInt(id)%4;
                    String message = find_spawn+" "+id+" "+managerRDM;
                    System.out.println("Sending message : "+message);
                    sys.basicPublish(ExchangeSysName, "Chunk"+managerRDM, null, message.getBytes("UTF-8"));
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

    private void initSysRecepteur(){
        String queueSysName = queuePortailSysName;
        try {
            sys.queueDeclare(queueSysName, true, false, false, null);
            sys.queueBind(queueSysName, ExchangeSysName, "Portail");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String finalQueueSysName = queueSysName;
        new Thread() {
            public void run() {
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    String parser[] = message.split(" ");
                    assert(parser.length == 2 && parser[0].equals(free_ID));
                    addFreeID(parser[1]);
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

    private void initIDList(){
        listID = new ArrayList<String>();
        for(int i=0; i<16;i++){
            addFreeID(Integer.toString(i));
        }
    }

    private String getFreeID(){
        if(listID.size() > 0){
            return listID.remove(0);
        }
        return "-1";
    }
    private void addFreeID(String id){
        listID.add(id);
    }
}
