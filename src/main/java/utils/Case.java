package utils;

enum CaseState{
    libre,
    reservee,
    occupeeJoueur,
    occupeeObstacle
};

public class Case {
    private CaseState etat;
    private int playerID;
    private String playerPseudo;
    //pour la partie graphique, lui associer une texture

    public Case(){
        playerPseudo=null;
        playerID=-1;
        etat = CaseState.libre;
    }

    /* ======================================
    =       getter and setter methods       =
    ========================================= */


    public boolean isOccupied() {
        return etat != CaseState.libre;
    }

    public String getPlayerPseudo(){
        assert(etat == CaseState.occupeeJoueur);
        return playerPseudo;
    }
    public int getPlayerID(){
        assert(etat == CaseState.occupeeJoueur);
        return playerID;
    }

    public boolean occupe(int id,String pseudo) {
        if(etat == CaseState.occupeeJoueur || etat == CaseState.occupeeObstacle || (etat == CaseState.reservee && id != playerID)){
            return false;
        }
        playerID=id;
        playerPseudo=pseudo;
        etat = CaseState.occupeeJoueur;
        return true;
    }

    public boolean reserve(int id) {
        if(etat == CaseState.libre){
            return false;
        }
        playerID=id;
        etat = CaseState.occupeeJoueur;
        return true;
    }

    public boolean free() {
        if(etat == CaseState.occupeeJoueur){
            return false;
        }
        playerID=-1;
        playerPseudo=null;
        etat = CaseState.libre;
        return true;
    }

    public CaseState getEtat() {
        return etat;
    }
}
