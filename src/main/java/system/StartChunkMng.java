package system;

import utils.Chunk;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class StartChunkMng {
    public static void main(String[] args) {
        try {
            Chunk k = new Chunk();
            k.loadRdmSeed();
            new ChunkManager(k, Integer.parseInt(args[0]));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
