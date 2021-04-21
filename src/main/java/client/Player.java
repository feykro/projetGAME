package client;

import com.rabbitmq.client.*;
import utils.Chunk;
import utils.Direction;

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

    private int currentChunkNumber;

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

    public void requestHelloChunk() {
        String message = hello_chunk+" "+String.valueOf(getID());
        requestToChunk(message);
    }

    public void requestMove(Direction direction) {
        String message = move+" "+String.valueOf(getID())+" "+direction;
        requestToChunk(message);
    }

    public void requestSay(Direction direction,String msg) {
        String message = say+" "+String.valueOf(getID())+" "+direction+" "+msg;
        requestToChunk(message);
    }

    public void requestLeaveGame(Direction direction,String msg) {
        String message = leave+" "+String.valueOf(getID());
        requestToChunk(message);
    }

    private void requestToChunk(String message){
        try {
            chunk.basicPublish(ExchangeChunkPlayerName, "ChunkManager"+currentChunkNumber, null, message.getBytes(StandardCharsets.UTF_8));
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
        currentChunkNumber = chunkNumber;
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
        String parser[] = message.split(" ");
        assert(parser.length > 0);
        String type = parser[0];
        if(type.equals(update)){
            assert(parser.length > 6);
            int nb_update = Integer.parseInt(parser[1]);
            for(int i=2;i<nb_update;i+=5){
                plateau.freeCase(Integer.parseInt(parser[i+1]),Integer.parseInt(parser[i+2]));
                plateau.occupeCase(Integer.parseInt(parser[i+3]), Integer.parseInt(parser[i+4]), parser[i]);
            }
        } else if(type.equals(leaving_player)){
            assert(parser.length == 3);
            plateau.freeCase(Integer.parseInt(parser[1]),Integer.parseInt(parser[2]));
        } else if(type.equals(message_from)){
            assert(parser.length == 3);
            System.out.println(parser[1]+" : "+parser[2]);
            //todo graphique part
        } else if(type.equals(hello_player)){
            assert(parser.length == 2);
            initChunkQueueReciever(Integer.parseInt(parser[1]));
            requestHelloChunk();
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
