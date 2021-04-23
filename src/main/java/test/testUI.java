package test;

import UI.GraphiqueChunk;
import utils.Chunk;

public class testUI {
    public static void main(String[] args) {
        GraphiqueChunk ui = new GraphiqueChunk(5,null);
        Chunk chunk = new Chunk();
        chunk.loadRdmSeed();
        int coords[] =chunk.findFreeCase();
        chunk.occupeCase(coords[0],coords[1],0,"thomas");
        ui.drawChunk(chunk);
    }
}
