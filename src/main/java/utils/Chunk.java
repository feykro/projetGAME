package utils;

public class Chunk {
    private int taille;
    private Case[] tab;

    public Chunk(int taille){
        this.taille = taille;
        tab = new Case[this.taille*this.taille];
        tabInit();
    }

    public void tabInit(){
        for(int j=0; j<taille; j++){
            for(int i=0; i<taille; i++){
                Case c = new Case(false);
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
