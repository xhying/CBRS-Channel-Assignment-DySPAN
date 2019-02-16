/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.utils;

import java.io.Serializable;

/**
 *
 * @author changki
 */
public class GeoUtils {
    
    public GeoUtils() {
        
    }

    /**
     * Get latitude and longitude of the destination from distance with azimuth
     * @param lat
     * @param lng
     * @param distance
     * @param azimuth
     * @return 
     */
    public static Location getDestPoint(double lat, double lng, double distance, double azimuth) {
        double radius = 6371; // km
        
        double RadLat = deg2rad(lat);
        double RadLng = deg2rad(lng);
        
        double radAzi = deg2rad(azimuth);
        
        double DestLat = Math.asin(Math.sin(RadLat) * Math.cos(distance/radius) 
                + Math.cos(RadLat) * Math.sin(distance/radius) * Math.cos(radAzi));
        
        double DestLng = RadLng + Math.atan2(Math.sin(radAzi) * Math.sin(distance/radius) * Math.cos(RadLat),
                Math.cos(distance/radius) - Math.sin(RadLat) * Math.sin(DestLat));

        Location loc = new Location();
        
        loc.latitude = rad2deg(DestLat);
        loc.longitude = rad2deg(DestLng);
        
        return loc;
    }

    /**
     * Get distance between two points in km
     * @param srcLat
     * @param srcLng
     * @param destLat
     * @param destLng
     * @return 
     */
    public static double getDistance(double srcLat, double srcLng, double destLat, double destLng) {
        double theta = srcLng - destLng;
        double dist = Math.sin(deg2rad(srcLat)) * Math.sin(deg2rad(destLat)) 
                + Math.cos(deg2rad(srcLat)) * Math.cos(deg2rad(destLat)) * Math.cos(deg2rad(theta));
        
        dist = rad2deg(Math.acos(dist));
        
        dist = dist * 111.1896;
        
        return dist;
    }
    
    /**
     * Convert decimal degrees to radians
     * @param input parameter double - deg
     * @return double - radians.  
     */
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /**
     * Convert radians to decimal degrees 
     * @param input parameter double - rad
     * @return double - degrees.  
     */
    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }    
    
    public static class Location implements Serializable {
        public double latitude;
        public double longitude;
    }
}
