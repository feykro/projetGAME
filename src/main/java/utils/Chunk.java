package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import static utils.Direction.getDirection;

public class Chunk {
    private int taille;
    private Case[] tab;
    private ArrayList<String> pseudoList;

    public Chunk(int taille){
        this.taille = taille;
        tab = new Case[this.taille*this.taille];
        tabInit();
    }

    public Chunk(){
        this.taille = 5;
        tab = new Case[this.taille*this.taille];
        tabInit();
    }

//==========- methods -============

    /**
     * Checks if the player's movement in Direction is valid. Does not check
     * if the player leaves the area or not. This should be handled by either
     * changing return type to int or include a chunk id in this Class
     */
    public boolean isValidMovement(int playerID, String direction){
        int[] coor=getNewCoor(playerID, direction);
        Case c = getCase(coor[0], coor[1]);
        if(c != null) {
            return !c.isOccupied();
        }
        else{
            return false;
        }
    }

    public int[] getNewCoor(int playerID, String direction){
        int[] coor = findIDCaseCoor(playerID);
        int x = coor[0];
        int y = coor[1];
        if(direction.equals("NORTH")){
            y -=1;
        }else if(direction.equals("SOUTH")){
            y += 1;
        }else if(direction.equals("WEST")){
            x -= 1;
        }else if(direction.equals("EAST")){
            x += 1;
        }else{
            System.out.println("Direction error: not NORTH/SOUTH/EAST/WEST\n");
            return null;
        }
        return new int[]{x,y};

    }

    /**
     * Checks if there's a player in an adjacent tile in Direction of the player
     * return the coordinate of that adjacent player, or -1 -1 if there isn't one
     */
    public int[] isValidTalk(int playerID, String direction){
        int[] coor = getNewCoor(playerID, direction);
        int x = coor[0];
        int y = coor[1];
        Case c = getCase(x, y);
        int res[] = new int[]{-1, -1};
        if(c != null && c.getEtat() == CaseState.occupeeJoueur) {
            res[0] = x;
            res[1] = y;
        }
        return res;
    }

    public int[] moveTo(int id,String direction){
        int[] coor = findIDCaseCoor(id);
        int x = coor[0];
        int y = coor[1];
        String pseudo = getCase(x,y).getPlayerPseudo();
        freeUserCase(id);
        if(direction.equals("NORTH")){
            y -=1;
        }else if(direction.equals("SOUTH")){
            y += 1;
        }else if(direction.equals("WEST")){
            x -= 1;
        }else if(direction.equals("EAST")){
            x += 1;
        }else{
            System.out.println("Direction error: not NORTH/SOUTH/EAST/WEST\n");
            return coor;
        }
        occupeCase(x,y,id,pseudo,getDirection(direction));
        return new int[]{x,y};
    }

    public String loadRdmSeed(){
        int seedNumber = (int)(Math.random() * ((11) + 1));
        String mapName = "seed"+seedNumber+".seed";
        File fileMap = new File("resources/mapSeed/"+mapName);
        try {
            Scanner reader = new Scanner(fileMap);
            for (int i =0; i < taille;i++) {
                String line = reader.nextLine();
                for(int j =0; j < taille ; j++){
                    assert(line.length()>= taille);
                    switch(line.charAt(j)){
                        case '1':
                            addObstacle(j,i);
                            break;
                        case '0':
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + line.charAt(j));
                    }
                }
            }
            reader.close();
        } catch (
                FileNotFoundException e) {
            System.out.println("Can not open Map seed");
            e.printStackTrace();
        }
        return mapName;
    }

//==========- find ... -===========

    public int[] findFreeCase(){
        int[] res = {-1, -1};
        for(int y=0; y<this.taille; y++){
            for(int x=0; x<this.taille; x++){
                Case current = getCase(x, y);
                if(!current.isOccupied()){
                    res[0] = x;
                    res[1] = y;
                    return res;
                }
            }
        }
        return res;
    }

    public Case findIdCase(int id){
        for(int y=0; y<taille; y++){
            for(int x=0; x<taille; x++){
                Case c = getCase(x, y);
                if(c.getEtat()==CaseState.occupeeJoueur && c.getPlayerID() == id){
                    return c;
                }
            }
        }
        return null;
    }

    public int[] findIDCaseCoor(int id){
        int[] res = {-1, -1};
        for(int y=0; y<taille; y++){
            for(int x=0; x<taille; x++){
                Case c = getCase(x, y);
                if(c.getPlayerID() == id){
                    res[0] = x;
                    res[1] = y;
                    return res;
                }
            }
        }
        return res;
    }

