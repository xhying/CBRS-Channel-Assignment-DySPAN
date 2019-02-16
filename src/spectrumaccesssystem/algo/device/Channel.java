/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.device;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Xuhang Ying <xuhang.1.ying@nokia.com>
 */
public class Channel {
    public static int TOTAL_NUM_OF_CHANNELS = 15;
    public static int INVALID_CHANNEL_ID = -1;
    
    /*
        Channel is a set of contiguous primitive 10 MHz channels.
    */
    private int startChn;
    private int endChn;
    private int numOfChns;
    private int chId;
    
    public Channel(){
        this.startChn = 0;
        this.endChn = 0;
        this.numOfChns = 0;
        this.chId = 0;
    }
    
    public Channel( int startChn, int endChn){
        this.startChn = startChn; 
        this.endChn = endChn;
        this.numOfChns = endChn - startChn + 1;
        this.chId = findChId( startChn, endChn );
    }
    
    /*
        Static methods.
    */
    
    public static int findChId ( int startChn, int endChn){
        if ((startChn > endChn) || (startChn < 0) || (endChn >= TOTAL_NUM_OF_CHANNELS)){
            throw new IllegalArgumentException("Error: startChn = " + startChn + ", endChn = " + endChn);
        }
        return startChn * TOTAL_NUM_OF_CHANNELS + endChn;
    }
    
    public static int findStartChn (int chId){
        return chId / TOTAL_NUM_OF_CHANNELS;
    }
    
    public static int findEndChn (int chId){
        return chId % TOTAL_NUM_OF_CHANNELS;
    }
    
    /*
        Public methods.
    */
    
    public boolean isConflicting(Channel channel){      // Co-channel
        return !(( this.endChn < channel.startChn) || ( channel.endChn < this.startChn));
    }
    
    public boolean isConflicting(Channel channel, int minChnDist){
        if ( minChnDist >= 0 ){
            int startChn1 = this.getStartChn();
            int endChn1 = this.getEndChn();
            
            int startChn2 = channel.getStartChn();
            int endChn2 = channel.getEndChn();
            
            startChn1 = Math.max(0, startChn1 - minChnDist);
            endChn1   = Math.min(TOTAL_NUM_OF_CHANNELS-1, endChn1 + minChnDist);
            
            return(!((endChn2 < startChn1) || (endChn1 < startChn2)));
        }else{
            return false;
        }
    }
    
    public int getStartChn (){
        return this.startChn;
    }
    
    public int getEndChn(){
        return this.endChn;
    }
    
    public int getNumOfChns(){
        return this.numOfChns;
    }
    
    public int getChId(){
        return this.chId;
    }
    
    public List<Integer> getChnList(){
        List<Integer> chnList = new ArrayList<>();
        for(int chn = startChn; chn <= endChn; chn++){
            chnList.add(chn);
        }
        return(chnList);
    }
    
    public void setChns( int startChn, int endChn ){
        this.startChn = startChn; 
        this.endChn = endChn;
        this.numOfChns = endChn - startChn + 1;
        this.chId = findChId( startChn, endChn );
    }
    
    public void setChId (int chId){
        int m_startChn = chId / TOTAL_NUM_OF_CHANNELS;
        int m_endChn = chId % TOTAL_NUM_OF_CHANNELS;
        if ((m_startChn > m_endChn) || (m_startChn < 0) || ( m_endChn >= TOTAL_NUM_OF_CHANNELS)){
            throw new IllegalArgumentException("Invalid startChn or endChn");
        }else{
            this.startChn = m_startChn;
            this.endChn = m_endChn;
            this.numOfChns = endChn - startChn + 1;
            this.chId = chId;
        }
    }
}
