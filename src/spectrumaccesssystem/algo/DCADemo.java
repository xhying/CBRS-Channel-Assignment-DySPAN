/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo;

import javafx.application.Application;
import javafx.stage.Stage;
import spectrumaccesssystem.algo.pathloss.PathLossEngine;
import spectrumaccesssystem.algo.simulation.*;
import spectrumaccesssystem.utils.LogUtils;

/**
 *
 * @author Xuhang Ying <xuhang.1.ying@nokia.com>
 */
public class DCADemo extends Application{
    private final boolean isSimulationEnabled = true;
    private final boolean isDebug = true;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            //LogUtils.INSTANCE.initialize(logPane, logLevel); // Initialize Logger Class
            LogUtils.INSTANCE.initialize( null, "INFO"); // Initialize Logger Class
        } catch(NullPointerException e) {
            System.out.println(e.getMessage());
            return;
        }
        
        if (isSimulationEnabled){
            //PathLossEngine.INSTANCE.test();
            runSimulation();
            System.exit(0);
        }
    }
    
    private void runSimulation(){
        Simulation testSimulation = new TestSimulation(isDebug);
        testSimulation.start();
    }
    
}
