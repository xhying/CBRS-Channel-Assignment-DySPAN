/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.simulation;

import java.util.List;
import java.util.Map;
import spectrumaccesssystem.algo.device.*;
import spectrumaccesssystem.algo.graph.*;
import spectrumaccesssystem.algo.solver.*;

/**
 *
 * @author Xuhang Ying <xhying@uw.edu>
 */
public class Simulation {
    
    protected Map<String, Solver> solvers;
    protected String activeSolverName;
    
    protected List<CBSDDevice> cbsdDeviceList;
    protected ConflictGraph cGraph;
    protected boolean isDebug;
    
    public Simulation(boolean isDebug){
        this.solvers = null;
        this.activeSolverName = null;
        this.cbsdDeviceList = null;
        this.cGraph = null;
        this.isDebug = isDebug;
    }
    
    public void start(){}           // Run all experiments
    public void start(int expId){}  // Run the specificed experiemnt
    
}
