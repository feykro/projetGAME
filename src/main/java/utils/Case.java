package utils;

import static utils.CaseState.*;

/**
 * manage Case
 */
public class Case {

    private CaseState etat;
    private int playerID;
    private Direction playerDirection;
    private String playerPseudo;
    //pour la partie graphique, lui associer une texture

    public Case() {
        resetOccupant();
    }


    /**
     * @return true if not free
     */
    public boolean isOccupied() {
        return etat != libre;
    }

    /**
     * occupy case with player and his metadata
     *
     * @param id        id player
     * @param pseudo    pseudo player
     * @param direction direction player
     * @return true if the case is not already occupied or reserved
     */
    public boolean occupe(int id, String pseudo, Direction direction) {
        if (etat == occupeeJoueur || etat == occupeeObstacle || (etat == reservee && id != playerID)) {
            return false;
        }
        playerID = id;
        playerPseudo = pseudo;
        playerDirection = direction;
        etat = occupeeJoueur;
        return true;
    }

    /**
     * reset metadata and free case
     */
    private void resetOccupant() {
        playerID = -1;
        playerPseudo = null;
        etat = libre;
    }

    /**
     * reserve case by player
     *
     * @param id player
     * @return true if the case is not already occupied or reserved
     */
    public boolean reserve(int id) {
        if (etat != libre) {
            return false;
        }
        playerID = id;
        etat = reservee;
        return true;
    }

    /**
     * occupe case with obstacle
     *
     * @return true if the case is not already occupied or reserved
     */
    public boolean setObstacle() {
        if (etat != libre) {
            return false;
        }
        etat = occupeeObstacle;
        return true;
    }

    /**
     * free the case occupe by player
     *
     * @return
     * @return false if the case is not occupied by player
     */
    public boolean free() {
        if (etat != occupeeJoueur) {
            return false;
        }
        resetOccupant();
        return true;
    }

    /* ======================================
    =       getter and setter methods       =
    ========================================= */

    /**
     * get state of case
     *
     * @return state
     */
    public CaseState getEtat() {
        return etat;
    }

    /**
     * get pseudo of the player present in the case
     * assert fail if the case is not occupe by player
     *
     * @return pseudo
     */
    public String getPlayerPseudo() {
        assert (etat == occupeeJoueur);
        return playerPseudo;
    }

    /**
     * get id of the player present in the case
     * assert fail if the case is not occupe by player
     *
     * @return id
     */
    public int getPlayerID() {
        assert (etat == occupeeJoueur);
        return playerID;
    }

    /**
     * get direction of player present in the case
     * assert fail if the case is not occupe by player
     *
     * @return direction
     */
    public Direction getPlayerDirection() {
        assert (etat == occupeeJoueur);
        return playerDirection;
    }

    /**
     * set direction of player present in the case
     * assert fail if the case is not occupe by player
     *
     * @param direction new direction
     */
    public void updatePlayerDirection(Direction direction) {
        assert (etat == occupeeJoueur);
        playerDirection = direction;
    }
}
