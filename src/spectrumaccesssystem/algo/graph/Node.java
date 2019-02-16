/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import spectrumaccesssystem.algo.device.CBSDDevice;
import spectrumaccesssystem.algo.device.Channel;
import spectrumaccesssystem.algo.pathloss.PathLossEngine;

/**
 *
 * @author Xuhang Ying <xhying@uw.edu>
 */
public class Node {
    private final int nodeIdx;
    private final CBSDDevice cbsdDevice;
    private final double activityIdx;
    private final boolean coexEnabled;
    
    private final boolean[] availableChns; 
    private final boolean[] assignedChns;
    private int startAssignedChn;
    private int endAssignedChn;
    private int numOfAssignedChns;
    
    private final boolean isMaxDemandMet;
    
    // For SUI (txPower = 30, rxPower = -96, -72, -80)
    // commRadius=0.2603, csRadius=0.1387, intRadius=0.1711
    public final double commRadius;    // Coverage radius in km
    public final double csRadius;      // Carrier-sensing radius in km
    public final double intRadius;     // Interference radius in km
    
    private final String rewardFunc;    // Reward function
    
    public Node(){
        this.nodeIdx = 0;
        this.cbsdDevice = null;
        this.activityIdx = 0;
        this.coexEnabled = false;
        this.availableChns = null;
        this.assignedChns = null;
        this.startAssignedChn = -1;
        this.endAssignedChn = -1;
        this.numOfAssignedChns = 0;
        this.isMaxDemandMet = false;
        this.commRadius = 0.0;      // -96 dBm/10 MHz
        this.csRadius = 0.0;        // -72 dBm/10 MHz
        this.intRadius = 0.0;       // -80 dBm/10 MHz
        this.rewardFunc = null;
    }
    
    public Node(int nodeIdx, CBSDDevice device, double activityIdx, boolean coexistenceEnabeld,
            boolean[] availableChns, boolean[] assignedChns, String rewardFunc){
        this.nodeIdx = nodeIdx;
        this.cbsdDevice = device;
        this.activityIdx = activityIdx;
        this.coexEnabled = coexistenceEnabeld;
        
        this.availableChns = new boolean[Channel.TOTAL_NUM_OF_CHANNELS];
        this.assignedChns = new boolean[Channel.TOTAL_NUM_OF_CHANNELS];
        
        if (availableChns == null){
            // All channels are available
            for (int chn = 0;chn < Channel.TOTAL_NUM_OF_CHANNELS; chn++){
                this.availableChns[ chn ] = true;
            }
        }else{
            // Only specified channels are available
            System.arraycopy( availableChns, 0, this.availableChns, 0, availableChns.length );
        }
        
        this.startAssignedChn = -1;
        this.endAssignedChn = -1;
        
        if ( assignedChns != null){
            System.arraycopy( assignedChns, 0, this.assignedChns, 0, assignedChns.length );
            
            // Find the first assigned chn
            for ( int i = 0; i < assignedChns.length; i ++){
                if ( assignedChns[i] ){
                    this.startAssignedChn = i;
                    break;
                }
            }
            
            // Find the last assigned chn
            for ( int i = assignedChns.length - 1; i >= 0; i --){
                if ( assignedChns[i] ){
                    this.endAssignedChn = i;
                    break;
                }
            }
            
            if ( startAssignedChn >= 0 && endAssignedChn >= 0){
                // Assigned channels should be contiguous
                for ( int i = this.startAssignedChn; i <= this.endAssignedChn; i ++ ){
                    if ( !assignedChns[i] ){
                        throw new IllegalArgumentException(String.format("Invalid assigned channels: start = %d, end = %d\n", startAssignedChn, endAssignedChn));
                    }
                }

                this.numOfAssignedChns = this.endAssignedChn - this.startAssignedChn + 1;
            }
        }
        
        this.isMaxDemandMet = (this.numOfAssignedChns == device.deviceDemand.getMaxDemand());
        
        this.commRadius = PathLossEngine.INSTANCE.getDistance(device.frequency, 
                device.txPower - device.communicationRss, device.txAntHeight, device.rxAntHeight);
        this.csRadius = PathLossEngine.INSTANCE.getDistance(device.frequency, 
                device.txPower - device.carrierSenseRss, device.txAntHeight, device.rxAntHeight);;
        this.intRadius = PathLossEngine.INSTANCE.getDistance(device.frequency, 
                device.txPower - device.interferenceRss, device.txAntHeight, device.rxAntHeight);;
        
        //System.out.printf("commRadius=%.4f, csRadius=%.4f, intRadius=%.4f\n", commRadius, csRadius, intRadius);
                
        this.rewardFunc = rewardFunc;
    }
    
