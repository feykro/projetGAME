package utils;

public class Case {
    private boolean isOccupied;
    private String playerPseudo;
    //pour la partie graphique, lui associer une texture

    public Case(boolean occ){
        playerPseudo=null;
        isOccupied = occ;
    }

    /* ======================================
    =       getter and setter methods       =
    ========================================= */


    public boolean isOccupied() {
        return isOccupied;
    }
    public String getPlayerPseudo(){return playerPseudo;}
    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
        playerPseudo=null;
    }
    public void occupe(String pseudo) {
        isOccupied = true;
        playerPseudo=pseudo;
    }


}
