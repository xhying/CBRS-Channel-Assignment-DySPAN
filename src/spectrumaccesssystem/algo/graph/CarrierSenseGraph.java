/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import spectrumaccesssystem.utils.GeoUtils;

/**
 *
 * @author Xuhang Ying <xhying@uw.edu>
 */
public class CarrierSenseGraph {
    private final boolean[][] adjacencyMatrix;
    
    public CarrierSenseGraph(){
        adjacencyMatrix = null;
    }
    
    public CarrierSenseGraph(List<Node> nodeList, boolean isDebug){
        double dist;
        Node n1, n2;
        
        int numOfNodes = nodeList.size();
        adjacencyMatrix = new boolean[numOfNodes][numOfNodes];
        
        for ( int i = 0; i < numOfNodes; i++){
            for ( int j = i + 1; j < numOfNodes; j++){
                n1 = nodeList.get(i);
                n2 = nodeList.get(j);
                
                dist = GeoUtils.getDistance(n1.getCbsdDevice().lat, n1.getCbsdDevice().lng, 
                        n2.getCbsdDevice().lat, n2.getCbsdDevice().lng);
                
                // Check if nodes i and j are within each other CS range
                adjacencyMatrix[i][j] = (dist <= n1.csRadius) && (dist <= n2.csRadius);
                adjacencyMatrix[j][i] = adjacencyMatrix[i][j];
            }
        }
        
        for (int i = 0; i < numOfNodes; i++){
            adjacencyMatrix[i][i] = true;
        }
        
        if (isDebug){
            print();
        }
    }
    
    public boolean isWithinCsRange(int idx1, int idx2){
        return adjacencyMatrix[idx1][idx2];
    }
    
    public List<Integer> getNeighborIdxList(int nodeIdx){
        List<Integer> nbrList = new ArrayList<>();
        for(int i = 0;i < adjacencyMatrix[0].length; i++){
            if ( (i != nodeIdx) && adjacencyMatrix[nodeIdx][i]){
                nbrList.add(i);
            }
        }
        return(nbrList);
    }
    
    public void print(){
        System.out.println(" csGraph AdjacencyMatrix = " + Arrays.deepToString(adjacencyMatrix));
    }
}
