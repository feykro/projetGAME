package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static utils.Direction.getDirection;

/**
 * Manage chunk function
 */
public class Chunk {
    private final int taille;
    private final Case[] tab;

    public Chunk() {
        this.taille = 5;
        tab = new Case[this.taille * this.taille];
        tabInit();
    }

//==========- methods -============

    /**
     * Checks if the player's movement in Direction is valid. Does not check
     * if the player leaves the area or not. This should be handled by either
     * changing return type to int or include a chunk id in this Class
     *
     * @param playerID  player who would move
     * @param direction the direction of movement
     * @return if the player can move or not
     */
    public boolean isValidMovement(int playerID, String direction) {
        int[] coor = getNewCoor(playerID, direction);
        Case c = getCase(coor[0], coor[1]);
        if (c != null) {
            return !c.isOccupied();
        } else {
            return false;
        }
    }

    /**
     * get the coordinate if the player move to one case in the direction
     *
     * @param playerID  player moving
     * @param direction direction of movement
     * @return new coordinate
     */
    public int[] getNewCoor(int playerID, String direction) {
        int[] coor = findIDCaseCoor(playerID);
        int x = coor[0];
        int y = coor[1];
        switch (direction) {
            case "NORTH" -> y -= 1;
            case "SOUTH" -> y += 1;
            case "WEST" -> x -= 1;
            case "EAST" -> x += 1;
            default -> {
                System.out.println("Direction error: not NORTH/SOUTH/EAST/WEST\n");
                return null;
            }
        }
        return new int[]{x, y};

    }

    /**
     * Checks if there's a player in an adjacent tile in Direction of the player
     * return the coordinate of that adjacent player, or -1 -1 if there isn't one
     *
     * @param playerID  player who would talk
     * @param direction the direction talk
     * @return coordinate of the player to talk or (-1;-1) if player can't talk
     */
    public int[] isValidTalk(int playerID, String direction) {
        int[] coor = getNewCoor(playerID, direction);
        int x = coor[0];
        int y = coor[1];
        Case c = getCase(x, y);
        int[] res = new int[]{-1, -1};
        if (c != null && c.getEtat() == CaseState.occupeeJoueur) {
            res[0] = x;
            res[1] = y;
        }
        return res;
    }

    /**
     * compute the new coordinate of the player who move at one direction
     * need to call isValidMovement before to check if the movement is valid
     *
     * @param id        player who would move
     * @param direction the direction of movement
     * @return new coordinate
     */
    public int[] moveTo(int id, String direction) {
        int[] coor = findIDCaseCoor(id);
        int x = coor[0];
        int y = coor[1];
        String pseudo = getCase(x, y).getPlayerPseudo();
        freeUserCase(id);
        switch (direction) {
            case "NORTH" -> y -= 1;
            case "SOUTH" -> y += 1;
            case "WEST" -> x -= 1;
            case "EAST" -> x += 1;
            default -> {
                System.out.println("Direction error: not NORTH/SOUTH/EAST/WEST\n");
                return coor;
            }
        }
        occupeCase(x, y, id, pseudo, getDirection(direction));
        return new int[]{x, y};
    }

    /**
     * load a random seed to add obstacle in the chunk
     *
     * @return seed name
     */
    public String loadRdmSeed() {
        int seedNumber = (int) (Math.random() * ((11) + 1));
        String mapName = "seed" + seedNumber + ".seed";
        File fileMap = new File("resources/mapSeed/" + mapName);
        try {
            Scanner reader = new Scanner(fileMap);
            for (int i = 0; i < taille; i++) {
                String line = reader.nextLine();
                for (int j = 0; j < taille; j++) {
                    assert (line.length() >= taille);
                    switch (line.charAt(j)) {
                        case '1':
                            addObstacle(j, i);
                            break;
                        case '0':
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + line.charAt(j));
                    }
                }
            }
            reader.close();
        } catch (
                FileNotFoundException e) {
            System.out.println("Can not open Map seed");
            e.printStackTrace();
        }
        return mapName;
    }

//==========- find ... -===========

    /**
     * get coordonate of free case in the chunk
     *
     * @return coordonate of free case or (-1;-1) if all case are full
     */
    public int[] findFreeCase() {
        int[] res = {-1, -1};
        for (int y = 0; y < this.taille; y++) {
            for (int x = 0; x < this.taille; x++) {
                Case current = getCase(x, y);
                if (!current.isOccupied()) {
                    res[0] = x;
                    res[1] = y;
                    return res;
                }
            }
        }
        return res;
    }

    /**
     * get coordinate of player in the chunk
     *
     * @param id id of the player
     * @return Case of the player if is in the chunk or null
     */
    public Case findIdCase(int id) {
        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                Case c = getCase(x, y);
                if (c.getEtat() == CaseState.occupeeJoueur && c.getPlayerID() == id) {
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * get coordinate of player in the chunk
     *
     * @param id id of the player
     * @return coordinate of the player if is in the chunk or (-1;-1)
     */
    public int[] findIDCaseCoor(int id) {
        int[] res = {-1, -1};
        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                Case c = getCase(x, y);
                if (c.getPlayerID() == id) {
                    res[0] = x;
                    res[1] = y;
                    return res;
                }
            }
        }
        return res;
    }

