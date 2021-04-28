package client;

import UI.GraphiqueChunk;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import sun.misc.Signal;
import utils.Chunk;
import utils.Direction;
import utils.NotificationSound;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static java.lang.Math.min;
import static utils.Direction.SOUTH;
import static utils.Direction.getDirection;
import static utils.ExchangeName.*;

/**
 * Manage Player
 */
public class Player {

    private final NotificationSound soundBox;
    private final Chunk plateau;
    private final GraphiqueChunk ui;

    private final PlayerSender sender;
    private final PlayerReceiver receiver;
    private Direction direction;
    private int currentChunkNumber;
    private String pseudo;
    private int id;

    public Player(String pseudo) throws IOException, TimeoutException {

        Signal.handle(new Signal("INT"), signal -> disconnect());

        this.pseudo = pseudo;

        this.direction = SOUTH;

        this.plateau = new Chunk();
        soundBox = new NotificationSound();

        this.ui = new GraphiqueChunk(plateau.getTaille(), this);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel portailRequest = connection.createChannel();
        Channel chunk = connection.createChannel();
        Channel id_response = connection.createChannel();
        portailRequest.exchangeDeclare(ExchangePortailRequestName, BuiltinExchangeType.TOPIC, true);
        id_response.exchangeDeclare(ExchangeIDRespondName, BuiltinExchangeType.DIRECT, true);
        chunk.exchangeDeclare(ExchangeChunkPlayerName, BuiltinExchangeType.TOPIC, true);

        sender = new PlayerSender(this, portailRequest, chunk);
        receiver = new PlayerReceiver(this, id_response, chunk);

        receiver.initReciveID();
        connect();
    }

    private void disconnect() {
        System.out.println("Je me deconnect");
        LeaveGame();
        System.exit(0);
    }


    /**
     * Initialise the queue to take respond of the request spawn from portal
     */
    public void spawn() {
        System.out.println("Je demande un spawn point");
        sender.spawnRequest();
    }

    /**
     * First Request to get an game id to communicate with chunk/portal
     */
    public void connect() {
        System.out.println("Je fais une demande de connection");
        sender.idRequest();
    }

    /**
     * Send fail to connect because all chunk are full
     */
    public void failConnect() {
        System.out.println("J'echoue a me connecter ");
        receiver.disconnect();
        sender.sendFull();

    }

    /**
     * Request to chunk when Player enter in new chunk to signal it
     *
     * @param x coordonate to initial position in the chunk
     * @param y coordonate to initial position in the chunk
     */
    public void helloChunk(int x, int y) {
        System.out.println("Je dis au chunk " + getCurrentChunkNumber() + " que je rentre dans sa zone");
        sender.requestHelloChunk(x, y, getPseudo(), getCurrentDirection());
    }

    /**
     * Update to chunk and player when Player turn
     *
     * @param direction the direction of the movement
     */
    public void turn(Direction direction) {
        System.out.println("je tourne vers " + direction);
        sender.sendTurn(direction);
        plateau.updateDirection(getID(), direction);
        ui.drawChunk(plateau);
    }

    /**
     * move or turn on the chunk in the direction
     *
     * @param direction the direction of the movement
     */
    public void move(Direction direction) {

        if (this.direction != direction) {
            this.direction = direction;
            turn(direction);
        } else {
            System.out.println("Je bouge vers " + direction);
            sender.requestMove(direction);
        }
    }

    /**
     * Request to chunk when player would say something to neighbour
     *
     * @param msg
     */
    public void saySomething(String msg) {
        System.out.println("Je dis '" + msg + "' at " + getCurrentDirection());
        sender.requestSay(msg, getCurrentDirection());
        ui.drawSay(plateau, getID(), msg);
    }

    /**
     * Request to chunk when Player would leave the game
     */
    public void LeaveGame() {
        System.out.println("Je leave le jeu");
        boolean chunkQueueActive = receiver.disconnect();
        if (chunkQueueActive)
            sender.requestLeaveGame();
    }


    public void manageInfoChunkMessage(String[] parser) {
        System.out.println("Message Info chunk");
        assert (parser.length > 6);
        int nb_update = Integer.parseInt(parser[1]);
        for (int i = 2; (i - 2) / 5 < nb_update; i += 5) {
            if (parser[i].equals("-1")) {
                plateau.addObstacle(Integer.parseInt(parser[i + 2]), Integer.parseInt(parser[i + 3]));
            } else {
                plateau.occupeCase(Integer.parseInt(parser[i + 2]), Integer.parseInt(parser[i + 3]), Integer.parseInt(parser[i]), parser[i + 1], getDirection(parser[i + 4]));
            }
        }
        System.out.println("-> chunk is update :");
        plateau.showChunk();
    }

