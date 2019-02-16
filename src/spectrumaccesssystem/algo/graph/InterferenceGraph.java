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
public class InterferenceGraph {
    int[][] adjacencyMatrix;
    
    public InterferenceGraph(){
        adjacencyMatrix = null;
    }
    
    public InterferenceGraph(List<Node> nodeList, boolean isDebug){
        double dist;
        Node n1, n2;
        
        int numOfNodes = nodeList.size();
        adjacencyMatrix = new int[numOfNodes][numOfNodes];
        
        for ( int i = 0; i < numOfNodes; i++){
            for ( int j = i + 1; j < numOfNodes; j++){
                n1 = nodeList.get(i);
                n2 = nodeList.get(j);
                
                dist = GeoUtils.getDistance(n1.getCbsdDevice().lat, n1.getCbsdDevice().lng, 
                        n2.getCbsdDevice().lat, n2.getCbsdDevice().lng);
                
                // Check if nodes i and j are interfering with each other
                // -1: no interference
                //  0: co-channel interference
                //  k: +/- k adj-channel interference (Not considered)
                if ( (dist > (n1.commRadius + n2.intRadius)) && 
                        (dist > (n2.commRadius + n1.intRadius) )){
                    adjacencyMatrix[i][j] = -1;
                    adjacencyMatrix[j][i] = -1;
                }else{
                    adjacencyMatrix[i][j] = 0;
                    adjacencyMatrix[j][i] = 0;
                }
            }
        }
        
        for (int i = 0; i < numOfNodes; i++){
            adjacencyMatrix[i][i] = 0;
        }
        
        if (isDebug){
            print();
        }
    }
    
    public int getMinChnDist(int idx1, int idx2){
        return adjacencyMatrix[idx1][idx2];
    }
    
    public List<Integer> getNeighborIdxList(int nodeIdx){
        List<Integer> nbrList = new ArrayList<>();
        for(int i = 0;i < adjacencyMatrix[0].length; i++){
            if ((i != nodeIdx) && adjacencyMatrix[nodeIdx][i] >= 0){
                nbrList.add(i);
            }
        }
        return(nbrList);
    }
    
    public void print(){
        System.out.println("intGraph AdjacencyMatrix = " + Arrays.deepToString(adjacencyMatrix));
    }
}
