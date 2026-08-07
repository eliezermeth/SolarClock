package util.enums;

/**
 * Different view modes for analog clock.<br>
 * <b>ONLY SUNDIAL AND FULL_DAY CURRENTLY IMPLEMENTED.</b><br>
 * {@link #SUNDIAL} - Full circle; top half is day and bottom half is night.<br>
 * {@link #PROPORTIONAL} - Full circle; day and night get their true percentage of the 24-hour period.<br>
 * {@link #HALF_SUNDIAL} - Half circle; only shows the current day or night period.<br>
 * {@link #DIAL} - Full circle; similar to Standard Clock with a 24-hour range with the top at an arbitrary time.<br>
 * {@link #STANDARD_CLOCK} - Full circle; mimics a standard 12-hour clock.  Displays times viewable in current face.
 */
public enum ViewMode
{
    /**
     * Full circle; top half is day and bottom half is night.
     */
    SUNDIAL(true),
    /**
     * Full circle; day and night get their true percentage of the 24-hour period.
     */
    PROPORTIONAL(false),
    /**
     * Half circle; only shows the current day or night period.
     */
    HALF_SUNDIAL(true),
    /**
     * Full circle; similar to Standard Clock with a 24-hour range with the top at an arbitrary time.
     */
    DIAL(false),
    /**
     * Full circle; mimics a standard 12-hour clock.  Displays times viewable in current face.
     */
    STANDARD_CLOCK(false);

    public final boolean equalDayNightView;

    /**
     * Constructor; never to be called except by enum.
     * @param equalDayNightView if clock should be split 50/50 day/night
     */
    private ViewMode(boolean equalDayNightView)
    {
        this.equalDayNightView = equalDayNightView;
    }
}