//==========- free ... -============

    public boolean freeCase(int x, int y){
        if(x<taille && y < taille){
            getCase(x,y).free();
            return true;
        }
        return false;
    }


    public boolean freeUserCase(int id){
        Case c = findIdCase(id);
        if(c == null){
            return false;
        }
        c.free();
        return true;
    }

//=========- populate a case -============

    public boolean occupeCase(int x, int y,int id, String pseudo,Direction direction){
        if(x<taille && y < taille){
            getCase(x,y).occupe(id,pseudo,direction);
            return true;
        }
        return false;
    }

    public void reserveCase(int x, int y,int id){
        Case c = getCase(x, y);
        c.reserve(id);
    }

    public boolean updateDirection(int id,Direction direction){
        Case c = findIdCase(id);
        if(c == null){
            return false;
        }
        c.updatePlayerDirection(direction);
        return true;
    }

    //===========- init -==================
    public void tabInit(){
        for(int j=0; j<taille; j++){
            for(int i=0; i<taille; i++){
                Case c = new Case();
                tab[j*taille + i] = c;
            }
        }
    }

    public void showChunk(){
        for(int i=0; i< taille ; i++){
            for(int j=0; j< taille ; j++) {
                System.out.print("--");
            }
            System.out.println("-");
            for(int j=0; j< taille ; j++) {
                System.out.print("|");
                switch(getCase(j,i).getEtat()) {
                    case occupeeJoueur:
                            System.out.print("웃");
                        break;
                    case occupeeObstacle:
                            System.out.print("¤");
                        break;
                    default:
                        System.out.print(" ");
                }

            }
            System.out.println("|");
        }
        for(int j=0; j< taille ; j++) {
            System.out.print("--");
        }
        System.out.println("-");
    }

    public boolean setCase(int x, int y, Case c){
        if(x<taille && y < taille){
            tab[y*taille + x]=c;
            return true;
        }
        return false;
    }

    public void addObstacle(int x, int y){
        Case c = getCase(x, y);
        c.setObstacle();
    }

    public int[] getCoordoneeCase(int id){
        for(int i =0 ; i < taille*taille;i++){
            if(tab[i].getEtat()==CaseState.occupeeJoueur && tab[i].getPlayerID()== id){
                return new int[]{i/taille,i%taille};
            }
        }
        return null;
    }


    public Case getCase(int x, int y){
        if(x>=taille || x<0 || y>=taille || y<0){
            return null;
        }
        return tab[y*taille + x];
    }

    /* ======================================
    =       getter and setter methods       =
    ========================================= */

    public int getTaille() {
        return taille;
    }

    public Case[] getTab() {
        return tab;
    }

    public String getInfochunk(int playerID){
        String info = "";
        int cmt = 0;
        for(int x=0; x<this.taille; x++){
            for(int y=0; y<this.taille; y++){
                Case c = getCase(x, y);
                if(c.getEtat() == CaseState.occupeeJoueur && c.getPlayerID() != playerID){
                    String id = Integer.toString(c.getPlayerID()) + " ";
                    String pseudo = c.getPlayerPseudo() + " ";
                    info = info + " " + id + pseudo + Integer.toString(x) + " " + Integer.toString(y) + " " + c.getPlayerDirection();
                    cmt++;
                }else if(c.getEtat() == CaseState.occupeeObstacle){
                    String id = "-1 ";
                    String pseudo = "obstacle ";
                    info = info + " " + id + pseudo + Integer.toString(x) + " " + Integer.toString(y)+ " SOUTH";
                    cmt++;
                }
            }
        }
        return MessageType.info_chunk + " " + Integer.toString(cmt) +info;
    }
}
