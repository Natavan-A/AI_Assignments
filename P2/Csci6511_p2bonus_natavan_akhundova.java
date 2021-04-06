import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Block --- a class for encapsulating 4x4 block information.
 */
class Block{
    private int assigned = -1;              // a tile assigned to the block, default -1
    private int posX = -1;                  // a starting position of the block on the x line in the board, default -1
    private int posY = -1;                  // a starting position of the block on the y line in the board, default -1
    private ArrayList<Integer> bushes;      // a list of marked bushes in the block

    /**
     * Constructor function for initializing variables
     * @param posX - a starting position of the block on the x line in the board
     * @param posY - a starting position of the block on the y line in the board
     */
    Block(int posX, int posY){
        bushes = new ArrayList<>();
        this.posX = posX;
        this.posY = posY;
    }

    /**
     * Getter and setter functions for class variables
     */

    public int getAssigned(){
        return assigned;
    }

    public void setAssigned(int a){
        assigned = a;
    }

    public int getPosX(){
        return posX;
    }

    public int getPosY(){
        return posY;
    }

    public ArrayList<Integer> getBushes(){
        return bushes;
    }

    public void addBush(int color){ bushes.add(color); }

}
/**
 * Csci6511_p2bonus_natavan_akhundova --- a program to solve CSP.
 */

public class Csci6511_p2bonus_natavan_akhundova {
    static Map<String, Block> blocks;                       // storage for all blocks in the board
    static Queue<String> blockIds;                          // storage for all block IDs in the board (queue)
    static Queue<int[]> arcs;                               // storage for all arcs/edges in CSP
    static Map<Integer, ArrayList<Integer>> removedColors;  // storage for removed colors during AC3 Checking
    static Map<Integer, Integer> targets;                   // storage for targets for bushes in a specific color
    static Map<Integer, Integer> tiles;                     // storage for available tiles in the CSP
    final static int EL_SHAPE = 1;                          // the index of the L shaped tiles
    final static int OUTER_BOUNDARY = 2;                    // the index of the outer boundary shaped tiles
    final static int FULL_BLOCK = 3;                        // the index of the full block shaped tiles

    /**
     * Checks if assignment of the tile to the block is safe considering targets of CSP
     * @param tile - the assigned tile to the block
     * @param bushes - 4x4 colored values of the block
     * @return boolean value
     */
    public static boolean isSafe(Integer tile, ArrayList<Integer> bushes){
        // if any of the tiles are placed more than needed, it is not safe
        if (tiles.get(tile)-1 < 0) return false;

        // if any of the targets are visible more than needed, it is not safe
        if (tile == EL_SHAPE){
            if (!coverWithELShape(bushes)) return false;
        }
        else if (tile == OUTER_BOUNDARY){
            if (!coverWithOuterBoundary(bushes)) return false;
        }
        else{
            if (!coverWithFullBlock(bushes)) return false;
        }

        // otherwise, safe
        return true;
    }

    /**
     * Covers the block with the EL Shape and changes targets accordingly, if safe
     * @param bushes - bushes of the block
     * @return
     */
    public static boolean coverWithELShape(ArrayList<Integer> bushes){
            boolean isSafe = true;                     // boolean for checking safety
            int i = 0;

            // Check if any target is visible more than needed
            for (;i < bushes.size(); i++){
                if (bushes.get(i) == 0) continue;
                targets.put(bushes.get(i), targets.get(bushes.get(i))-1);
                if (targets.get(bushes.get(i)) < 0){
                    isSafe = false;
                    break;
                }
            }

            // if so, return everything back and declare non-safety
            if (!isSafe) {
                for (; i >= 0; i--) {
                    if (bushes.get(i) == 0) continue;
                    targets.put(bushes.get(i), targets.get(bushes.get(i))+1);
                }
            }
            else tiles.put(1, tiles.get(1)-1); // decrease available EL Shapes

            // return a boolean whether it is safe to use the tile
            return isSafe;
    }

    /**
     * Uncovers the block with the EL Shape and changes targets accordingly
     * @param bushes - bushes of the block
     * @return
     */
    public static void uncoverWithELShape(ArrayList<Integer> bushes){

        // return everything back
        for (int i = 0; i < bushes.size(); i++) {
            if (bushes.get(i) == 0) continue;
            targets.put(bushes.get(i), targets.get(bushes.get(i))+1);
        }
        tiles.put(1, tiles.get(1)+1); // increase available EL Shapes
    }

