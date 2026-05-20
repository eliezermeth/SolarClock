package util.enums;

/**
 * Defines how the midpoint of a day/night should be determined.  {@code ASTRONOMICAL} uses the zenith/nadir as the
 * midpoint, while {@code MEDIAN} uses the time equally between sunrise and sunset.
 */
public enum MidpointMode
{
    ASTRONOMICAL,
    MEDIAN
}
