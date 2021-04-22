package utils;

import java.util.ArrayList;

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
    public boolean isValidMovement(int playerID, String Direction){
        int[] coor = findIDCaseCoor(playerID);
        int x = coor[0];
        int y = coor[1];
        if(Direction.equals("NORTH")){
            y -=1;
        }else if(Direction.equals("SOUTH")){
            y += 1;
        }else if(Direction.equals("WEST")){
            x -= 1;
        }else if(Direction.equals("EAST")){
            x += 1;
        }else{
            System.out.println("Direction error: not NORTH/SOUTH/EAST/WEST\n");
            return false;
        }
        //TODO: implémenter un id pour voir si le mouvement est valide
        //if(x == 6 && (id == 1) || (id == 2)) ...
        Case c = getCase(x, y);
        return !c.isOccupied();
    }

    /**
     * Checks if there's a player in an adjacent tile in Direction of the player
     * return the coordinate of that adjacent player, or -1 -1 if there isn't one
     */
    public int[] isValidTalk(int playerID, String Direction){
        int[] coor = findIDCaseCoor(playerID);
        int[] res = {-1, -1};
        int x = coor[0];
        int y = coor[1];
        if(Direction.equals("NORTH")){
            y -=1;
        }else if(Direction.equals("SOUTH")){
            y += 1;
        }else if(Direction.equals("WEST")){
            x -= 1;
        }else if(Direction.equals("EAST")){
            x += 1;
        }else{
            System.out.println("Direction error: not NORTH/SOUTH/EAST/WEST\n");
            return res;
        }

        if(x >= 5 || x<0 || y >= 5 || y<0){
            return res;
        }
        Case c = getCase(x, y);
        if(c.getEtat() == CaseState.occupeeJoueur){
            res[0] = x;
            res[1] = y;
        }
        return res;
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
                if(c.getPlayerID() == id){
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

    public void freeUserCase(int id){
        Case c = findIdCase(id);
        c.free();
    }

//=========- populate a case -============

    public boolean occupeCase(int x, int y,int id, String pseudo){
        if(x<taille && y < taille){
            getCase(x,y).occupe(id,pseudo);
            return true;
        }
        return false;
    }

    public void reserveCase(int x, int y,int id){
        Case c = getCase(x, y);
        c.reserve(id);
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

 //============- display -===============

    public void showChunk(){
        for(int i=0; i< taille ; i++){
            for(int j=0; j< taille ; j++) {
                System.out.print("--");
            }
            System.out.println("-");
            for(int j=0; j< taille ; j++) {
                System.out.print("|");
                if(getCase(i, j).isOccupied()){
                    System.out.print("웃");
                }
                else{
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

    /* ======================================
    =       getter and setter methods       =
    ========================================= */

    public Case getCase(int x, int y){
        return tab[x*taille + y];
    }

    public int getTaille() {
        return taille;
    }

    public Case[] getTab() {
        return tab;
    }

    public boolean setCase(int x, int y, Case c){
        if(x<taille && y < taille){
            tab[y*taille + x]=c;
            return true;
        }
        return false;
    }
}
