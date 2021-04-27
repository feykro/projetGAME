package UI;

import client.Player;
import utils.CaseState;
import utils.Chunk;
import utils.Direction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

/**
 * Manage graphique interface to show player in the chunk
 */

public class GraphiqueChunk extends JFrame {
    private int taille;
    private int casebordersize;
    private int bordersize = 20;
    private int barsize = 30;

    private Player player;
    private Chunk plateau;
    private String message = null;
    private int messageFromID = -1;


    private Image imgDOWN = Toolkit.getDefaultToolkit().getImage("resources/images/personnage_DOWN.png");
    private Image imgTOP = Toolkit.getDefaultToolkit().getImage("resources/images/personnage_TOP.png");
    private Image imgLEFT = Toolkit.getDefaultToolkit().getImage("resources/images/personnage_LEFT.png");
    private Image imgRIGHT = Toolkit.getDefaultToolkit().getImage("resources/images/personnage_RIGHT.png");
    private Image imgStone = Toolkit.getDefaultToolkit().getImage("resources/images/cayou.png");
    private Image imgCross = Toolkit.getDefaultToolkit().getImage("resources/images/lacroix.png");
    private Image imgGrass;


    private HashMap<String, int[]> mapPlayer;


    public GraphiqueChunk(int taille, Player player) {
        this.player = player;
        this.taille = taille;
        casebordersize = 32;
        setSize(900, 900);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
        addCloseListener();
        initListener();
    }

