package system;

import com.rabbitmq.client.*;
import utils.Chunk;

public class ChunkManager {

    private Chunk chunkNorris;
    private int identifiant;
    private String sysQueueName;    //nom de la queue entre chunks + portail
    private String managePlayerQueueName;   //nom de la queue pour gérer les joueurs

    private Channel sysQueue;
    private Channel managePlayerQueue;
    private Connection connexion;

    public ChunkManager(Chunk chunk){
        chunkNorris = chunk;
        sysQueueName = "sysQueue"+Integer.toString(identifiant);
        managePlayerQueueName = "manageQueue"+Integer.toString(identifiant);

        //Connexion à RabbitMQ pour créer la réception (ou boite au lettre)
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            this.connexion = factory.newConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //============== initialisation des queues ==================

    private void initQueues() throws Exception{
        this.sysQueue= this.connexion.createChannel();
        this.sysQueue.queueDeclare(this.sysQueueName, true, false, false, null);
        this.managePlayerQueue = this.connexion.createChannel();
        this.managePlayerQueue.queueDeclare(this.managePlayerQueueName, true, false, false, null);
    }

    //============================================================

}
