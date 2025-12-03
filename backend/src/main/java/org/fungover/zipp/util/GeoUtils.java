package org.fungover.zipp.util;

public class GeoUtils {
    // Mean Earth radius in meters according to WGS84 standard
    private static final double EARTH_RADIUS = 6371000.0;

    private GeoUtils() {}

    public static double distanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a =
            Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) *
                    Math.cos(Math.toRadians(lat2)) *
                    Math.sin(lonDistance / 2) *
                    Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

}