    /**
     * Covers the block with the Outer Boundary and changes targets accordingly, if safe
     * @param bushes - bushes of the block
     * @return
     */
    public static boolean coverWithOuterBoundary(ArrayList<Integer> bushes){
        boolean isSafe = true;                   // boolean for checking safety
        int[] indexes = {0,1,3,4};       // indices that should be considered with this tile
        int i = 0;

        // Check if any target is visible more than needed
        for (;i < indexes.length; i++){
            int index = indexes[i];
            if (bushes.get(index) == 0) continue;
            targets.put(bushes.get(index), targets.get(bushes.get(index))-1);
            if (targets.get(bushes.get(index)) < 0){
                isSafe = false;
                break;
            }
        }

        // if so, return everything back and declare non-safety
        if (!isSafe) {
            for (; i >= 0; i--) {
                int index = indexes[i];
                if (bushes.get(index) == 0) continue;
                targets.put(bushes.get(index), targets.get(bushes.get(index))+1);
            }
        }
        else tiles.put(2, tiles.get(2)-1); // decrease available Outer Boundary

        // return a boolean whether it is safe to use the tile
        return isSafe;
    }

    /**
     * Uncovers the block with the Outer Boundary and changes targets accordingly
     * @param bushes - bushes of the block
     * @return
     */
    public static void uncoverWithOuterBoundary(ArrayList<Integer> bushes){
        int[] indexes = {0,1,3,4}; // indices that should be considered with this tile

        // return everything back
        for (int i = 0; i < indexes.length; i++) {
            int index = indexes[i];
            if (bushes.get(index) == 0) continue;
            targets.put(bushes.get(index), targets.get(bushes.get(index))+1);
        }
        tiles.put(2, tiles.get(2)+1); // increase available Outer Boundary
    }

    /**
     * Covers the block with the Full Block and changes targets accordingly, if safe
     * @param bushes - bushes of the block
     * @return
     */
    public static boolean coverWithFullBlock(ArrayList<Integer> bushes){
        boolean isSafe = true;           // boolean for checking safety
        tiles.put(3, tiles.get(3)-1);   // decrease available Full Blocks

        // return a boolean whether it is safe to use the tile
        return isSafe;
    }

    /**
     * Uncovers the block with the Full Block and changes targets accordingly, if safe
     * @param bushes - bushes of the block
     * @return
     */
    public static void uncoverWithFullBlock(ArrayList<Integer> bushes){
        tiles.put(3, tiles.get(3)+1); // increase available Full Blocks
    }

    /**
     * Restores CSP to the state where colors were not deleted from domains
     * @param block - currently processing block object
     * @param tile - the assigned tile to the block
     */
    public static void restorePreviousState(Block block, int tile){
        // remove assignment of the tile to the block
        block.setAssigned(-1);

        // return the targets to their previous values
        ArrayList<Integer> bushes = block.getBushes();
        if (tile == EL_SHAPE) uncoverWithELShape(bushes);
        else if (tile == OUTER_BOUNDARY) uncoverWithOuterBoundary(bushes);
        else uncoverWithFullBlock(bushes);
    }

        /**
         * Colors the provided vertex and its adjacent vertices recursively checking FC and AC3
         * @return boolean
         */
        public static boolean coverBoardRecursive(String blockId){
            // if there is no left block to cover
            if (blockId.equals("-1"))
                return true;

            Block block = blocks.get(blockId); // get current block by Id

            // find a tile in available tiles for the block that is safe to assign
            for (int tile = 1; tile <= 3; tile++){
                if(isSafe(tile, block.getBushes())){
                    boolean satisfied = true;
                    block.setAssigned(tile); // assign the current tile to the current block

                    String nextBlockId = "-1";
                    if (blockIds.size() != 0) nextBlockId = blockIds.remove();
                    else {
                        Set<Integer> keys = targets.keySet();
                        for (Integer key : keys) {
                            // if target has not been reached
                            if (targets.get(key) != 0) {
                                satisfied = false;
                            }
                        }
                    }

                    if (satisfied && coverBoardRecursive(nextBlockId)) return true; // recursively cover other blocks
                    if (!nextBlockId.equals("-1"))
                        blockIds.add(nextBlockId);
                    restorePreviousState(block, tile); // restore the previous state if the color assignment lead to no solution
                }
            }
            // no tile has been assigned to the block
            return false;
        }

        /**
         * Calls the recursive function of covering the board and prints the result
         */
        public static void coverBoard(){
            String blockId = blockIds.remove(); // pop one blockID to cover

            // find a solution for the CSP
            boolean solution = coverBoardRecursive(blockId);

            // printing the result
            if (!solution) System.out.println("Solution does not exist.");
            else{
                System.out.println("Solution exists:");

                SortedSet<String> keys = new TreeSet<>(blocks.keySet());
                int i = 0;
                for (String key : keys) {
                    int tile = blocks.get(key).getAssigned();
                    String tileName = "";
                    if (tile == EL_SHAPE) tileName = "EL_SHAPE";
                    else if (tile == OUTER_BOUNDARY) tileName = "OUTER_BOUNDARY";
                    else if (tile == FULL_BLOCK) tileName = "FULL_BLOCK";
                    System.out.printf("%d 4 %s\n", i, tileName);
                    i++;
                }

//                Set<Integer> keys2 = targets.keySet();
//                for (Integer key : keys2) System.out.println(key+" "+targets.get(key));
//
//                keys2 = tiles.keySet();
//                for (Integer key : keys2) System.out.println(key+" "+tiles.get(key));
            }
        }

