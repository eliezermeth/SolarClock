import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class TimeUtil
{
    /**
     * Calculate the milliseconds between a start time and an end time.  If the period between spans midnight, <code>end
     * </code> will be moved to the next day and return the proper time between them.
     * <br>
     * Milliseconds deemed a small enough duration for accuracy.  Seconds provide a period too large, and the additional
     * accuracy afforded by microseconds is not considered significant.
     * @param start LocalTime for beginning of time period.
     * @param end LocalTime for end of time period.
     * @return <code>long</code> milliseconds between time periods.
     */
    public static long calculateMillisBetween(LocalTime start, LocalTime end)
    {
        long timeAccumulated = 0L;

        if (end.isBefore(start)) // overlaps midnight
        {
            timeAccumulated += ChronoUnit.MILLIS.between(start, LocalTime.MAX) + 1000; // add millis between MAX and midnight
            start = LocalTime.MIDNIGHT;
        }

        return timeAccumulated + ChronoUnit.MILLIS.between(start, end);
    }

}
