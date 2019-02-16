/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.solver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import spectrumaccesssystem.algo.graph.*;
import spectrumaccesssystem.utils.*;

/**
 *
 * @author Xuhang Ying <xhying@uw.edu>
 */
public class MaxRewardSolver extends Solver {

    public MaxRewardSolver(ConflictGraph cGraph, boolean isDebug) {
        super(cGraph, isDebug);
    }
    
    @Override
    public void solve(){
        this.isRunning = true;
        LogUtils.INSTANCE.writeLog("INFO", "MaxRewardSolver has started ...");
        System.out.println("Start Time= " + TimeUtils.getCurrentDateTime(false));
        
        allocation = new ArrayList<>();
        
        int iterId = 0;
        
        if (isDebug){
            System.out.print("=================== Iter ID = " + iterId + " =================== \n");
            cGraph.print();
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
                cGraph.printUnremovedNcPairIdxSet();
            }
        }
        
        if (isDebug){
            System.out.println("Final allocation = " + allocation.toString());
            allocation.stream().forEach((idx) -> {
                cGraph.getNcPair(idx).print();
            });
        }
        
        System.out.println("End Time= " + TimeUtils.getCurrentDateTime(false));
        this.isRunning = false;
        LogUtils.INSTANCE.writeLog("INFO", "Solver MaxRewardDSA has finished ...");
    }
    
    private int getNextNcPairIdx(){
        double maxScore = -1.0;
        int bestIdx = -1;
        
        Set<Integer> unremovedNcPairIdxSet = cGraph.getUnremovedNcPairIdxSet();
        
        // Iterate over unremoved NC pair indices.
        Iterator<Integer> iterator = unremovedNcPairIdxSet.iterator();
        while (iterator.hasNext()){
            int idx = iterator.next();
            double score = cGraph.getReward(idx)/(cGraph.getVertexDegree(idx) + 1);
            
            if (score > maxScore){
                maxScore = score;
                bestIdx = idx;
            }
            
            if (isDebug){
                System.out.format("NCPair Idx = %d, reward = %.2f, degree = %d, score = %.2f\n", 
                            idx, cGraph.getReward(idx), cGraph.getVertexDegree(idx), score);
            }
        }
        
        return bestIdx;
    }
    
    private void removeNcPairFromGraph(int ncPairIdx){
        cGraph.removeNcPairFromGraph(ncPairIdx);
    }
}
