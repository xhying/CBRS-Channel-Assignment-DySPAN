/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo;

/**
 *
 * @author Xuhang Ying <xhying@uw.edu>
 */
public class DCAConstants {
    public static final String CONFLICT_GRAPH_BINARY = "binary";
    public static final String CONFLICT_GRAPH_NON_BINARY = "nonbinary";
    
    // CA algorithms
    public static final String MIN_DEMAND = "Min-Demand-CA";
    public static final String MAX_REWARD = "Max-Reward-CA";
    public static final String MIN_DEMAND_MAX_REWARD = "Min-Demand-Max-Reward-CA";
    public static final String MAX_UTILITY = "Max-Utility-CA";
    
    // Reward functions
    public static final String REWARD_FUNC_LINEAR = "linear";
    public static final String REWARD_FUNC_LOG = "log";
    
    // Penalty function
    public static final int PENALTY_FUNC_INT = 0;   // Interference as penalty function
    
    // Experiment identifiers
    public static final int EXP_TEST = 0;
    public static final int EXP_GAA_CA_BINARY = 1;
    public static final int EXP_GAA_CA_COEXISTENCE = 2;
    public static final int EXP_GAA_CA_COEXISTENCE2 = 3;
    public static final int EXP_GAA_CA_NON_BINARY = 4;
    
}
