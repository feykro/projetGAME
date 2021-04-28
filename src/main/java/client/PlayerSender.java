package client;

import com.rabbitmq.client.Channel;
import utils.Direction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static utils.ExchangeName.ExchangeChunkPlayerName;
import static utils.ExchangeName.ExchangePortailRequestName;
import static utils.MessageType.*;

/**
 * Manage Player Request
 */
public class PlayerSender {

    private final Channel portailRequest;
    private final Channel chunk;
    private final Player player;

    public PlayerSender(Player player, Channel portailRequest, Channel chunk) {
        this.portailRequest = portailRequest;
        this.chunk = chunk;
        this.player = player;
    }

    /**
     * Initialise the queue to take respond of the request spawn from portal
     */
    public void spawnRequest() {
        try {
            portailRequest.basicPublish(ExchangePortailRequestName, "SPAWN", null, Integer.toString(player.getID()).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * First Request to get an game id to communicate with chunk/portal
     */
    public void idRequest() {
        try {
            portailRequest.basicPublish(ExchangePortailRequestName, "ID", null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send fail to connect because all chunk are full
     */
    public void sendFull() {
        String message = fail + " " + player.getID();
        System.out.println("je dis au portail de reprendre mon id (" + player.getID() + ")");
        try {
            portailRequest.basicPublish(ExchangePortailRequestName, "Fail", null, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Request to chunk when Player enter in new chunk to signal it
     *
     * @param x coordonate to initial position in the chunk
     * @param y coordonate to initial position in the chunk
     */
    public void requestHelloChunk(int x, int y, String pseudo, Direction direction) {
        String message = hello_chunk + " " + player.getID() + " " + pseudo + " " + x + " " + y + " " + direction;
        requestToChunk(message);
    }

    /**
     * Update to chunk and player when Player turn
     *
     * @param direction the direction of the movement
     */
    public void sendTurn(Direction direction) {
        String message = turn + " " + player.getID() + " " + direction;
        requestToChunkAndPlayer(message);
    }

    /**
     * Request to chunk when Player would move in the chunk
     *
     * @param direction the direction of the movement
     */
    public void requestMove(Direction direction) {
        String message = move + " " + player.getID() + " " + direction;
        requestToChunk(message);
    }

    /**
     * Request to chunk when player would say something to neighbour
     *
     * @param msg
     */
    public void requestSay(String msg, Direction direction) {
        String message = say + " " + player.getID() + " " + direction + " " + msg;
        requestToChunk(message);
    }

    /**
     * Request to chunk when Player would leave the game
     */
    public void requestLeaveGame() {
        String message = leave + " " + player.getID();
        requestToChunk(message);
    }


    /**
     * general methode to send message to current chunk and players
     *
     * @param message
     */
    private void requestToChunkAndPlayer(String message) {
        try {
//            chunk.basicPublish(ExchangeChunkPlayerName, "Chunk" + player.getCurrentChunkNumber() + ".#", null, message.getBytes(StandardCharsets.UTF_8));
            chunk.basicPublish(ExchangeChunkPlayerName, "Chunk" + player.getCurrentChunkNumber(), null, message.getBytes(StandardCharsets.UTF_8));
            chunk.basicPublish(ExchangeChunkPlayerName, "Chunk" + player.getCurrentChunkNumber() + ".Players", null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("J' ai mi a jour mes informations pour le chunk " + player.getCurrentChunkNumber() + " et les players abonnés sur la clé 'Chunk" + player.getCurrentChunkNumber() + ".Players'");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * general methode to send message to current chunk
     *
     * @param message
     */
    private void requestToChunk(String message) {
        try {
            chunk.basicPublish(ExchangeChunkPlayerName, "Chunk" + player.getCurrentChunkNumber(), null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("J'ai fait une demande au chunk " + player.getCurrentChunkNumber());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
