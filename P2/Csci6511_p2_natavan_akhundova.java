import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Vertex --- a class for encapsulating vertex information.
 */
class Vertex{
    private int assigned = -1;          // a color assigned to the vertex, default -1
    private Set<Integer> available;     // colors that are assignable to the vertex (i.e domain)
    private Set<Integer> edges;         // adjacent vertices connected through edges

    /**
     * Constructor function for initializing variables
     * @param K - the number of colors given in the CSP
     */
    Vertex(int K){
        available = new HashSet<Integer>();
        for(int i = 0; i < K; i++){
            available.add(i);
        }
        edges = new HashSet<Integer>();
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

    public Set<Integer> getAvailable(){
        return available;
    }

    public void addAvailable(int a){
        available.add(a);
    }

    public void removeAvailable(int a){
        available.remove(a);
    }

    public Set<Integer> getEdges(){
        return edges;
    }

    public void addEdges(int a){
        edges.add(a);
    }
}
/**
 * Csci6511_p2_natavan_akhundova --- a program to solve CSP.
 */
public class Csci6511_p2_natavan_akhundova {
    static Map<Integer, Vertex> vertices;                   // storage for all vertices in CSP
    static Queue<int[]> arcs;                               // storage for all arcs/edges in CSP
    static Map<Integer, ArrayList<Integer>> removedColors;  // storage for removed colors during AC3 Checking
    static int K = -1;                                      // the number of colors given in the CSP

    /**
     * Checks if assignment of the color to the vertex is safe considering its neighbors
     * @param color - the assigned color to the vertex
     * @param adjacentVertices - a set of adjacent vertices of the vertex
     * @return boolean value
     */
    public static boolean isSafe(Integer color, Set<Integer> adjacentVertices){
        // if any of the adjacent vertices has the same color, it is not safe
        for (Integer adjVertexId : adjacentVertices){
            if (vertices.get(adjVertexId).getAssigned() == color) return false;
        }
        // otherwise, safe
        return true;
    }

    /**
     * Restores CSP to the state where colors were not deleted from domains
     * @param vertex - currently processing vertex object
     * @param adjacentVertices - a set of adjacent vertices of currently processing vertex
     * @param color - the assigned color to the vertex
     */
    public static void restorePreviousState(Vertex vertex, Set<Integer> adjacentVertices, int color){
        // remove assignment of the color to the vertex
        vertex.setAssigned(-1);

        // return the color to the adjacent vertices
        for (Integer adjVertexId : adjacentVertices) {
            vertices.get(adjVertexId).addAvailable(color);
        }

        // restore removed colors from AC-3 checking
        if (removedColors.size() > 0) {
            Set<Integer> keys = removedColors.keySet();
            for (int key : keys) {
                ArrayList<Integer> values = removedColors.get(key);
                for (int value: values) {
                    vertices.get(key).addAvailable(value);
                }
            }
        }
    }

    /**
     * Sorts colors in the domain according to constraints
     * @param availableColors - a set of colors in a domain of the vertex
     * @param adjacentVertices - a set of adjacent vertices of the vertex
     * @return an ArrayList
     */
    public static ArrayList<Integer> leastConstrainingColors(Set<Integer> availableColors,
                                                             Set<Integer> adjacentVertices){
        Map<Integer, Integer> colorCount = new HashMap<>();
        ArrayList<Integer> sortedColors = new ArrayList<>();

        // set occurrences of available colors to 0
        for (Integer color : availableColors){
            colorCount.put(color, 0);
        }

        // count and update occurrences (inversely) of colors of assigned adjacent vertices
        for (Integer adjVertexId : adjacentVertices){
            int adjacentColor = vertices.get(adjVertexId).getAssigned();
            if (adjacentColor != -1) {
                if (colorCount.containsKey(adjacentColor))
                    colorCount.put(adjacentColor, colorCount.get(adjacentColor) - 1); // decrease the count
            }
        }

        // sort by occurrence values in ascending order
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(colorCount.entrySet());
        list.sort(Map.Entry.comparingByValue());

        for (Map.Entry<Integer, Integer> entry : list) {
            sortedColors.add(entry.getKey());
        }
        return sortedColors;
    }

    /**
     * Checks arc consistency between two vertices
     * @param xId - id of the first vertex
     * @param yId - id of the second vertex
     * @return boolean
     */
    public static boolean AC(int xId, int yId){
        boolean removed = false;
        // for every value of x, there should exist allowable value of y in the arc
        Set<Integer> xDomain = vertices.get(xId).getAvailable(); // get available colors of the vertex
        Set<Integer> yDomain = vertices.get(yId).getAvailable(); // get available colors of the adjacent vertex

        int yElem = yDomain.iterator().next();
        if (yDomain.size() == 1 && xDomain.contains(yElem)) {
            vertices.get(xId).removeAvailable(yElem);
            if (!removedColors.containsKey(xId)) removedColors.put(xId, new ArrayList<>());
            removedColors.get(xId).add(yElem);
            removed = true;
        }

        return removed;
    }

    /**
     * Checks arc consistency of all CSP
     * @return boolean
     */
    public static boolean AC3() {
        Queue<int[]> arcsCopy = new LinkedList<>(arcs);
        removedColors = new HashMap<>();

        while (arcsCopy.size() != 0){
            int[] xy = arcsCopy.remove();
            int xId = xy[0];
            int yId = xy[1];

            if(AC(xId,yId))
                if(vertices.get(xId).getAvailable().size() == 0) return false;
        }

        return true;
    }