    /**
     * Stores bushes from a line to blocks and blocks to the block collection
     * @param line - a read line from the board
     * @param posY - the number of the read line
     */
    public static void storeBoardLine(String line, int posY){
            // Store marked bushes to blocks
            line = line.replace("  ", " 0 "); // replace empty spaces with 0 value
            String[] parts = line.split(" ");
            int posX = 0;
            for (int i = 0; i < parts.length; i++){
                String part = parts[i];
                String blockId = ""+posX/4+posY/4;
                // Add blocks to block collection
                if (!blocks.containsKey(blockId)) blocks.put(blockId, new Block(posX/4, posY/4));

                // Add marked bushes to blocks
                if (!parts[i].equals("")) {
                    // no need to store the first columns of blocks
                    if (posX%4 == 0) {
                        posX++;
                        continue;
                    }
                    blocks.get(blockId).addBush(Integer.parseInt(part));
                    posX++;
                }

            }
        }

        /**
         * The Main Function; Handles reading an input file and starts the processing
         * @param args - should consist of an input file
         */
        public static void main(String args[])
        {
            // verify the input file
//            if (args.length <= 0){
//                System.out.println("Argument Not Found ✗\nExiting");
//                System.exit(0);
//            }
            Scanner sc = new Scanner(System.in);
            String fileName = "tiles.txt";//sc.nextLine();//args[0];   // get the name of the input file
            blocks = new HashMap<String, Block>();      // initialize storage for blocks
            targets = new HashMap<Integer, Integer>();  // initialize storage for targets
            tiles = new HashMap<Integer, Integer>();    // initialize storage for tiles
            arcs = new LinkedList<>();                 // initialize storage for arcs

            // Add default values to targets and tiles map
            targets.put(1,-1); targets.put(2,-1); targets.put(3,-1); targets.put(4,-1);
            tiles.put(EL_SHAPE,-1); tiles.put(OUTER_BOUNDARY,-1); tiles.put(FULL_BLOCK,-1);

            // ----------- Reading The File -----------
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                System.out.println("Reading file inputs...");
                String line;
                int boardSize = 0;
                int posY = 0;
                while ((line = br.readLine()) != null) {
                    if(line.length() == 0 || line.substring(0,1).equals("#"))
                        continue;
                    else if(line.substring(0,1).equals("{")) {
                        String[] parts = line.split("[^0-9]");
                        for (int i = 0; i < parts.length; i++){
                            if (!parts[i].equals("")) {
                                if (tiles.get(OUTER_BOUNDARY) == -1) tiles.put(OUTER_BOUNDARY, Integer.parseInt(parts[i]));
                                else if (tiles.get(EL_SHAPE) == -1) tiles.put(EL_SHAPE, Integer.parseInt(parts[i]));
                                else if (tiles.get(FULL_BLOCK) == -1) tiles.put(FULL_BLOCK, Integer.parseInt(parts[i]));
                            }
                        }
                        continue;
                    }
                    else if(line.substring(1,2).equals(":")) {
                        String[] parts = line.split(":");
                        if (targets.get(1) == -1) targets.put(1, Integer.parseInt(parts[1]));
                        else if (targets.get(2) == -1) targets.put(2, Integer.parseInt(parts[1]));
                        else if (targets.get(3) == -1) targets.put(3, Integer.parseInt(parts[1]));
                        else if (targets.get(4) == -1) targets.put(4, Integer.parseInt(parts[1]));
                        continue;
                    }

                    // Store the board size
                    if (posY == 0) boardSize = line.length()/2;
                    if (posY != boardSize)
                    {
                        // no need to store the first rows of blocks
                        if (posY%4 == 0) {
                            posY++;
                            continue;
                        }
                        storeBoardLine(line, posY);
                        posY++;
                    }
                }
                System.out.println("Done reading file inputs ✓");

                // Store all edges of the graph
                Set<String> keys = blocks.keySet();
                blockIds = new LinkedList<>(keys);  // store block ids to the queue
                System.out.printf("The board size is %d.\nTargets: \n\t1: %d\n\t2: %d\n\t3: %d\n\t4: %d\n" +
                                "The number of tiles: \n\tEL_SHAPE: %d\n\tOUTER_BOUNDARY: %d\n\tFULL_BLOCK: %d\n",
                        boardSize,targets.get(1),targets.get(2),targets.get(3),targets.get(4),
                        tiles.get(EL_SHAPE), tiles.get(OUTER_BOUNDARY), tiles.get(FULL_BLOCK));
            }
            catch (Exception ex){
                ex.printStackTrace();
                System.out.println("File Name: Not Found ✗\nExiting");
                System.exit(0);
            }
            // ----------- End Reading The File -----------

            // ----------- Covering The Board -----------
            coverBoard();
        }
}
