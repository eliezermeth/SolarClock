package util;

/**
 * Holds the data required to construct a GeoLocation.
 */
public class GeoData
{
    final String name;
    final double latitude;
    final double longitude;
    final String region;

    /**
     * Constructor.
     * @param name Name of the location.
     * @param latitude Latitude of location (N positive, S negative).
     * @param longitude Longitude of location (W negative, E positive).
     * @param region TimeZone region of location.
     */
    public GeoData(String name, double latitude, double longitude, String region)
    {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.region = region;
    }

    public String getName()
    {
        return name;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public String getRegion()
    {
        return region;
    }
}