    /**
     * add a listener to leave player and stop app when we close the window
     */
    private void addCloseListener() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                player.LeaveGame();
                System.exit(0);
            }
        });
    }

    /**
     * initialise listener to manage keyinput to moove and send message
     * moove or turn at
     * < left
     * > right
     * ^ top
     * v down
     * SPACE send message in your current direction
     */
    private void initListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (player != null) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_DOWN:
                            player.move(Direction.SOUTH);
                            break;
                        case KeyEvent.VK_UP:
                            player.move(Direction.NORTH);
                            break;
                        case KeyEvent.VK_RIGHT:
                            player.move(Direction.EAST);
                            break;
                        case KeyEvent.VK_LEFT:
                            player.move(Direction.WEST);
                            break;
                        case KeyEvent.VK_SPACE:
                            player.saySomething("Hey coucou toi !");
                            break;
                    }
                }
            }
        });
    }

    /**
     * draw the chunk and entity on it
     *
     * @param chunk
     */
    public void drawChunk(Chunk chunk) {
        plateau = chunk;
        repaint();
    }

    /**
     * draw the chunk and the message from player
     *
     * @param chunk
     * @param id      player who say the message
     * @param message
     */
    public void drawSay(Chunk chunk, int id, String message) {
        plateau = chunk;
        this.message = message;
        this.messageFromID = id;
        repaint();
    }

    /**
     * methode who draw chunk, messages, and entities
     * call repaint to call this
     *
     * @param g
     */
    public void paint(Graphics g) {
        g.clearRect(0, 0, 900, 900);
        imgGrass = Toolkit.getDefaultToolkit().getImage("resources/images/grass" + player.getCurrentChunkNumber() + ".png");
        if (plateau != null) {
            for (int x = 0; x < taille; x++) {
                for (int y = 0; y < taille; y++) {
                    g.drawImage(imgGrass, bordersize + x * ((getWidth() - bordersize * 2) / taille), barsize + bordersize + y * ((getHeight() - barsize - bordersize * 2) / taille), this);
                    switch (plateau.getCase(x, y).getEtat()) {
                        case libre -> {
                            break;
                        }
                        case reservee -> {
                            int locationIMG[] = getLocation(x, y);
                            g.drawImage(imgCross, locationIMG[0] + 2, locationIMG[1] + 15, this);
                            break;
                        }
                        case occupeeJoueur -> {
                            drawPlayer(g, x, y);
                            break;
                        }
                        case occupeeObstacle -> {
                            int locationIMG[] = getLocation(x, y);
                            g.drawImage(imgStone, locationIMG[0] + 2, locationIMG[1] + 15, this);
                            break;
                        }
                    }
                }
            }
        }
        drawGrille(g);
        drawMessage(g);

    }

    /**
     * draw player at x,y
     * assert fail if they are no player at x,y
     *
     * @param g
     * @param x player coordinate
     * @param y player coordinate
     */
    private void drawPlayer(Graphics g, int x, int y) {
        assert (plateau.getCase(x, y).getEtat() == CaseState.occupeeJoueur);
        int locationIMG[] = getLocation(x, y);
        switch (plateau.getCase(x, y).getPlayerDirection()) {
            case SOUTH -> {
                g.drawImage(imgDOWN, locationIMG[0], locationIMG[1], this);
            }
            case NORTH -> {
                g.drawImage(imgTOP, locationIMG[0], locationIMG[1], this);
            }
            case WEST -> {
                g.drawImage(imgLEFT, locationIMG[0], locationIMG[1], this);
            }
            case EAST -> {
                g.drawImage(imgRIGHT, locationIMG[0], locationIMG[1], this);
            }
        }

        String pseudo = plateau.getCase(x, y).getPlayerPseudo();
        g.clearRect(locationIMG[0] + 50 - pseudo.length() * 5, locationIMG[1] + 10, pseudo.length() * 12, 20);
        g.setFont(new Font("Serif", Font.PLAIN, 20));
        g.drawString(pseudo, locationIMG[0] + 50 - pseudo.length() * 5, locationIMG[1] + 25);

    }

    /**
     * draw the grille who delimite cases
     *
     * @param g
     */
    private void drawGrille(Graphics g) {
        for (int width = 0; width < taille; width++) {
            for (int height = 0; height < taille; height++) {
                g.drawRect(bordersize + width * ((getWidth() - bordersize * 2) / taille), barsize + bordersize + height * ((getHeight() - barsize - bordersize * 2) / taille), (getWidth() - bordersize * 2) / taille, ((getHeight() - bordersize * 2 - barsize) / taille));
            }
        }
    }

    /**
     * draw message from messageFromID
     * if he is at top right angle draw it at left of the player
     * if he is at top draw it at right of the player
     * else draw it at top of the player
     *
     * @param g
     */
    private void drawMessage(Graphics g) {
        if (message != null) {
            assert (messageFromID != -1);
            int coor[] = plateau.getCoordoneeCase(messageFromID);
            //en haut donc msg sur le coté
            if (coor[0] == 0) {

                //gauche
                if (coor[1] == plateau.getTaille() - 1) {
                    coor = getLocation(coor[1] - 1, coor[0]);
                    g.clearRect(coor[0] + 50 - message.length() * 5, coor[1] + 20, message.length() * 11, 20);
                    g.drawRoundRect(coor[0] + 50 - message.length() * 5, coor[1] + 20, message.length() * 11, 20, 3, 3);
                    g.setColor(Color.white);
                    g.fillPolygon(new int[]{coor[0] + 50 - message.length() * 5 + message.length() * 11, coor[0] + 50 - message.length() * 5 + message.length() * 11 + 30, coor[0] + 50 - message.length() * 5 + message.length() * 11 + 30, coor[0] + 50 - message.length() * 5 + message.length() * 11}, new int[]{coor[1] + 25, coor[1] + 30, coor[1] + 30, coor[1] + 35}, 4);
                    g.setColor(Color.black);
                    g.drawPolygon(new int[]{coor[0] + 50 - message.length() * 5 + message.length() * 11, coor[0] + 50 - message.length() * 5 + message.length() * 11 + 30, coor[0] + 50 - message.length() * 5 + message.length() * 11 + 30, coor[0] + 50 - message.length() * 5 + message.length() * 11}, new int[]{coor[1] + 25, coor[1] + 30, coor[1] + 30, coor[1] + 35}, 4);
                    g.setFont(new Font("Serif", Font.PLAIN, 20));
                    g.drawString(message, coor[0] + 55 - message.length() * 5, coor[1] + 37);

                }
                //droite
                else {
                    coor = getLocation(coor[1] + 1, coor[0]);
                    g.setColor(Color.white);
                    g.fillPolygon(new int[]{coor[0] - 70, coor[0] - 30, coor[0] - 30, coor[0] - 70}, new int[]{coor[1] + 30, coor[1] + 25, coor[1] + 35, coor[1] + 30}, 4);
                    g.clearRect(coor[0] - 30, coor[1] + 20, message.length() * 11, 20);
                    g.setColor(Color.black);
                    g.drawRoundRect(coor[0] - 30, coor[1] + 20, message.length() * 11, 20, 3, 3);
                    g.drawPolygon(new int[]{coor[0] - 70, coor[0] - 30, coor[0] - 30, coor[0] - 70}, new int[]{coor[1] + 30, coor[1] + 25, coor[1] + 35, coor[1] + 30}, 4);
                    g.setFont(new Font("Serif", Font.PLAIN, 20));
                    g.drawString(message, coor[0] - 28, coor[1] + 37);
                }
            }
            //msg en haut
            else {
                coor = getLocation(coor[1], coor[0]);
                g.clearRect(coor[0] + 50 - message.length() * 5, coor[1] - 40, message.length() * 11, 20);
                g.drawRoundRect(coor[0] + 50 - message.length() * 5, coor[1] - 40, message.length() * 11, 20, 3, 3);
                g.setColor(Color.white);
                g.fillPolygon(new int[]{coor[0] + 30, coor[0] + 25, coor[0] + 35, coor[0] + 30}, new int[]{coor[1], coor[1] - 20, coor[1] - 20, coor[1]}, 4);
                g.setColor(Color.black);
                g.drawPolygon(new int[]{coor[0] + 30, coor[0] + 25, coor[0] + 35, coor[0] + 30}, new int[]{coor[1], coor[1] - 20, coor[1] - 20, coor[1]}, 4);
                g.setFont(new Font("Serif", Font.PLAIN, 20));
                g.drawString(message, coor[0] + 55 - message.length() * 5, coor[1] - 22);
            }
            this.message = null;
            this.messageFromID = -1;
        }
    }

    /**
     * compute player poistion in Frame to draw player
     *
     * @param x player coordinate in chunk
     * @param y player coordinate in chunk
     * @return player position needed to draw in frame
     */
    private int[] getLocation(int x, int y) {
        return new int[]{casebordersize + bordersize + x * ((getWidth() - bordersize * 2) / taille), casebordersize / 2 + barsize + bordersize + y * ((getHeight() - barsize - bordersize * 2) / taille)};
    }
}
