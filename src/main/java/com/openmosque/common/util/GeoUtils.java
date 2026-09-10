package com.openmosque.common.util;

import com.openmosque.common.exception.BadRequestException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Utility class for Spatial Geography and Coordinate conversions.
 * 
 * WHY THIS IS PRESENT:
 * 1. PostGIS SRID 4326: Standard WGS84 spatial reference system used worldwide by GPS and Google Maps.
 * 2. Point Factory: Converts (latitude, longitude) doubles into JTS Geometry Points for Hibernate Spatial.
 * 3. Validation: Ensures coordinates are within valid geographic bounds (-90 to +90 lat, -180 to +180 lng).
 */
public final class GeoUtils {

    /**
     * Spatial Reference Identifier for WGS84 GPS Coordinates.
     */
    public static final int SRID_WGS84 = 4326;

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), SRID_WGS84);

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {
        // Prevent instantiation
    }

    /**
     * Creates a JTS Point from latitude and longitude.
     * Note: In JTS/PostGIS coordinates, X is Longitude and Y is Latitude (lon, lat).
     * 
     * @param latitude North/South coordinate [-90.0, 90.0]
     * @param longitude East/West coordinate [-180.0, 180.0]
     * @return JTS Point geometry with SRID 4326
     */
    public static Point createPoint(double latitude, double longitude) {
        validateCoordinates(latitude, longitude);
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    /**
     * Validates that latitude and longitude are within acceptable physical geographic bounds.
     */
    public static void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new BadRequestException("Invalid latitude: " + latitude + ". Latitude must be between -90.0 and 90.0");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new BadRequestException("Invalid longitude: " + longitude + ". Longitude must be between -180.0 and 180.0");
        }
    }

    /**
     * Calculates great-circle distance between two geographic coordinates using the Haversine formula.
     * 
     * @param lat1 Point 1 Latitude
     * @param lon1 Point 1 Longitude
     * @param lat2 Point 2 Latitude
     * @param lon2 Point 2 Longitude
     * @return Distance in kilometers (rounded to 2 decimal places)
     */
    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;
        return Math.round(distance * 100.0) / 100.0;
    }
}