    public List<NodeChannelPair> getAvaialbleNcPairList(){
        List<NodeChannelPair> ncPairList = new ArrayList<>();
        
        if ( isMaxDemandMet ){
            return ncPairList;
        }
        
        if ( numOfAssignedChns == 0 ){
            // Loop over each startChn
            for ( int startChn = 0; startChn <= Channel.TOTAL_NUM_OF_CHANNELS-1; startChn ++){
                if ( availableChns[startChn] ){
                    // Loop over all demands (i.e., requested number of contiguous channels )
                    for ( int i = 0; i < cbsdDevice.deviceDemand.size(); i++ ){
                        int demand = cbsdDevice.deviceDemand.get(i);
                        int minDemand = cbsdDevice.deviceDemand.getMinDemand();
                        int endChn = startChn + demand - 1;
                        
                        if ((endChn <= Channel.TOTAL_NUM_OF_CHANNELS-1) 
                                && checkAvailability(startChn, endChn) ){ // [startChn, endChn], inclusive
                            // Nodes
                            List<Integer> nodeIdxList = new ArrayList<>();
                            nodeIdxList.add(nodeIdx);
                            
                            // Channel
                            Channel channel = new Channel(startChn, endChn);
                            
                            // Node-channel pair
                            ncPairList.add( new NodeChannelPair(nodeIdxList, channel, demand == minDemand, rewardFunc));
                        }
                        
                    }
                }
            }
        }else{
            for ( int i = 0 ; i < cbsdDevice.deviceDemand.size(); i++ ){
                int demand = cbsdDevice.deviceDemand.get(i);
                
                if ( demand > numOfAssignedChns ){
                    // Case 1: [endAssignedChn-demand+1, startAssignedChn-1] + assigned channels
                    int startChn = endAssignedChn - demand + 1;
                    int endChn = startAssignedChn - 1;
                    
                    if ((startChn >= 0) && checkAvailability(startChn, endChn) ){
                        List<Integer> nodeIdxList = new ArrayList<>();
                        nodeIdxList.add(nodeIdx);
                        Channel channel = new Channel( startChn, endChn);
                        ncPairList.add( new NodeChannelPair( nodeIdxList, channel, false, rewardFunc) );
                    }
                    
                    // Case 2: assigned channels + [endAssignedChn+1, startAssignedChn+demand-1]
                    startChn = endAssignedChn + 1;
                    endChn = startAssignedChn + demand - 1;
                    
                    if ((endChn <= Channel.TOTAL_NUM_OF_CHANNELS - 1) && checkAvailability(startChn, endChn) ){
                        List<Integer> nodeIdxList = new ArrayList<>();
                        nodeIdxList.add(nodeIdx);
                        Channel channel = new Channel( startChn, endChn);
                        ncPairList.add( new NodeChannelPair( nodeIdxList, channel, false, rewardFunc) );
                    }
                    
                }
            }
        }
        
        return ncPairList;
    }
    
    public boolean checkAvailability( int startChn, int endChn){
        for (int chn = startChn; chn <= endChn; chn++){
            if ( !availableChns[chn] ){
                return false;
            }
        }
        return true;
    }

    public int getNumOfAssignedChns(){
        return numOfAssignedChns;
    }
    
    public boolean isMaxDemandMet(){
        return isMaxDemandMet;
    }
    
    public int getNodeIdx(){
        return nodeIdx;
    }
    
    public CBSDDevice getCbsdDevice(){
        return cbsdDevice;
    }
    
    public double getActivityIdx(int chId){
        int numOfChns = Channel.findEndChn(chId) - Channel.findStartChn(chId) + 1;
        return Math.min(1.0, activityIdx/numOfChns);
    }
    
    public int getMinDemand(){
        return cbsdDevice.deviceDemand.getMinDemand();
    }
    
    public boolean isCoexEnabled(){
        return coexEnabled;
    }
    
    public void print(){
        System.out.print("\tNode: " + nodeIdx + ", ");
        System.out.print("availableChns: " + Arrays.toString(availableChns) + ", ");
        System.out.print("isMaxDemandMet: " + isMaxDemandMet + ", ");
        System.out.print("assignedChns: " + Arrays.toString(assignedChns) + "\n");
    }
    
}
