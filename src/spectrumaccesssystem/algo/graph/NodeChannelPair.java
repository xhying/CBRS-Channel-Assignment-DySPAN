/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.graph;

import java.util.List;
import spectrumaccesssystem.algo.DCAConstants;
import spectrumaccesssystem.algo.device.Channel;

/**
 *
 * @author Xuhang Ying <xuhang.1.ying@nokia.com>
 */
public class NodeChannelPair {
    
    private int ncPairIdx;
    private final List<Integer> nodeIdxList;
    private final Channel channel; 
    private final boolean isMinDemand;
    private final String rewardFunc; 
    
    public NodeChannelPair(){
        this.nodeIdxList = null;
        this.channel = null;
        this.isMinDemand = false;
        this.rewardFunc = null;
    }
    
    public NodeChannelPair(List<Integer> nodeIdxList, Channel channel, 
            boolean isMinDemand, String rewardFunc){
        this.nodeIdxList = nodeIdxList;
        this.channel = channel;
        this.isMinDemand = isMinDemand;
        this.rewardFunc = rewardFunc;
    }
    
    public boolean containsNode (int nodeIdx){
        return nodeIdxList.stream().anyMatch((idx) -> ( idx == nodeIdx ));
    }
    
    public double getReward(){
        double w = 0.0;
        
        switch ( rewardFunc ){
            case DCAConstants.REWARD_FUNC_LINEAR:
                w = 1.0 * nodeIdxList.size() * channel.getNumOfChns();
                break;
            case DCAConstants.REWARD_FUNC_LOG:
                w = 1.0 * nodeIdxList.size() * ( 1.0 + Math.log( channel.getNumOfChns() ));
                break;
            default:
                break;
        }
        
        return w;
    }
    
    /*
        Getters.
    */
    
    public int getNcPairIdx(){
        return this.ncPairIdx;
    }
    
    public int getNodeListSize(){
        return this.nodeIdxList.size();
    }
    
    public List<Integer> getNodeIdxList(){
        return this.nodeIdxList;
    }
    
    public Channel getChannel(){
        return this.channel;
    }
    
    public boolean isMinDemand(){
        return this.isMinDemand;
    }
    
    /*
        Setters
    */
    
    public void setNcPairIdx(int ncPairIdx){
        this.ncPairIdx = ncPairIdx;
    }
    
    public void print(){
        String str = "\t";
        str += String.format("Idx: %d, (%s, [%d,%d]), ", 
                this.ncPairIdx, 
                this.getNodeIdxList().toString(), 
                channel.getStartChn(), 
                channel.getEndChn());
        str += String.format("Reward: %.1f, ", getReward());
        str += String.format("isMinDemand: %5s, ", isMinDemand);
        str += "\n";
        System.out.print(str);
    }
}
