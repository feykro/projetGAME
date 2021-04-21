package test;

import utils.Case;
import utils.Chunk;

public class testChunk {

    public static void main(String[] args) {
        Chunk plateau = new Chunk(5);
        plateau.setCase(2,3,new Case(true));
        plateau.showChunk();
        return;
    }
}
