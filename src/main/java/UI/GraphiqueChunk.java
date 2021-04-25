package UI;

import client.Player;
import utils.Case;
import utils.CaseState;
import utils.Chunk;
import utils.Direction;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import javax.swing.JFrame;

import static utils.CaseState.*;

public class GraphiqueChunk extends JFrame{
    private int taille;
    private int casebordersize;
    private int bordersize = 20;
    private int barsize = 30;

    private Player player;
    private Chunk plateau;

    private Image imgDOWN = Toolkit.getDefaultToolkit().getImage("resources/images/personnage_DOWN.png");
    private Image imgTOP = Toolkit.getDefaultToolkit().getImage("resources/images/personnage_TOP.png");
    private Image imgLEFT = Toolkit.getDefaultToolkit().getImage("resources/images/personnage_LEFT.png");
    private Image imgRIGHT = Toolkit.getDefaultToolkit().getImage("resources/images/personnage_RIGHT.png");
    private Image imgStone = Toolkit.getDefaultToolkit().getImage("resources/images/cayou.png");
    private Image imgCross = Toolkit.getDefaultToolkit().getImage("resources/images/lacroix.png");
    private Image imgGrass;



    private HashMap<String,int[]> mapPlayer;
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

    private int addCloseListener(){
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                player.requestLeaveGame();
                System.exit(0);
            }
        });
        return 0;
    }

    public void drawChunk(Chunk chunk) {
        plateau = chunk;
        repaint();
    }

    public void paint(Graphics g){
        g.clearRect(0,0,900,900);
        imgGrass = Toolkit.getDefaultToolkit().getImage("resources/images/grass"+player.getCurrentChunkNumber()+".png");
        if(plateau!=null) {
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
                            drawPlayer(g,x,y);
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

    }

    private void drawPlayer(Graphics g,int x ,int y){
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
        g.clearRect(locationIMG[0] + 50- pseudo.length()*5, locationIMG[1] + 10, pseudo.length() * 12, 20);
        g.setFont(new Font("Serif", Font.PLAIN, 20));
        g.drawString(pseudo, locationIMG[0] + 50 - pseudo.length()*5, locationIMG[1] + 25);

    }


    private void drawGrille(Graphics g){
        for(int width = 0;width< taille;width++) {
            for (int height = 0; height < taille; height++) {
                g.drawRect(bordersize+width*((getWidth()-bordersize*2)/taille),barsize+bordersize+height*((getHeight()-barsize-bordersize*2)/taille),(getWidth()-bordersize*2)/taille,((getHeight()-bordersize*2 - barsize)/taille));
            }
        }
    }

    private int[] getLocation(int x ,int y){
        return new int[]{casebordersize+bordersize + x * ((getWidth() - bordersize * 2) / taille), casebordersize/2 + barsize + bordersize + y * ((getHeight() - barsize - bordersize * 2) / taille)};
    }

    private void initListener(){
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(player != null) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_DOWN:
                            player.requestMove(Direction.SOUTH);
                            break;
                        case KeyEvent.VK_UP:
                            player.requestMove(Direction.NORTH);
                            break;
                        case KeyEvent.VK_RIGHT:
                            player.requestMove(Direction.EAST);
                            break;
                        case KeyEvent.VK_LEFT:
                            player.requestMove(Direction.WEST);
                            break;
                    }
                }
            }
        });
    }
}
