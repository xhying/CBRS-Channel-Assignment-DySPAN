/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package spectrumaccesssystem.algo.pathloss;

//import cbsdemulator.utils.LogUtils;

import spectrumaccesssystem.utils.LogUtils;


/**
 *
 * @author dbserver
 */
public class PropagationModel {
    
    private final String configFilePath;
    
    public enum Models {
        FREE_SPACE, SUI, COST231, HATA_OKUMURA, ERICSSON;
    };
    
    private Models model;
    private PropagationModel modelObject;

    public PropagationModel() {
        this.configFilePath = null;
        
//        this.model = Models.FREE_SPACE;
//        this.modelObject = new FreeSpaceModel();        
    }
    
    public PropagationModel(String configFilePath, String modelName) {       
        this.configFilePath = configFilePath;
        
        //String modelName = "SUI";
                
        switch (modelName) {
            case "FREE_SPACE":
                this.model = Models.FREE_SPACE;
                this.modelObject = new FreeSpaceModel(configFilePath);
                break;
            case "SUI":
                this.model = Models.SUI;
                this.modelObject = new SUIModel(configFilePath);
                break;
            case "COST231":
                this.model = Models.COST231;
                this.modelObject = new COST231Model(configFilePath);
                break;
            case "HATA_OKUMURA":
                this.model = Models.HATA_OKUMURA;
                this.modelObject = new HataOkumuraModel(configFilePath);
                break;
            case "ERICSSON":
                this.model = Models.ERICSSON;
                this.modelObject = new EricssonModel(configFilePath);
                break;
            default:
                
                break;
        }
        
    }
    
    // frequency in MHz, distance in km, txAntHeight and rxAntHeight in meter
    public double getPathLoss(double frequency, double distance, double txAntHeight, double rxAntHeight) {
        double pathLoss = 0.0;
        
        switch (this.model) {
            case FREE_SPACE:
                FreeSpaceModel freeSpaceModel = (FreeSpaceModel)modelObject;
                pathLoss = freeSpaceModel.calculatePathLoss(frequency, distance, txAntHeight, rxAntHeight);
                break;
            case SUI:
                SUIModel suiModel = (SUIModel)modelObject;
                pathLoss = suiModel.calculatePathLoss(frequency, distance, txAntHeight, rxAntHeight);
                break;
            case COST231:
                COST231Model cost231Model = (COST231Model)modelObject;
                pathLoss = cost231Model.calculatePathLoss(frequency, distance, txAntHeight, rxAntHeight);
                break;
            case HATA_OKUMURA:
                HataOkumuraModel hataOkumuraModel = (HataOkumuraModel)modelObject;
                pathLoss = hataOkumuraModel.calculatePathLoss(frequency, distance, txAntHeight, rxAntHeight);                
                break;
            case ERICSSON:
                EricssonModel ericssonModel = (EricssonModel)modelObject;
                pathLoss = ericssonModel.calculatePathLoss(frequency, distance, txAntHeight, rxAntHeight);                                
                break;
            default:
                
                break;
        }

        return pathLoss;
    }
    
    // frequency in MHz, distance in km, txAntHeight and rxAntHeight in meter. (Shaun)
    public double getDistance(double pathLoss, double frequency, double txAntHeight, double rxAntHeight) {
        double distance = 0.0;
        
        switch (this.model) {
            case SUI:
                SUIModel suiModel = (SUIModel)modelObject;
                distance = suiModel.calculateDistance(pathLoss, frequency, txAntHeight, rxAntHeight);
                break;
            case COST231:
                COST231Model cost231Model = (COST231Model)modelObject;
                distance = cost231Model.calculateDistance(pathLoss, frequency, txAntHeight, rxAntHeight);
                break;
            default:
                LogUtils.INSTANCE.writeLog("SEVERE", "getDistance() undefined for model " + this.model);
                break;
        }

        return distance;
    }
    
    public double calculatePathLoss(double frequency, double distance, double txAntHeight, double rxAntHeight) {return 0;}
    
    public double calculateDistance(double frequency, double desiredPathLoss, double txAntHeight, double rxAntHeight){return 0;}
    
    public static class FreeSpaceModel extends PropagationModel {
        
        public FreeSpaceModel(String configFilePath) {
            // Read config for the selected model
//            LogUtils.INSTANCE.writeLog("WARNING", "FreeSpaceModel()");            
        }
        
