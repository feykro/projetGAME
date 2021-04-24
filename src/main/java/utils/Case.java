package utils;

import static utils.CaseState.*;

public class Case {

    private CaseState etat;
    private int playerID;
    private Direction playerDirection;
    private String playerPseudo;
    //pour la partie graphique, lui associer une texture

    public Case(){
        resetOccupant();
    }

    /* ======================================
    =       getter and setter methods       =
    ========================================= */


    public boolean isOccupied() {
        return etat != libre;
    }


    public boolean occupe(int id,String pseudo,Direction direction) {
        if(etat == occupeeJoueur || etat == occupeeObstacle || (etat == reservee && id != playerID)){
            return false;
        }
        playerID=id;
        playerPseudo=pseudo;
        playerDirection=direction;
        etat = occupeeJoueur;
        return true;
    }

    private void resetOccupant(){
        playerID=-1;
        playerPseudo=null;
        etat = libre;
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
        if(etat != occupeeJoueur){
            return false;
        }
        resetOccupant();
        return true;
    }

    public CaseState getEtat() {
        return etat;
    }

    public String getPlayerPseudo(){
        assert(etat == occupeeJoueur);
        return playerPseudo;
    }
    public int getPlayerID(){
        assert(etat == occupeeJoueur);
        return playerID;
    }
    public Direction getPlayerDirection(){
        assert(etat == occupeeJoueur);
        return playerDirection;
    }

    public void updatePlayerDirection(Direction direction){
        assert(etat == occupeeJoueur);
        playerDirection = direction;
    }
}
