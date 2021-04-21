package utils;

public class Case {
    private boolean isOccupied;
    //pour la partie graphique, lui associer une texture

    public Case(boolean occ){
        isOccupied = occ;
    }

    /* ======================================
    =       getter and setter methods       =
    ========================================= */


    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }
}
