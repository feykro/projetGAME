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

    public void tabInit(){
        for(int j=0; j<taille; j++){
            for(int i=0; i<taille; i++){
                Case c = new Case();
                tab[j*taille + i] = c;
            }
        }
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
