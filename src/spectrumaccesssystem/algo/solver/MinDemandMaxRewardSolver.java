/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.solver;

import java.util.List;
import spectrumaccesssystem.algo.graph.*;
import spectrumaccesssystem.utils.*;

/**
 *
 * @author Xuhang Ying <xhying@uw.edu>
 */
public class MinDemandMaxRewardSolver extends Solver {
    
    public MinDemandMaxRewardSolver(ConflictGraph cGraph, boolean isDebug){
        super(cGraph, isDebug);
    }
    
    @Override
    public void solve(){
        cGraph.reset();
        
        this.isRunning = true;
        LogUtils.INSTANCE.writeLog("INFO", "MinDemandMaxRewardSolver has started ...");
        System.out.println("Start Time = " + TimeUtils.getCurrentDateTime(false));
        
        //=================== Min-demand phase =====================
        Solver minDemandSolver = new MinDemandSolver(cGraph, isDebug);
        minDemandSolver.solve();
        List<Integer> minDemandAllocation = minDemandSolver.getAllocation();
        
        if (isDebug){
            System.out.println("Mi-demand allocation = " + minDemandAllocation.toString());
        }
        
        //=================== Max-reward phase =====================
        cGraph.reset(minDemandAllocation);
        
        Solver maxRewardSolver = new MaxRewardSolver(cGraph, isDebug);
        maxRewardSolver.solve();
        List<Integer> maxRewardAllocation = maxRewardSolver.getAllocation();
        
        if (isDebug){
            System.out.println("Max-reward allocation = " + minDemandAllocation.toString());
        }
        
        System.out.println("End Time = " + TimeUtils.getCurrentDateTime(false));
        
        this.isRunning = false;
        LogUtils.INSTANCE.writeLog("INFO", "Solver MinDemandMaxRewardDSA has finished ...");
        LogUtils.INSTANCE.writeLog("INFO", "New channel assignments have been prepared ...");
        
        //=================== Final results =====================
        if (isDebug){
            System.out.println("cGraph = ");
            cGraph.print();
            System.out.println("Final allocation = " + maxRewardAllocation.toString());
        }
    }
    
}
