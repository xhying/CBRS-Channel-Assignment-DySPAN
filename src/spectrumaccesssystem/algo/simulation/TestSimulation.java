/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import spectrumaccesssystem.algo.*;
import spectrumaccesssystem.algo.device.*;
import spectrumaccesssystem.algo.graph.*;
import spectrumaccesssystem.algo.solver.*;
import spectrumaccesssystem.utils.GeoUtils;

/**
 *
 * @author Xuhang Ying <xhying@uw.edu>
 */
public class TestSimulation extends Simulation {
    
    public TestSimulation(boolean isDebug) {
        super(isDebug);
        setup();
    }
    
    private void setup(){
        // Set the total number of channels
        Channel.TOTAL_NUM_OF_CHANNELS = 3;
        
        int numOfCbsds = 3;
        
        // Set all channels to available
        boolean[][] availableChns = new boolean[numOfCbsds][Channel.TOTAL_NUM_OF_CHANNELS];
        for (int i = 0; i < numOfCbsds; i++){
            for (int j = 0; j < availableChns[i].length; j++){
                availableChns[i][j] = true;
            }
        }
        
        boolean[][] assignedChns = new boolean[numOfCbsds][Channel.TOTAL_NUM_OF_CHANNELS];
        // assignedChns[0][0] = true;           // Assign the 1st channel to the 1st node.
        
        // CBSD parameters
        double txPower = 30.0;
        double antHeight = 3.0;

        // Demand of CBSD device = {1, 2, 3, 4}
        int minDemand = 1;
        int maxDemand = 4;
        int stepSize = 1;
        
        // Locations
        double[][] locations = new double[numOfCbsds][2];
        locations[0][0] = 40.6870; 
        locations[0][1] = -74.4040;
        
        locations[1][0] = 40.6870; 
        locations[1][1] = -74.4030;
        
        locations[2][0] = 40.6840; 
        locations[2][1] = -74.4000;
        
        System.out.printf("Dist(node 0, node 1) = %.2f\n", GeoUtils.getDistance(locations[0][0], locations[0][1], 
                        locations[1][0], locations[1][1]));
        System.out.printf("Dist(node 0, node 2) = %.2f\n", GeoUtils.getDistance(locations[0][0], locations[0][1], 
                        locations[2][0], locations[2][1]));
        System.out.printf("Dist(node 1, node 2) = %.2f\n", GeoUtils.getDistance(locations[1][0], locations[1][1], 
                        locations[2][0], locations[2][1]));
        
        if (isDebug){
            System.out.println("Locations: " + Arrays.deepToString(locations));
        }
        
        // Activity indices
        double[] activityIndices = new double[]{0.3, 0.2, 0.1};
        
        // Create cbsd device list
        CBSDDeviceDemand deviceDemand; 
        
        boolean coexistenceEnabled = true;
        
        cbsdDeviceList = new ArrayList<>();
        for (int i = 0; i < numOfCbsds; i++){
            deviceDemand = new CBSDDeviceDemand(minDemand, maxDemand, stepSize);
            cbsdDeviceList.add(new CBSDDevice(i, locations[i][0], locations[i][1], txPower, antHeight, 
                    availableChns[i], assignedChns[i], deviceDemand, activityIndices[i], coexistenceEnabled));
        }
        
        // Create conflict graph
        double alphaLimit = 1.0;        // 0.0 means no super-nodes. 
        String rewardFunc = DCAConstants.REWARD_FUNC_LINEAR;
        
        cGraph = new ConflictGraph( DCAConstants.CONFLICT_GRAPH_BINARY, cbsdDeviceList, alphaLimit, rewardFunc, isDebug );
        
        // Create multiple solvers
        solvers = new HashMap<>();
        //solvers.put(DCAConstants.MAX_REWARD, new MaxRewardSolver(cGraph, isDebug));
        //solvers.put(DCAConstants.MIN_DEMAND, new MinDemandSolver(cGraph, isDebug));
        solvers.put(DCAConstants.MIN_DEMAND_MAX_REWARD, new MinDemandMaxRewardSolver(cGraph, isDebug));
    }
    
    @Override
    public void start(){
        // activeSolverName = DCAConstants.MAX_REWARD;
        // activeSolverName = DCAConstants.MIN_DEMAND;
        activeSolverName = DCAConstants.MIN_DEMAND_MAX_REWARD;
        
        if ( solvers.containsKey(activeSolverName) ){
            Solver solver = solvers.get(activeSolverName);
            solver.resetConflictGraph();
            solver.solve();
        }
    }
}
