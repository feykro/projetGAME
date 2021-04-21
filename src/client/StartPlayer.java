package client;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class StartPlayer {
    public static void main(String[] args) {
        try {
            new Player("thomas");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}