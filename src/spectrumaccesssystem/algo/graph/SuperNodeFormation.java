/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.graph;

/**
 *
 * Modified from https://algos.org/maximal-cliquesbron-kerbosch-without-pivot-java/
 * 
 * @author Xuhang Ying <xuhang.1.ying@nokia.com>
 */

import java.io.BufferedReader; 
import java.io.StringReader; 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SuperNodeFormation {

    private int nodesCount; 
    private final List<Vertex> graph = new ArrayList<>(); 
    private final List<List<Integer>> cliques = new ArrayList<>();
    private final List<List<Integer>> reorganizedCliques = new ArrayList<>();
    
    private final boolean debug = false;

    private class Vertex implements Comparable<Vertex> {
        private int x; 

        private int degree; 
        private List<Vertex> nbrs = new ArrayList<>(); 

        public int getX() {
            return x; 
        } 

        public void setX(int x) {
            this.x = x; 
        } 

        public int getDegree() {
            return degree; 
        } 

        public void setDegree(int degree) {
            this.degree = degree; 
        } 

        public List<Vertex> getNbrs() { 
            return nbrs; 
        } 

        public void setNbrs(List<Vertex> nbrs) {
            this.nbrs = nbrs; 
        } 

        public void addNbr(Vertex y) {
            this.nbrs.add(y); 
            if (!y.getNbrs().contains(y)) { 
                y.getNbrs().add(this); 
                y.degree++; 
            } 
            this.degree++; 

        } 

        public void removeNbr(Vertex y) {
            this.nbrs.remove(y); 
            if (y.getNbrs().contains(y)) { 
                y.getNbrs().remove(this); 
                y.degree--; 
            } 
            this.degree--; 

        } 

        @Override 
        public int compareTo(Vertex o) {
            if (this.degree < o.degree) {
                return -1; 
            } 
            if (this.degree > o.degree) {
                return 1;
            } 
            return 0; 
        } 

        @Override
        public String toString() { 
            return "" + x; 
        } 
    } 

    private void initGraph() { 
        graph.clear(); 
        for (int i = 0; i < nodesCount; i++) {
            Vertex V = new Vertex(); 
            V.setX(i); 
            graph.add(V); 
        } 
    } 

    private int readTotalGraphCount(BufferedReader bufReader) throws Exception {

        return Integer.parseInt(bufReader.readLine()); 
    } 

    // Reads Input 
    private void readNextGraph(BufferedReader bufReader) throws Exception {
        try { 
            nodesCount = Integer.parseInt(bufReader.readLine()); 
            int edgesCount = Integer.parseInt(bufReader.readLine());
            initGraph(); 

            for (int k = 0; k < edgesCount; k++) {
                String[] strArr = bufReader.readLine().split(" "); 
                int u = Integer.parseInt(strArr[0]);
                int v = Integer.parseInt(strArr[1]);
                Vertex vertU = graph.get(u); 
                Vertex vertV = graph.get(v); 
                vertU.addNbr(vertV); 

            } 

        } catch (Exception e) { 
            e.printStackTrace(); 
            throw e; 
        } 
    } 

    // Finds nbrs of vertex i 
    private List<Vertex> getNbrs(Vertex v) { 
        int i = v.getX(); 
        return graph.get(i).nbrs; 
    } 

    // Intersection of two sets 
    private List<Vertex> intersect(List<Vertex> arlFirst, 
            List<Vertex> arlSecond) { 
        List<Vertex> arlHold = new ArrayList<>(arlFirst); 
        arlHold.retainAll(arlSecond); 
        return arlHold; 
    } 

    // Union of two sets 
    private List<Vertex> union(List<Vertex> arlFirst, List<Vertex> arlSecond) { 
        List<Vertex> arlHold = new ArrayList<>(arlFirst); 
        arlHold.addAll(arlSecond); 
        return arlHold; 
    } 

    // Removes the neigbours 
    private List<Vertex> removeNbrs(List<Vertex> arlFirst, Vertex v) { 
        List<Vertex> arlHold = new ArrayList<>(arlFirst); 
        arlHold.removeAll(v.getNbrs()); 
        return arlHold; 
    } 

    // Version with a Pivot 
    private void Bron_KerboschWithPivot(List<Vertex> R, List<Vertex> P,
            List<Vertex> X, String pre) { 

        if (debug) 
            System.out.print(pre + " " + printSet(R) + ", " + printSet(P) + ", " 
                + printSet(X)); 
        if ((P.size() == 0) && (X.size() == 0)) {
            if (debug) printClique(R);
            
            // Shaun: store each obtained clique to cliques
            List<Integer> idxList = new ArrayList<>();
            R.stream().forEach((v)->{
                idxList.add(v.getX());
            });
            cliques.add(idxList);
            return; 
        } 
        if (debug) 
            System.out.println(); 
        List<Vertex> P1 = new ArrayList<>(P); 
        // Find Pivot 
        Vertex u = getMaxDegreeVertex(union(P, X)); 

        if (debug)
            System.out.println("" + pre + " Pivot is " + (u.x)); 
        // P = P / Nbrs(u) 
        P = removeNbrs(P, u); 

        for (Vertex v : P) { 
            R.add(v); 
            Bron_KerboschWithPivot(R, intersect(P1, getNbrs(v)), 
                    intersect(X, getNbrs(v)), pre + "\t"); 
            R.remove(v); 
            P1.remove(v); 
            X.add(v); 
        } 
    } 

    private Vertex getMaxDegreeVertex(List<Vertex> g) { 
        Collections.sort(g); 
        return g.get(g.size() - 1);
    } 

    private void Bron_KerboschPivotExecute() { 

        List<Vertex> X = new ArrayList<>(); 
        List<Vertex> R = new ArrayList<>(); 
        List<Vertex> P = new ArrayList<>(graph); 
        Bron_KerboschWithPivot(R, P, X, ""); 
    } 

    private void printClique(List<Vertex> R) { 
        System.out.print("  --- Maximal Clique : "); 
        for (Vertex v : R) { 
            System.out.print(" " + (v.getX())); 
        } 
        System.out.println(); 
    } 

    private String printSet(List<Vertex> Y) { 
        StringBuilder strBuild = new StringBuilder(); 

        strBuild.append("{"); 
        for (Vertex v : Y) { 
            strBuild.append("" + (v.getX()) + ","); 
        } 
        if (strBuild.length() != 1) {
            strBuild.setLength(strBuild.length() - 1); 
        } 
        strBuild.append("}"); 
        return strBuild.toString(); 
    } 
    
    // Each vertex can only belong to one clique.
    private void reorganizeCliques(){
        List<List<Integer>> vertexList = new ArrayList<>();
        for(int i=0;i<nodesCount;i++){
            vertexList.add(new ArrayList<>());  // A list of cliques each vertex belongs to
        }
        
        for(int cliqueIdx=0;cliqueIdx<cliques.size();cliqueIdx++){
            List<Integer> list = cliques.get(cliqueIdx);
            for(int i=0;i<list.size();i++){
                vertexList.get(list.get(i)).add(cliqueIdx);
            }
        }
        
        // Debug
        for(int i=0;i<vertexList.size();i++){
            if (debug) 
                System.out.println("Vertex idx " + i + ", " + vertexList.get(i).toString());
        }
        
        for(int i=0;i<cliques.size();i++){
            reorganizedCliques.add(new ArrayList<>());
        }
        
        // Each vertex joins the first clique it belongs to.
        // Alternative (TODO): each vertex joins a clique randomly.
        for(int vertexId=0;vertexId<nodesCount;vertexId++){
            if ( vertexList.get(vertexId).isEmpty() == false){
                reorganizedCliques.get(vertexList.get(vertexId).get(0)).add(vertexId);
            }
        }
    }
    
    public List<List<Integer>> getReorganizedCliques(){
        return(reorganizedCliques);
    }
    
    private void printCliques(){
        System.out.println("Cliques:");
        for(int i=0;i<cliques.size();i++){
            System.out.format("# %d: ", i);
            for(int j=0;j<cliques.get(i).size();j++){
                System.out.format("%d, ", cliques.get(i).get(j));
            }
            System.out.format("\n");
        }
        
        reorganizeCliques();
        
        System.out.println("Reorganized Cliques:");
        for(int i=0;i<reorganizedCliques.size();i++){
            System.out.format("# %d: ", i);
            for(int j=0;j<reorganizedCliques.get(i).size();j++){
                System.out.format("%d, ", reorganizedCliques.get(i).get(j));
            }
            System.out.format("\n");
        }
    }
    
    public static List<List<Integer>> getSuperNodeList(String graph, List<Double> activityIdxList, double alpha){
        BufferedReader bufReader = new BufferedReader(new StringReader(graph));
        SuperNodeFormation snf = new SuperNodeFormation();
        List<List<Integer>> superNodeList = new ArrayList<>();
            
        try { 
            int totalGraphs = snf.readTotalGraphCount(bufReader);
            if (snf.debug)
                System.out.println("Max Cliques with Pivot"); 
            for (int i = 0; i < totalGraphs; i++) {
                if (snf.debug){
                    System.out.println("************** Start Graph " + (i + 1) 
                            + "******************************");
                }
                snf.readNextGraph(bufReader); 
                
                // Vertices are labled from 0 to nodesCount-1 in some order
                // Corresponding activity indices are stored in activityIdxList in the same order.
                // Bron-Kerbosch algorithm to find all cliques
                snf.Bron_KerboschPivotExecute(); 
                
                // Each node belonging to multiple cliques has to choose one to join. 
                snf.reorganizeCliques();
                
                // For each clique, run FFD to identify super-nodes with respect to alpha
                List<List<Integer>> cliques = snf.getReorganizedCliques();
                for(int j=0;j<cliques.size();j++){
                    superNodeList.addAll(snf.FFD(cliques.get(j), activityIdxList, alpha));
                }
            } 
        } catch (Exception e) { 
            e.printStackTrace(); 
            System.err.println("Exiting : " + e); 
        } finally { 
            try { 
                bufReader.close(); 
            } catch (Exception f) { 

            } 
        } 
        
        return(superNodeList);
    }
    
    private List<List<Integer>> FFD(List<Integer> clique, List<Double> activityIdxList, double alpha){
        List<List<Integer>> superNodeList = new ArrayList<>();
        
        // The number of super-nodes or bins cannot be greater than the clique size.
        double[] totalActivityIndexArray = new double[clique.size()];
        for(int i=0;i<totalActivityIndexArray.length;i++){
            totalActivityIndexArray[i] = 0.0;
        }
        
        // Construct a list of vertices with activity index information
        List<Vertex2> list = new ArrayList<>();
        for(int i=0;i<clique.size();i++){
            int vertexIdx = clique.get(i);
            double activityIdx = activityIdxList.get(vertexIdx);
            list.add(new Vertex2(vertexIdx, activityIdx));
        }

        if (debug){
            System.out.println("Before sorting");
            list.stream().forEach((v)->{
                System.out.println(v.toString());
            });
        }
        // Sort vertices in non-increasing order of activity index
        Collections.sort(list, Collections.reverseOrder());     // Descending order
        if (debug){
            System.out.println("After sorting");
            list.stream().forEach((v)->{
                System.out.println(v.toString());
            });
        }
        
        // Place each vertex into the lowest-indexed super-node with sufficient remaining space.
        for(int i=0;i<list.size();i++){
            Vertex2 v = list.get(i);
            
            // Find an existing super-node with sufficient remaining space
            boolean isExistingSuperNodeFound = false;
            for(int j=0;j<superNodeList.size();j++){
                if ( (v.activityIdx+totalActivityIndexArray[j]) <= alpha ){
                    superNodeList.get(j).add(v.idx);
                    totalActivityIndexArray[j] += v.activityIdx;
                    isExistingSuperNodeFound = true;
                    break;
                }
            }
            
            // Otherwise, create a new super-node
            if (isExistingSuperNodeFound == false){
                superNodeList.add(new ArrayList<>());   // Create a new empty bin
                int superNodeIdx = superNodeList.size()-1;
                superNodeList.get(superNodeIdx).add(v.idx);
                totalActivityIndexArray[superNodeIdx] = v.activityIdx;
            }
        }
        
        if (debug){
            for(int i=0;i<superNodeList.size();i++){
                System.out.println("ID=" + i + ", total activity index = " + totalActivityIndexArray[i] 
                        + ", " + superNodeList.get(i));
            }
        }
        
        // Ignore super-nodes with a single node.
        List<List<Integer>> superNodeList2 = new ArrayList<>();
        for(int i=0;i<superNodeList.size();i++){
            if (superNodeList.get(i).size()>1){
                superNodeList2.add(superNodeList.get(i));
            }
        }
        
        return(superNodeList2);
    }
    
    private class Vertex2 implements Comparable<Vertex2>{
        int idx;
        double activityIdx;
        
        public Vertex2(int idx, double activityIdx){
            this.idx = idx;
            this.activityIdx = activityIdx;
        }
        
        @Override
        public int compareTo(Vertex2 o) {
            if (this.activityIdx < o.activityIdx) {
                return -1; 
            } 
            if (this.activityIdx > o.activityIdx) {
                return 1;
            } 
            return 0; 
        }
        
        @Override
        public String toString() { 
            return String.format("(%d,%.2f)", idx, activityIdx); 
        } 
        
    }
}
