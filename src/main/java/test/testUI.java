package test;

import UI.GraphiqueChunk;
import utils.Chunk;

public class testUI {
    public static void main(String[] args) {
        GraphiqueChunk ui = new GraphiqueChunk(5,null);
        Chunk chunk = new Chunk();
        chunk.occupeCase(2,4,0,"thomas");
        chunk.reserveCase(2,3,0);
        chunk.addObstacle(0,0);
        ui.drawChunk(chunk);
    }
}
