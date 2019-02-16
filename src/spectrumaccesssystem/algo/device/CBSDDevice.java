/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.device;

import java.util.Set;

/**
 *
 * @author Xuhang Ying <xuhang.1.ying@nokia.com>
 */
public class CBSDDevice {
    public int cbsdId;
    
    public double lat;
    public double lng; 
    
    public double txPower;      // Unit in dBm
    public double txAntHeight;    // Unit in meters
    public double rxAntHeight = 1.5;                // in meters
    public double frequency = (3550.0 + 3700.0)/2;  // in MHz
    
    public boolean[] availableChns;
    public boolean[] assignedChns;
    
    public double activityIndex;
    public boolean coexistenceEnabled; 
    
    public CBSDDeviceDemand deviceDemand;
    
    public final double communicationRss = -96;
    public final double carrierSenseRss = -72;
    public final double interferenceRss = -80;
    
    public CBSDDevice(){
        this.cbsdId = 0;
        this.lat = 0.0;
        this.lng = 0.0;
        this.txPower = 0.0;
        this.txAntHeight = 0.0;
        this.availableChns = null;
        this.assignedChns = null;
        this.deviceDemand = null;
        this.activityIndex = 0.0;
        this.coexistenceEnabled = false;
    }
    
    public CBSDDevice(int cbsdId, double lat, double lng, double txPower, double txAntHeight, 
            boolean[] availableChns, boolean[] assignedChns,
            CBSDDeviceDemand deviceDemand, double activityIndex, boolean coexistenceEnabled){
        this.cbsdId = cbsdId;
        this.lat = lat;
        this.lng = lng;
        this.txPower = txPower;
        this.txAntHeight = txAntHeight;
        
        this.availableChns = new boolean[Channel.TOTAL_NUM_OF_CHANNELS];
        this.assignedChns = new boolean[Channel.TOTAL_NUM_OF_CHANNELS];
        
        System.arraycopy( availableChns, 0, this.availableChns, 0, availableChns.length );
        System.arraycopy( assignedChns, 0, this.assignedChns, 0, assignedChns.length );
        
        this.deviceDemand = deviceDemand;
        this.activityIndex = activityIndex;
        this.coexistenceEnabled = coexistenceEnabled;
    }
}
