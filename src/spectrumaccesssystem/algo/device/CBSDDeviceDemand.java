/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.device;

/**
 *
 * @author Xuhang Ying <xuhang.1.ying@nokia.com>
 */
public class CBSDDeviceDemand {
    
    private int minDemand;
    private int maxDemand;
    private int[] demand;   // A set of acceptable non-zero channel widths;
    
    public CBSDDeviceDemand(){
        this.minDemand = 1;
        this.maxDemand = 4;
        this.demand = new int[]{1,2,3,4};
    }
    
    public CBSDDeviceDemand(int minDemand, int maxDemand, int stepSize){
        if ( minDemand > maxDemand || stepSize < 0 ){
            throw new IllegalArgumentException("Valid demand range: 0 < min <= max and unit > 0");
        }
        
        this.demand = new int[ (maxDemand-minDemand)/stepSize + 1 ];
        
        
        for (int i = 0; i < this.demand.length; i++){
            this.demand[i] = minDemand + stepSize * i;
        }
        
        this.minDemand = this.demand[0];
        this.maxDemand = this.demand[ this.demand.length - 1 ];
    }
    
    public int getMinDemand(){
        return (this.minDemand);
    }
    
    public int getMaxDemand(){
        return (this.maxDemand);
    }
    
    public int[] getDemand(){
        return (this.demand);
    }
    
    public int size(){
        return demand.length;
    }
    
    public int get(int idx){
        return demand[idx];
    }
    
    public void print(){
        if (demand == null){
            System.out.println("Demand is null");
        }else{
            System.out.print("D = [");
            for (int i=0; i<demand.length; i++){
                System.out.print(demand[i] + ",");
            }
             System.out.print("], ");   
        }
    }
}
