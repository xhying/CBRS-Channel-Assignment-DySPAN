/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.solver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author changki
 */
public class TimeUtils {
    private long startTime;
    private long endTime;
    
    public enum timeUnit {
        DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS, MICROSECONDS;
    }
    
    public TimeUtils () {
        startTime = 0;
        endTime = 0;
    }
    
    public void startTimer() {
        startTime = System.nanoTime();
    }
    
    public void stopTimer() {
        endTime = System.nanoTime();
    }
 
    public double getTimeDiff(timeUnit unit) {
        long elapsedTime = endTime - startTime;

        double time;
        
        switch (unit) {
            case DAYS :
                time = (double)elapsedTime / 1000000000.0;
                time = time / (60*60*24);
            break;
            case HOURS :
                time = (double)elapsedTime / 1000000000.0;
                time = time / (60*60);
            break;
            case MINUTES :
                time = (double)elapsedTime / 1000000000.0;
                time = time / 60;
            break;
            case SECONDS :
                time = (double)elapsedTime / 1000000000.0;
            break;
            case MILLISECONDS :
                time = (double)elapsedTime / 1000000.0;
            break;
            case MICROSECONDS :
                time = (double)elapsedTime / 1000.0;
            break;
            default : // Milli Seconds
                time = (double)elapsedTime / 1000000.0;
        }
            
        // Return in Milli Seconds
        return time;
    }
    
    public static String getCurrentDateTime(boolean isIso8601) {
        DateFormat dateFormat;
        
        if (isIso8601 == true) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // ISO 8601 Format in RFC 3339
        } else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        // Get Current Date & Time
        Calendar cal = Calendar.getInstance();
        
        return dateFormat.format(cal.getTime());
    }
    
    public static String getCurrentDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }
    
    public static String getFutureTime(timeUnit unit, int period, boolean isIso8601) {
        DateFormat dateFormat;
        
        if (isIso8601 == true) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // ISO 8601 Format in RFC 3339
        } else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        
        // Get Current Date & Time
        Calendar cal = Calendar.getInstance();
//        LogUtils.INSTANCE.writeLog("INFO", dateFormat.format(cal.getTime()));

        switch (unit) {
            case DAYS :
                cal.add(Calendar.DAY_OF_MONTH, period);
            break;
            case HOURS :
                cal.add(Calendar.HOUR_OF_DAY, period);
            break;
            case MINUTES :
                cal.add(Calendar.MINUTE, period);
            break;
            default : // Hour
                cal.add(Calendar.HOUR_OF_DAY, period);
        }
        
        String futureTime = dateFormat.format(cal.getTime());
//        LogUtils.INSTANCE.writeLog("INFO", futureTime);

        return futureTime;
    }
    
    public static String parseIso8601Date(String input) {
        String output;
        
        String tmp = input.replace("T", " ");
        output = tmp.replace("Z", "");
        
        return output;
    }
}
