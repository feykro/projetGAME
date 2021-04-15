public class Chunk {
    private int taille;
    private Case[] tab;
    private int identifiant;

    public Chunk(int taille, int id){
        this.taille = taille;
        this.identifiant = id;
        tab = new Case[this.taille];


    }

    public void tabInit(){
        for(int j=0; j<taille; j++){
            for(int i=0; i<taille; i++){
                Case c = new Case(i, j);
                tab[j*taille + i] = c;
            }
        }
    }

    public Case getCase(int x, int y){
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

    public int getIdentifiant() {
        return identifiant;
    }
}
