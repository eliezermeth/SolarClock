package util.geo;

/**
 * Holds the data required to construct a GeoLocation.
 */
public class NewGeoData
{
    private String name;
    private double latitude;
    private double longitude;
    private Double elevation;
    private boolean elevationLocked;
    private String timezone;

    /**
     * Constructor.
     * @param name Name of the location.
     * @param latitude Latitude of location (N positive, S negative).
     * @param longitude Longitude of location (W negative, E positive).
     * @param elevation Elevation of location.
     * @param elevationLocked If the elevation is locked and should not be recalculated.
     * @param timezone String of timezone of location.
     */
    public NewGeoData(String name, double latitude, double longitude,
                      Double elevation, boolean elevationLocked, String timezone)
    {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.elevationLocked = elevationLocked;
        this.timezone = timezone;
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

    public Double getElevation()
    {
        return elevation;
    }

    public boolean isElevationLocked()
    {
        return elevationLocked;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public void setElevation(Double elevation)
    {
        this.elevation = elevation;
    }

    public void setElevationLocked(boolean elevationLocked)
    {
        this.elevationLocked = elevationLocked;
    }
}
