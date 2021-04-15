import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class Portail {

    private String sysQueueName;    //nom de la queue entre chunks + portail
    private String managePlayerQueueName;   //nom de la queue pour gérer les joueurs

    private Channel sysQueue;
    private Channel managePlayerQueue;
    private Connection connexion;

    public Portail(){

    }
}
