public class Case {
    private int x;
    private int y;
    private boolean isOccupied;
    //pour la partie graphique, lui associer une texture

    public Case(int x, int y){
        this.x = x;
        this.y = y;
        isOccupied = false;
    }

    /* ======================================
    =       getter and setter methods       =
    ========================================= */

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }
}
