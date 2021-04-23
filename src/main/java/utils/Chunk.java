package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Chunk {
    private int taille;
    private Case[] tab;
    private ArrayList<String> pseudoList;

    public Chunk(){
        this.taille = 5;
        tab = new Case[this.taille*this.taille];
        tabInit();
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

    public boolean setCase(int x, int y, Case c){
        if(x<taille && y < taille){
            tab[y*taille + x]=c;
            return true;
        }
        return false;
    }

    public boolean occupeCase(int x, int y,int id, String pseudo){
        if(x<taille && y < taille){
            getCase(x,y).occupe(id,pseudo);
            return true;
        }
        return false;
    }

    public boolean freeCase(int x, int y){
        if(x<taille && y < taille){
            getCase(x,y).free();
            return true;
        }
        return false;
    }

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

    public void reserveCase(int x, int y,int id){
        Case c = getCase(x, y);
        c.reserve(id);
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
        return tab[x*taille + y];
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
}