         // f in MHz, d in km, h_b in meter, h_r in meter
        @Override
        public double calculatePathLoss(double f, double d, double h_b, double h_r) {
            double pathLoss = (20.0*Math.log10(d)) + (20.0*Math.log10(f)) + 32.45;
                    
            return pathLoss;
        }
    }
    
    public static class SUIModel extends PropagationModel {
        
        private final double d0 = 0.1; // km
        private final double v = 3e8; // speed of light in m/s
        
        // Terrain model parameters
        private final double[] a = {4.6, 4.0, 3.6};
        private final double[] b = {0.0075, 0.0065, 0.005};
        private final double[] c = {12.6, 17.1, 20.0};
        private final double[] alpha = {6.6, 5.2, 5.2};
        private final double[] s = {10.6, 8.2, 8.2};

        private int m = 0; // Model type [A=0,B=1,C=2]
        
        public SUIModel(String configFilePath) {
//            LogUtils.INSTANCE.writeLog("WARNING", "SUIModel()");     

            // Read config for the selected model
            m = 0;
        }
        
         // f in MHz, d in km, h_b in meter, h_r in meter
        @Override
        public double calculatePathLoss(double f, double d, double h_b, double h_r) {
            double lambda = v/(f*10e6);
            double A = 20*Math.log10((4.0*Math.PI*d0)/lambda);
            
            double gamma = a[m] - b[m]*h_b + c[m]/h_b;
            
            double X_f = 6.0*Math.log10(f/2000.0);
            
            double X_h;            
            if (m < 2) { // m = 0 or 1
                X_h = -10.8*Math.log10(h_r/2000.0);
            } else {
                X_h = -20.0*Math.log10(h_r/2000.0);
            }
            
//            double s = 0.65*Math.pow(Math.log10(f), 2.0) - 1.3*Math.log10(f) + alpha[m];
            
            d = Math.max(d, d0); // d should be at least d0, and km -> m
            
            double pathLoss = A + (10.0*gamma*Math.log10(d/d0)) + X_f + X_h + s[m];
                    
            return pathLoss;
        }
        
        // pathloss in db, f in MHz, h_b in meter, h_r in meter
        @Override
        public double calculateDistance(double f, double pathLoss, double h_b, double h_r) {
            double lambda = v/(f*10e6);
            double A = 20*Math.log10((4.0*Math.PI*d0)/lambda);
            
            double gamma = a[m] - b[m]*h_b + c[m]/h_b;
            
            double X_f = 6.0*Math.log10(f/2000.0);
            
            double X_h;            
            if (m < 2) { // m = 0 or 1
                X_h = -10.8*Math.log10(h_r/2000.0);
            } else {
                X_h = -20.0*Math.log10(h_r/2000.0);
            }
            
            double d = d0 * Math.pow(10, (pathLoss - (A + X_f + X_h + s[m]))/(10.0*gamma));
            
            return Math.max(d, d0);
        }
    }      
    
    public static class COST231Model extends PropagationModel {

        private final String citySize; // "small", "medium", "large"
        private final String terrainType; // "urban", "suburban", "rural"
                
        public COST231Model(String configFilePath) {
//            LogUtils.INSTANCE.writeLog("WARNING", "COST231Model()");     

            // Read config for the selected model
            citySize = "large";
            terrainType = "urban";
        }
        
