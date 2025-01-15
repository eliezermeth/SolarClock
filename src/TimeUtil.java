import com.kosherjava.zmanim.ZmanimCalendar;

import java.sql.Time;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

    /**
     * Transform a <code>Date</code> to a <code>LocalTime</code>.
     * @param d Date
     * @param c Calendar
     * @return LocalTime of Date at Calendar location
     */
    public static LocalTime dateToLocalTime(Date d, Calendar c)
    {
        return d.toInstant().atZone(c.getTimeZone().toZoneId()).toLocalTime();
    }

    /**
     * Transform a <code>Date</code> to a <code>LocalTime</code>.
     * @param d Date
     * @param c ZmanimCalendar
     * @return LocalTime of Date at ZmanimCalendar's Calendar location
     */
    public static LocalTime dateToLocalTime(Date d, ZmanimCalendar c)
    {
        return dateToLocalTime(d, c.getCalendar());
    }

    /**
     * Transform a <code>Date</code> to a <code>LocalTime</code>.
     * <br>
     * <strong>WARNING:</strong> Returns based on a conversion using the operating system's default <code>TimeZone</code>
     * (<code>user.timezone</code>) if not set; if it is, that value will be as the default time zone.
     * @param d Date
     * @return LocalTime
     */
    public static LocalTime dateToLocalTime(Date d)
    {
        return dateToLocalTime(d, TimeZone.getDefault().toZoneId());
    }

    /**
     * Transform a <code>Date</code> to a <code>LocalTime</code> based on a TimeZone ID.
     * @param d Date
     * @param z ZoneID
     * @return LocalTime of Date within Zone
     */
    public static LocalTime dateToLocalTime(Date d, ZoneId z)
    {
        return d.toInstant().atZone(z).toLocalTime();
    }
}
