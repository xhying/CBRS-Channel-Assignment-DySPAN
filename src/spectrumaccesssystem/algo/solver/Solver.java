/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.solver;

import java.util.List;
import spectrumaccesssystem.algo.graph.*;

/**
 *
 * @author Xuhang Ying <xhying@uw.edu>
 */
public class Solver implements Runnable {
    
    protected ConflictGraph cGraph; // Input
    protected List<Integer> allocation; // Output: selected NC pair indices
    
    protected boolean isDebug;
    protected boolean isRunning;
    
    public Solver(){
        this.cGraph = null;
        this.allocation = null;
        this.isDebug = false;
        this.isRunning = false;
    }
    
    public Solver(ConflictGraph cGraph, boolean isDebug){
        this.cGraph = cGraph;
        this.allocation = null;
        this.isDebug = isDebug;
        this.isRunning = false;
    }

    @Override
    public void run() {
        solve();
    }
    
    public void solve(){}
    
    public void resetConflictGraph(){
        this.cGraph.reset();
    }
    
    public List<Integer> getAllocation(){
        return (this.allocation);
    }
    
    public boolean isRunning(){
        return (this.isRunning);
    }
    
}
