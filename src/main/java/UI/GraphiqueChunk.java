package UI;

import client.Player;
import utils.Chunk;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.JFrame;

public class GraphiqueChunk extends JFrame{
    private int taille;
    private int casebordersize;
    private int bordersize = 20;
    private int barsize = 30;
    private Player player;

    private Image imgDOWN = Toolkit.getDefaultToolkit().getImage("resources/personnage_DOWN.png");
    private Image imgTOP = Toolkit.getDefaultToolkit().getImage("resources/personnage_TOP.png");
    private Image imgLEFT = Toolkit.getDefaultToolkit().getImage("resources/personnage_LEFT.png");
    private Image imgRIGHT = Toolkit.getDefaultToolkit().getImage("resources/personnage_RIGHT.png");

    public GraphiqueChunk(int taille, Player player) {
        this.player = player;
        this.taille = taille;
        casebordersize = 32;
        setSize(900, 900);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void drawChunk(Chunk chunk) {

        repaint();
    }

    public void paint(Graphics g){
        drawGrille(g);
        for(int x = 0;x< taille;x++) {
            for (int y = 0; y < taille; y++) {
                int locationIMG[] = getLocation(x, y);
                g.drawImage(imgDOWN, locationIMG[0], locationIMG[1], this);
            }
        }
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
}
