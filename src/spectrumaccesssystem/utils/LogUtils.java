/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spectrumaccesssystem.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import spectrumaccesssystem.ui.LogPane;

/**
 *
 * @author changki
 */
public enum LogUtils {
    INSTANCE;
    
    private static Logger logger;
    private static FileHandler fh;
    private LogPane gui;
    private boolean isGuiEnabled = false;
    
    public void initialize(String level){
        this.isGuiEnabled = false;
        this.initialize(null, level);
    }
    
    //public void initialize(LogPane ui, String level) {
    public void initialize(LogPane ui, String level) {
        logger = Logger.getLogger(getClass().getName());

        setLevel(level);

        this.gui = ui;
                
        try {
//            fh = new FileHandler("C:\\temp\\server.log");
            fh = new FileHandler("./log/dca.log");
            
            LogFormatter formatter = new LogFormatter();
            fh.setFormatter(formatter);

            logger.addHandler(fh);
        } catch (SecurityException | IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void writeLog(String level, String msg) {
        StackTraceElement myCaller = Thread.currentThread().getStackTrace()[2];
        
//        if (level.equals("INFO") == false) {
            msg = "[" + myCaller.getClassName() + ":" + myCaller.getMethodName() + "():" + myCaller.getLineNumber() +"] " + msg;
//        }
        
        Level loggerLevel = Level.SEVERE;
        
        switch (level) {
            case "INFO":
                logger.info(msg);
                loggerLevel = Level.INFO;                
                break;
            case "WARNING":
                logger.warning(msg);
                loggerLevel = Level.WARNING;                
                break;
            case "SEVERE":
                logger.severe(msg);
                loggerLevel = Level.SEVERE;                
                break;
        }
        
        if (loggerLevel.intValue() >= logger.getLevel().intValue()) {
            if (isGuiEnabled) gui.updateLogMsg(msg);
        }
    }
    
    private void setLevel(String level) {
        switch (level) {
            case "INFO":
                logger.setLevel(Level.INFO);
                break;
            case "WARNING":
                logger.setLevel(Level.WARNING);
                break;
            case "SEVERE":
                logger.setLevel(Level.SEVERE);
                break;
        }
    }
    
    private final class LogFormatter extends Formatter {

        private final String LINE_SEPARATOR = System.getProperty("line.separator");

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            Date date = new Date(record.getMillis());
            
            sb.append(date.toString())
                .append(" [")
                .append(record.getLevel().getLocalizedName())
                .append("] ")
                .append(formatMessage(record))
                .append(LINE_SEPARATOR);

            if (record.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception e) {
                    // ignore
                }
            }

            return sb.toString();
        }
    }    
}
