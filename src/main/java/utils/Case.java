package utils;

import static utils.CaseState.*;

;

public class Case {

    private CaseState etat;
    private int playerID;
    private String playerPseudo;
    //pour la partie graphique, lui associer une texture

    public Case(){
        playerPseudo=null;
        playerID=-1;
        etat = libre;
    }

    /* ======================================
    =       getter and setter methods       =
    ========================================= */


    public boolean isOccupied() {
        return etat != libre;
    }

    public String getPlayerPseudo(){
        assert(etat == occupeeJoueur);
        return playerPseudo;
    }
    public int getPlayerID(){
        assert(etat == occupeeJoueur);
        return playerID;
    }

    public boolean occupe(int id,String pseudo) {
        if(etat == occupeeJoueur || etat == occupeeObstacle || (etat == reservee && id != playerID)){
            return false;
        }
        playerID=id;
        playerPseudo=pseudo;
        etat = occupeeJoueur;
        return true;
    }

    public boolean reserve(int id) {
        if(etat != libre){
            return false;
        }
        playerID=id;
        etat = reservee;
        return true;
    }

    public boolean setObstacle(){
        if(etat != libre){
            return false;
        }
        etat = occupeeObstacle;
        return true;
    }

    public boolean free() {
        if(etat == occupeeJoueur){
            return false;
        }
        playerID=-1;
        playerPseudo=null;
        etat = libre;
        return true;
    }

    public CaseState getEtat() {
        return etat;
    }
}
