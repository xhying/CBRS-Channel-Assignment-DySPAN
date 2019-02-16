/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.pathloss;

/**
 *
 * @author Xuhang Ying <xhying@uw.edu>
 */
public enum PathLossEngine {
    INSTANCE;
    
    // COST231, SUI
    private static PropagationModel model = new PropagationModel("", "SUI");
    
    // private final int startFreq = 3550; // MHz
    // private final int endFreq = 3700;   // MHz
    
    public void configModel(String modelName){
        model = new PropagationModel("", modelName);
    }
    
    // frequency in MHz, distance in km, txAntHeight and rxAntHeight in meter
    public double getPathLoss(double frequency, double distance, double txAntHeight, double rxAntHeight) {
        return( model.getPathLoss(frequency, distance, txAntHeight, rxAntHeight));
    }
    
    public double getDistance(double frequency, double pathloss, double txAntHeight, double rxAntHeight){
        return ( model.getDistance(frequency, pathloss, txAntHeight, rxAntHeight));
    }
    
    public void test(){
        double frequency = (3550.0 + 3700.0)/2;     // in MHz
        double txAntHeight = 3;             // in meters
        double rxAntHeight = 1.5;           // in meters
        
        double[] d = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8};
        double[] pathLoss = new double[d.length];
        
        for (int i = 0; i < d.length; i++){
            pathLoss[i] = getPathLoss(frequency, d[i], txAntHeight, rxAntHeight);
            System.out.printf("d = %.2f (km), pl = %.2f (dB)\n", d[i], pathLoss[i]);
        }
        
        pathLoss = new double[]{30 + 72, 30 + 80, 30 + 96};
        d = new double[pathLoss.length];
        
        System.out.println("test test");
        
        double[] pathLoss2 = new double[pathLoss.length];
        
        for (int i = 0; i < pathLoss.length; i++){
            System.out.println("test test2");
            d[i] = getDistance(frequency, pathLoss[i], txAntHeight, rxAntHeight);
            pathLoss2[i] = getPathLoss(frequency, d[i], txAntHeight, rxAntHeight);
            System.out.printf("pl = %.2f (dB), d = %.4f (km) -> pl2 = %.2f (dB)\n", 
                    pathLoss[i], d[i], pathLoss2[i]);
        }
        
    }
    
}
