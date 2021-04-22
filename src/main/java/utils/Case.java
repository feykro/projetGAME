package utils;

enum CaseState{
    libre,
    reservee,
    occupeeJoueur,
    occupeeObstacle
};

public class Case {
    private boolean isOccupied;
    private CaseState etat;
    private String playerPseudo;
    //pour la partie graphique, lui associer une texture

    public Case(boolean occ){
        playerPseudo=null;
        isOccupied = occ;
        etat = CaseState.libre;
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

    public void setEtat(CaseState newEtat){
        this.etat = newEtat;
        switch (newEtat){
            case reservee:
            case occupeeJoueur:
            case occupeeObstacle:
                this.isOccupied = true;
                break;
            default:
                this.isOccupied = false;
                break;
        }
    }


}
