package system;

import com.rabbitmq.client.*;
import com.rabbitmq.tools.json.JSONUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static utils.ExchangeName.*;
import static utils.QueueName.queuePortailRequestIDName;


public class Portail {

    private Channel id_request;
    private Channel sys;
    private Channel id_response;

    private Connection connection;

    private ArrayList<String> listID;

    public Portail() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        id_request = connection.createChannel();
        sys = connection.createChannel();
        id_response = connection.createChannel();

        id_request.exchangeDeclare(ExchangeIDRequestName, BuiltinExchangeType.DIRECT,true);
        sys.exchangeDeclare(ExchangeSysName, BuiltinExchangeType.DIRECT,true);
        id_response.exchangeDeclare(ExchangeIDRespondName, BuiltinExchangeType.DIRECT,true);

        initIDList();
        initRecepteurIDRequest();

    }


    private void initRecepteurIDRequest(){
        String queueSysName = queuePortailRequestIDName;
        try {
            id_request.queueDeclare(queueSysName, true, false, false, null);
            id_request.queueBind(queueSysName, ExchangeIDRequestName, "");
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
            listID.add(Integer.toString(i));
        }
    }

    private String getFreeID(){
        /*if(listID.size() > 0){
            return listID.remove(0);
        }*/
        return "-1";
    }
}
