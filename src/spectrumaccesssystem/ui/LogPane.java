/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package spectrumaccesssystem.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 *
 * @author dbserver
 */
public class LogPane {

    final TextArea logText = new TextArea ("");
    
    public Pane createLogPane() {
        BorderPane border = new BorderPane();
        border.setPadding(new Insets(20, 10, 20, 10));
               
        border.setLeft(addVBox());
        border.setCenter(logText);

        return border;
    }

    private VBox addVBox() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(0, 20, 0, 0));
        vbox.setSpacing(20);
        
        final Label titleLabel = new Label("Log Message");
        titleLabel.setFont(new Font("Tahoma", 20));
        
        Button resetLogBt = new Button("Reset Log");
        resetLogBt.setMaxWidth(Double.MAX_VALUE);

        Button saveLogBt = new Button("Save As..");
        saveLogBt.setMaxWidth(Double.MAX_VALUE);
        saveLogBt.setDisable(true);
        
        resetLogBt.setOnAction((ActionEvent ae) -> {
            logText.clear();
        });
                
        vbox.getChildren().addAll(titleLabel, resetLogBt, saveLogBt);
        
        return vbox;
    } 
 
    public void updateLogMsg(String msg) {
        Platform.runLater(() -> {
            logText.appendText(msg + "\n");
        });
    }
}
