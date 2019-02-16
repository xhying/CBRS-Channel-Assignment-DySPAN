/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.solver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import spectrumaccesssystem.algo.graph.ConflictGraph;
import spectrumaccesssystem.utils.LogUtils;

/**
 *
 * @author Xuhang Ying <xhying@uw.edu>
 */
public class MinDemandSolver extends Solver{
    
    public MinDemandSolver(ConflictGraph cGraph, boolean isDebug) {
        super(cGraph, isDebug);
    }
    
    @Override
    public void solve(){
        this.isRunning = true;
        LogUtils.INSTANCE.writeLog("INFO", "MinDemandSolver has started ...");
        System.out.println("Start Time = " + TimeUtils.getCurrentDateTime(false));
        
        allocation = new ArrayList<>();
        
        int iterId = 0;
        
        if (isDebug){
            System.out.print("=================== Iter ID = " + iterId + " =================== \n");
            cGraph.print();
            cGraph.printUnremovedMinDemandNcPairIdxSet();
            cGraph.printUnremovedNcPairIdxSet();
        }
        
        while (true){
            iterId ++;
            
            if (isDebug){
                System.out.print("=================== Iter ID = " + iterId + " =================== \n");
            }
            
            int nextNcPairIdx = getNextNcPairIdx();
            if (nextNcPairIdx != -1){
                allocation.add(nextNcPairIdx);
                removeNcPairFromGraph(nextNcPairIdx);
            }else{
                System.out.println("No more NC pairs available. Stopping ...");
                break;
            }
            
            if (isDebug){
                System.out.println("Selected = " + allocation.toString());
                cGraph.printUnremovedMinDemandNcPairIdxSet();
                cGraph.printUnremovedNcPairIdxSet();
            }
        }
        
        if (isDebug){
            System.out.println("Final allocation = " + allocation.toString());
            allocation.stream().forEach((idx) -> {
                cGraph.getNcPair(idx).print();
            });
        }
        
        System.out.println("End Time = " + TimeUtils.getCurrentDateTime(false));
        this.isRunning = false;
        LogUtils.INSTANCE.writeLog("INFO", "Solver MinDemandDSA has finished ...");
    }
    
    private int getNextNcPairIdx(){
        List<Integer> maxScoreNcPairIdxList = new ArrayList<>();
        List<Integer> minConflictNcPairIdxList = new ArrayList<>();
        List<Integer> maxSuccessorNcPairIdxList = new ArrayList<>();
        
        // Criterion 1: maximum score
        double maxScore = -1.0;
        
        Set<Integer> unremovedMinDemandNcPairIdxSet = cGraph.getUnremovedMinDemandNcPairIdxSet();
        
        Iterator<Integer> it = unremovedMinDemandNcPairIdxSet.iterator();
        
        while(it.hasNext()){
            int idx = it.next();
            
            double reward = cGraph.getReward(idx);
            int minDemandVertexDegree = cGraph.getMinDemandVertexDegree(idx);
            double score = reward/(minDemandVertexDegree + 1);
            
            if (score > maxScore){
                maxScore = score;
                maxScoreNcPairIdxList.clear();
                maxScoreNcPairIdxList.add(idx);
            }else if (score == maxScore){
                maxScoreNcPairIdxList.add(idx);
            }
            
            if (isDebug){
                System.out.format("[DEBUG] Idx = %d, reward = %.2f, minDeg = %d, score = %.2f\n", 
                            idx, reward, minDemandVertexDegree, score);
            }
        }
        
        // Criterion 2: minimum inter-node conflicts
        int minInterDegree = Integer.MAX_VALUE;
        
        for (int idx : maxScoreNcPairIdxList){
            int interDegree = cGraph.getInterNodeVertexDegree(idx);
            
            if (interDegree < minInterDegree){
                minInterDegree = interDegree;
                minConflictNcPairIdxList.clear();
                minConflictNcPairIdxList.add(idx);
            }else if (interDegree == minInterDegree){
                minConflictNcPairIdxList.add(idx);
            }
            
            if (isDebug){
                System.out.format("[DEBUG] Idx = %d, interDeg = %d\n", idx, interDegree);
            }
        }
        
        // Criterion 3: maxmium number of successors
        int maxNumOfSuccessors = -1;
        
        for (int idx : minConflictNcPairIdxList){
            int numOfSuccessors = cGraph.getNumOfSuccessors(idx);
            
            if (numOfSuccessors > maxNumOfSuccessors){
                maxNumOfSuccessors = numOfSuccessors;
                maxSuccessorNcPairIdxList.clear();
                maxSuccessorNcPairIdxList.add(idx);
            }else{
                maxSuccessorNcPairIdxList.add(idx);
            }
            
            if (isDebug){
                System.out.format("[DEBUG] Idx = %d, numOfSuccessors = %d\n", idx, numOfSuccessors);
            }
        }
        
        if (maxSuccessorNcPairIdxList.isEmpty()){
            return - 1;
        }else{
            return maxSuccessorNcPairIdxList.get(0);
        }
    }
    
    private void removeNcPairFromGraph(int ncPairIdx){
        cGraph.removeNcPairFromGraph2(ncPairIdx);
    }
}
