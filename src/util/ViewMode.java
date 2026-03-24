package util;

/**
 * Different view modes for analog clock.<br>
 * <b>ONLY SUNDIAL AND FULL_DAY CURRENTLY IMPLEMENTED.</b><br>
 * {@link #SUNDIAL} - Full circle; top half is day and bottom half is night.<br>
 * {@link #FULL_DAY} - Full circle; day and night get their true percentage of the 24-hour period.<br>
 * {@link #HALF_SUNDIAL} - Half circle; only shows the current day or night period.<br>
 * {@link #DIAL} - Full circle; similar to Standard Clock with a 24-hour range with the top at an arbitrary time.<br>
 * {@link #STANDARD_CLOCK} - Full circle; mimics a standard 12-hour clock.  Displays times viewable in current face.
 */
public enum ViewMode
{
    /**
     * Full circle; top half is day and bottom half is night.
     */
    SUNDIAL,
    /**
     * Full circle; day and night get their true percentage of the 24-hour period.
     */
    FULL_DAY,
    /**
     * Half circle; only shows the current day or night period.
     */
    HALF_SUNDIAL,
    /**
     * Full circle; similar to Standard Clock with a 24-hour range with the top at an arbitrary time.
     */
    DIAL,
    /**
     * Full circle; mimics a standard 12-hour clock.  Displays times viewable in current face.
     */
    STANDARD_CLOCK
}