         // f in MHz, d in km, h_b in meter, h_r in meter
        @Override
        public double calculatePathLoss(double f, double d, double h_b, double h_r) {            
            double a_h_r;
            
            if (citySize.equals("large") == true) {
                a_h_r = 3.20*Math.pow(Math.log10(11.75*h_r), 2.0) - 4.97;
            } else {
                a_h_r = (1.1*Math.log10(f) - 0.7)*h_r - (1.56*Math.log10(f) - 0.8);
            }
            
            double c_m = 0.0;
            if (terrainType.equals("urban") == true) {
                c_m = 3.0;
            }
            
            d = Math.max(d, 0.01); // d should be at least 0.01 km

            double pathLoss = 46.3 + (33.9*Math.log10(f)) - (13.82*Math.log10(h_b))
                    - a_h_r + ((44.9 - 6.55*Math.log10(h_b))*Math.log10(d)) + c_m;

/*            
            if (terrainType.equals("suburban") == true) {
                pathLoss = pathLoss - 2.0*Math.pow(Math.log10(f/28.0), 2) - 5.4;
            } else if (terrainType.equals("rural") == true) {
                pathLoss = pathLoss - 4.78*Math.pow(Math.log10(f), 2) + 18.33*Math.log10(f) - 40.94;                
            }
*/
            
            return pathLoss;
        }       
        
                
        // pathloss in db, f in MHz, h_b in meter, h_r in meter
        @Override
        public double calculateDistance(double f, double pathLoss, double h_b, double h_r) {
            double a_h_r;
            
            if (citySize.equals("large") == true) {
                a_h_r = 3.20*Math.pow(Math.log10(11.75*h_r), 2.0) - 4.97;
            } else {
                a_h_r = (1.1*Math.log10(f) - 0.7)*h_r - (1.56*Math.log10(f) - 0.8);
            }
            
            double c_m = 0.0;
            if (terrainType.equals("urban") == true) {
                c_m = 3.0;
            }
            
            double d_log10 = (pathLoss - (46.3 + (33.9*Math.log10(f)) - (13.82*Math.log10(h_b))
                    - a_h_r + c_m))/(44.9 - 6.55*Math.log10(h_b));
            
            return Math.pow(10.0, d_log10);       // d should be at least 0.01 km
        }
    }    
    
    public static class HataOkumuraModel extends PropagationModel {

        private final String citySize; // "medium", "large"
        
        public HataOkumuraModel(String configFilePath) {
//            LogUtils.INSTANCE.writeLog("WARNING", "HataOkumuraModel()");
            
            // Read config for the selected model
            citySize = "large";            
        }
        
         // f in MHz, d in km, h_b in meter, h_r in meter
        @Override
        public double calculatePathLoss(double f, double d, double h_b, double h_r) {
            f /= 1000.0; // MHz -> GHz
            
            d = Math.max(d, 0.01); // d should be at least 0.01 km
//            LogUtils.INSTANCE.writeLog("WARNING", "d=" + d);
            
            double A_fs = 92.4 + (20.0*Math.log10(d)) + (20.0*Math.log10(f));
            double A_bm = 20.41 + (9.83*Math.log10(d)) + (7.894*Math.log10(f)) + (9.56*Math.pow(Math.log10(f), 2.0));
            double G_b = Math.log10(h_b/200)*(13.958 + 5.8*Math.pow(Math.log10(d), 2.0));
            
            double G_r;
            if (citySize.equals("medium") == true) {
                G_r = (42.57 + 13.7*Math.log10(f))*(Math.log10(h_r) - 0.585);
            } else {
                G_r = 0.759*h_r - 1.862;
            }

//            LogUtils.INSTANCE.writeLog("WARNING", "A_fs=" + A_fs + ",A_bm=" + A_bm
//            + ",G_b=" + G_b + ",G_r=" + G_r);
            
            double pathLoss = A_fs + A_bm - G_b - G_r;
            
            return pathLoss;
        }        
    }
    
    public static class EricssonModel extends PropagationModel {

        // Terrain model parameters
        private final double[] a0 = {36.2, 43.20, 45.95};
        private final double[] a1 = {30.2, 68.93, 100.6};
        private final double[] a2 = {-12.0, -12.0, -12.0};
        private final double[] a3 = {0.1, 0.1, 0.1};

        private final int t; // "urban = 0", "suburban=1", "rural=2"

        public EricssonModel(String configFilePath) {
//            LogUtils.INSTANCE.writeLog("WARNING", "EricssonModel()");
            
            // Read config for the selected model
            t = 0;
        }
        
         // f in MHz, d in km, h_b in meter, h_r in meter
        @Override
        public double calculatePathLoss(double f, double d, double h_b, double h_r) {    
            double g_f = (44.49*Math.log10(f)) - (4.78*Math.pow(Math.log10(f), 2.0));
                    
            d = Math.max(d, 0.01); // d should be at least 0.01 km
            
            double pathLoss = a0[t] + a1[t]*Math.log10(d) + a2[t]*Math.log10(h_b) + a3[t]*Math.log10(h_b)*Math.log10(d) 
                    - 3.2*Math.pow(Math.log10(11.75*h_r), 2.0) + g_f;
            
            return pathLoss;
        }        
    }       
}
