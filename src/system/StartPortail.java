package system;

import client.Player;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class StartPortail {
    public static void main(String[] args) {
        try {
            new Portail();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