    /**
     * Colors the provided vertex and its adjacent vertices recursively checking FC and AC3
     * @param vertexId - id of the vertex to be colored
     * @return boolean
     */
    public static boolean colorGraphRecursive(Integer vertexId){
        // if there is no left vertex to color
        if (vertexId == -1)
            return true;

        Vertex vertex = vertices.get(vertexId); // get current vertex by Id
        Set<Integer> adjacentVertices = vertex.getEdges(); // get adjacent vertices
        ArrayList<Integer> colors = leastConstrainingColors(vertex.getAvailable(), adjacentVertices); // get least constraining colors to assign
        boolean failure = false;    // flag for failures of FC and AC3 checks within CSP

        // find a color in available colors for the vertex that is safe to assign
        for (Integer color : colors){
            if(isSafe(color, adjacentVertices)){
                vertex.setAssigned(color); // assign the current color to the current vertex

                int nextVertexId = -1;
                int minRemainingValue = Integer.MAX_VALUE;

                // ----------- Processing Adjacent Vertices -----------
                for (Integer adjVertexId : adjacentVertices){
                    Vertex adjVertex = vertices.get(adjVertexId); // get current adjacent vertex by Id
                    adjVertex.removeAvailable(color);

                    // Forward Checking: if any value has no values, terminate
                    if (adjVertex.getAvailable().size() == 0) {failure = true; break;}

                    // find the next vertex by the minimum remaining value
                    if (adjVertex.getAssigned() == -1 && adjVertex.getAvailable().size() < minRemainingValue){
                        minRemainingValue = adjVertex.getAvailable().size();
                        nextVertexId = adjVertexId;
                    }
                }
                // ----------- End Processing Adjacent Vertices -----------

                // 3-Consistency Checking
                if (!AC3()) failure = true;

                if (!failure && colorGraphRecursive(nextVertexId)) return true; // recursively color other vertices

                restorePreviousState(vertex, adjacentVertices, color); // restore the previous state if the color assignment lead to no solution
            }
        }

        // no color has been assigned to the vertex
        return false;
    }

    /**
     * Calls recursive function of coloring the graph and prints the result
     */
    public static void colorGraph(){
        boolean solution = false;
        Set<Integer> keys = vertices.keySet(); // get ids of all vertices

        // start coloring uncolored vertices
        for (Integer key : keys) {
            if(vertices.get(key).getAssigned() == -1) {
                solution = colorGraphRecursive(key);
                if (!solution) break; // if any vertex resulted in no assignment
            }
        }

        // printing the result
        if (!solution) System.out.println("Solution does not exist.");
        else{
            System.out.println("Solution exists:");
            for (Integer key : keys) {
                int color = vertices.get(key).getAssigned();
                // print id of vertices and their assigned colors colorful
                System.out.printf("\u001B[3%dmThe color of %d is %d.\u001B[3%dm\n",color+1, key, color, color+1);
            }
            System.out.println("\u001B[0m");
        }
    }

    /**
     * The Main Function; Handles reading an input file and starts the processing
     * @param args - should consist of an input file
     */
    public static void main(String args[])
    {
        // verify the input file
        if (args.length <= 0){
            System.out.println("Argument Not Found ✗\nExiting");
            System.exit(0);
        }

        String fileName = args[0];                  // get the name of the input file
        vertices = new HashMap<Integer, Vertex>(); // initialize storage for vertices
        arcs = new LinkedList<>();                 // initialize storage for arcs

        // ----------- Reading The File -----------
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            System.out.println("Reading file inputs...");
            String line;
            int lineCount = 0;
            while ((line = br.readLine()) != null) {
                if(line.substring(0,1).equals("#"))
                    continue;
                if(K == -1) K = Integer.parseInt(line.split("=")[1].strip());
                else{
                    if(lineCount >= 5000) break; // limit the size of CSP

                    int fromVertice = Integer.parseInt(line.split(",")[0].strip());
                    int toVertice = Integer.parseInt(line.split(",")[1].strip());

                    // Add vertices to vertex collection
                    if (!vertices.containsKey(fromVertice)) vertices.put(fromVertice, new Vertex(K));
                    if (!vertices.containsKey(toVertice)) vertices.put(toVertice, new Vertex(K));

                    // Add undirected edges
                    vertices.get(fromVertice).addEdges(toVertice);
                    vertices.get(toVertice).addEdges(fromVertice);
                    lineCount++;
                }
            }
            System.out.println("Done reading file inputs ✓");

            // Store all edges of the graph
            Set<Integer> keys = vertices.keySet();
            for (int key : keys){
                Set<Integer> edges = vertices.get(key).getEdges();
                for (int edge : edges){
                    arcs.add(new int[]{key, edge});
                }
            }

            System.out.printf("The number of \n\t1)vertices: %d\n\t2)arcs: %d\n\t3)colors: %d\n",
                    vertices.size(),arcs.size(),K);
        }
        catch (Exception ex){
            ex.printStackTrace();
            System.out.println("File Name: Not Found ✗\nExiting");
            System.exit(0);
        }
        // ----------- End Reading The File -----------

        // ----------- Coloring The Graph -----------
        colorGraph();
    }
}