    public void manageUpdateMessage(String[] parser) {
        System.out.println("Message Update position");
        assert (parser.length == 6);
        //si il etait deja sur le plateau
        plateau.freeUserCase(Integer.parseInt(parser[1]));
        plateau.occupeCase(Integer.parseInt(parser[3]), Integer.parseInt(parser[4]), Integer.parseInt(parser[1]), parser[2], getDirection(parser[5]));
        System.out.println("-> the new position of " + parser[2] + "(" + parser[1] + ") is : " + "(" + parser[3] + ";" + parser[4] + ")");
        ui.drawChunk(plateau);
    }

    public void manageLeavingPeopleMessage(String[] parser) {
        System.out.println("Message leaving People");
        assert (parser.length == 2);
        if (!plateau.freeUserCase(Integer.parseInt(parser[1]))) {
            System.out.println("-> fail to purge " + parser[1]);
        } else {
            System.out.println("-> success to purge " + parser[1]);
            ui.drawChunk(plateau);
        }
    }

    public void manageTurnPeopleMessage(String[] parser) {
        System.out.println("Message turn");
        assert (parser.length == 3);
        if (!plateau.updateDirection(Integer.parseInt(parser[1]), getDirection(parser[2]))) {
            System.out.println("-> fail to turn " + parser[1]);
        } else {
            System.out.println("-> " + parser[1] + " successfully turn at " + parser[2]);
            ui.drawChunk(plateau);
        }
    }

    public void manageSayFromAnotherPeopleMessage(String[] parser, String fullmessage) {
        System.out.println("Message SaySomething");
        assert (parser.length >= 4);
        int fromID = Integer.parseInt(parser[1]);
        String fromPseudo = parser[2];
        String msg = fullmessage.substring(parser[0].length() + parser[1].length() + parser[2].length() + 3);
        System.out.println("-> " + fromPseudo + "("+parser[1]+") : " + msg);
        ui.drawSay(plateau, fromID, msg);
        soundBox.notification_new_message();
    }

    public void manageHelloPlayerMessage(String[] parser) {
        System.out.println("Message Hello Player From " + parser[1]);
        assert (parser.length == 4);
        plateau.tabInit();
        plateau.reserveCase(Integer.parseInt(parser[2]), Integer.parseInt(parser[3]), getID());
        receiver.initChunkQueueReceiver(Integer.parseInt(parser[1]));
        helloChunk(Integer.parseInt(parser[2]), Integer.parseInt(parser[3]));
        ui.drawChunk(plateau);
    }

    public void manageFailMessage(String[] parser) {
        System.out.println("Message FAIL");
        assert (parser.length == 2);
        if (parser[1].equals("connect")) {
            System.out.println("-> Game full");
            JOptionPane.showMessageDialog(ui, "Game is full", "Fail to connect",
                    JOptionPane.WARNING_MESSAGE);
            failConnect();
            System.exit(1);

        } else if (parser[1].equals("move")) {
            System.out.println("-> fail to move");
            soundBox.notification_fail();
        } else {
            System.out.println("-> Unknown fail type");
        }
    }

    public void enterPseudo() {
        String input = JOptionPane.showInputDialog(ui, "Enter Nickname");
        if (input == null) {
            failConnect();
            System.exit(1);
        }
        if (input.equals("")) {
            pseudo += id;
        } else {
            pseudo = input.replaceAll(" ", "_").substring(0, min(input.length(), 10));
        }
        System.out.println("Mon pseudo est '" + pseudo + "'");
    }

    /**
     * check if id is a real id (less than -1 more than 16) and add save it if it s real
     *
     * @param id
     * @return true if id is real
     */
    public boolean checkID(String id) {
        System.out.println("mon id est " + id);
        int tmp = Integer.parseInt(id);
        if (tmp >= 0) {
            setID(tmp);
            return true;
        }
        return false;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    private String getPseudo() {
        return pseudo;
    }

    public int getCurrentChunkNumber() {
        return currentChunkNumber;
    }

    public void setCurrentChunkNumber(int id) {
        currentChunkNumber = id;
    }

    private Direction getCurrentDirection() {
        return direction;
    }

}
