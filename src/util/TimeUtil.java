package util;

import com.kosherjava.zmanim.ZmanimCalendar;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil
{
    /**
     * Calculate the milliseconds between a start time and an end time.  If the period between spans midnight,
     * {@code end} will be moved to the next day and return the proper time between them.
     * <br>
     * Milliseconds deemed a small enough duration for accuracy.  Seconds provide a period too large, and the additional
     * accuracy afforded by microseconds is not considered significant.
     * @param start LocalTime for beginning of time period.
     * @param end LocalTime for end of time period.
     * @return {@code long} milliseconds between time periods.
     */
    public static long calculateMillisBetween(LocalTime start, LocalTime end)
    {
        long diff = ChronoUnit.MILLIS.between(start, end);

        if (diff < 0) // if end is before start, wrap result around day to get true value
            diff += Constants.MILLIS_PER_DAY;

        return diff;
    }

    /**
     * Transform a {@code Date} to a {@code LocalTime}.
     * @param d Date
     * @param c Calendar
     * @return LocalTime of Date at Calendar location
     */
    public static LocalTime dateToLocalTime(Date d, Calendar c)
    {
        return d.toInstant().atZone(c.getTimeZone().toZoneId()).toLocalTime();
    }

    /**
     * Return the midpoint between two {@code ZonedDateTime}s.
     * @param a starting {@code ZonedDateTime}
     * @param b ending {@code ZonedDateTime}
     * @return the {@code ZonedDateTime} between the two
     */
    public static ZonedDateTime midpoint(ZonedDateTime a, ZonedDateTime b)
    {
        return a.plus(Duration.between(a, b).dividedBy(2));
    }

    /**
     * Transform a {@code Date} to a {@code LocalTime}.
     * @param d Date
     * @param c ZmanimCalendar
     * @return LocalTime of Date at ZmanimCalendar's Calendar location
     */
    public static LocalTime dateToLocalTime(Date d, ZmanimCalendar c)
    {
        return dateToLocalTime(d, c.getCalendar());
    }

    /**
     * Transform a {@code Date} to a {@code LocalTime}.
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