//==========- free ... -============

    /**
     * free the case at coordinate
     *
     * @param x Case coordinate
     * @param y case coordinate
     * @return true if the case exist
     */
    public boolean freeCase(int x, int y) {
        Case caseToFree = getCase(x, y);
        if (caseToFree != null) {
            caseToFree.free();
            return true;
        }
        return false;
    }

    /**
     * free the case whith the player
     *
     * @param id id of the player
     * @return true if the Case exist
     */
    public boolean freeUserCase(int id) {
        Case c = findIdCase(id);
        if (c == null) {
            return false;
        }
        c.free();
        return true;
    }

//=========- populate a case -============

    /**
     * occupe the case with the player and his metadata
     *
     * @param x         Case coordinate
     * @param y         case coordinate
     * @param id        player id
     * @param pseudo    player pseudo
     * @param direction player direction
     * @return true if the Case exist
     */
    public boolean occupeCase(int x, int y, int id, String pseudo, Direction direction) {
        Case caseToOccupe = getCase(x, y);
        if (caseToOccupe != null) {
            caseToOccupe.occupe(id, pseudo, direction);
            return true;
        }
        return false;
    }

    /**
     * reserve the case with the player
     *
     * @param x  Case coordinate
     * @param y  case coordinate
     * @param id player id
     * @return true if the Case exist
     */
    public boolean reserveCase(int x, int y, int id) {
        Case c = getCase(x, y);
        if (c != null) {
            c.reserve(id);
            return true;
        }
        return false;
    }

    /**
     * update the direction of the player
     *
     * @param id        player id
     * @param direction new direction of the player
     * @return true if the player is in the chunk
     */
    public boolean updateDirection(int id, Direction direction) {
        Case c = findIdCase(id);
        if (c == null) {
            return false;
        }
        c.updatePlayerDirection(direction);
        return true;
    }

    //===========- init -==================

    /**
     * initialise the tab who save the chunk
     */
    public void tabInit() {
        for (int j = 0; j < taille; j++) {
            for (int i = 0; i < taille; i++) {
                Case c = new Case();
                tab[j * taille + i] = c;
            }
        }
    }

    /**
     * show the chunk in the terminal
     * '¤'  obstacle case
     * ' '  empty case
     * '*'  reserved case
     * '웃' player case
     */
    public void showChunk() {
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                System.out.print("--");
            }
            System.out.println("-");
            for (int j = 0; j < taille; j++) {
                System.out.print("|");
                switch (getCase(j, i).getEtat()) {
                    case occupeeJoueur -> System.out.print("웃");
                    case occupeeObstacle -> System.out.print("¤");
                    case reservee -> System.out.print("*");
                    default -> System.out.print(" ");
                }

            }
            System.out.println("|");
        }
        for (int j = 0; j < taille; j++) {
            System.out.print("--");
        }
        System.out.println("-");
    }

    /**
     * add an obstacle in the chunk at the coordinate
     *
     * @param x coordinate
     * @param y coordinate
     * @return true if the Case exist
     */
    public boolean addObstacle(int x, int y) {
        Case c = getCase(x, y);
        if (c == null) {
            return false;
        }
        c.setObstacle();
        return true;
    }



    /* ======================================
    =       getter and setter methods       =
    ========================================= */

    /**
     * get size of chunk
     *
     * @return size of chunk
     */
    public int getTaille() {
        return taille;
    }

    /**
     * get case at coordinate
     *
     * @param x coordinate
     * @param y coordinate
     * @return Case if exist else null
     */
    public Case getCase(int x, int y) {
        if (x >= taille || x < 0 || y >= taille || y < 0) {
            return null;
        }
        return tab[y * taille + x];
    }

    /**
     * build the message info_chunk based on the chunk without player in parameter
     *
     * @param playerID id player
     * @return string message
     */
    public String getInfochunk(int playerID) {
        StringBuilder info = new StringBuilder();
        int cmt = 0;
        for (int x = 0; x < this.taille; x++) {
            for (int y = 0; y < this.taille; y++) {
                Case c = getCase(x, y);
                if (c.getEtat() == CaseState.occupeeJoueur && c.getPlayerID() != playerID) {
                    String id = c.getPlayerID() + " ";
                    String pseudo = c.getPlayerPseudo() + " ";
                    info.append(" ").append(id).append(pseudo).append(x).append(" ").append(y).append(" ").append(c.getPlayerDirection());
                    cmt++;
                } else if (c.getEtat() == CaseState.occupeeObstacle) {
                    String id = "-1 ";
                    String pseudo = "obstacle ";
                    info.append(" ").append(id).append(pseudo).append(x).append(" ").append(y).append(" SOUTH");
                    cmt++;
                }
            }
        }
        return MessageType.info_chunk + " " + cmt + info;
    }
}
