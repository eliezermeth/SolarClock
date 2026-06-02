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
     * @param start {@link LocalTime} for beginning of time period.
     * @param end {@link LocalTime} for end of time period.
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
     * Calculate the milliseconds between a start time and end time.  If the end is before the start, a negative result
     * will be returned.
     *
     * @param start starting {@link ZonedDateTime}
     * @param end ending {@link ZonedDateTime}
     * @return {@code long} of milliseconds between them
     */
    public static long calculateMillisBetween(ZonedDateTime start, ZonedDateTime end)
    {
        return ChronoUnit.MILLIS.between(start, end);
    }

    /**
     * Transform a {@link Date} to a {@link LocalTime}.
     * @param d {@link Date}
     * @param c {@link Calendar}
     * @return {@link LocalTime} of {@link Date} at {@link Calendar} location
     */
    public static LocalTime dateToLocalTime(Date d, Calendar c)
    {
        return d.toInstant().atZone(c.getTimeZone().toZoneId()).toLocalTime();
    }

    /**
     * Return the midpoint between two {@link ZonedDateTime}s.
     * @param a starting {@link ZonedDateTime}
     * @param b ending {@link ZonedDateTime}
     * @return the {@link ZonedDateTime} between the two
     */
    public static ZonedDateTime midpoint(ZonedDateTime a, ZonedDateTime b)
    {
        return a.plus(Duration.between(a, b).dividedBy(2));
    }

    /**
     * Transform a {@link Date} to a {@link LocalTime}.
     * @param d {@link Date}
     * @param c {@link ZmanimCalendar}
     * @return {@link LocalTime} of {@link Date} at {@link ZmanimCalendar}'s {@link Calendar} location
     */
    public static LocalTime dateToLocalTime(Date d, ZmanimCalendar c)
    {
        return dateToLocalTime(d, c.getCalendar());
    }

    /**
     * Transform a {@link Date} to a {@link LocalTime}.
     * <br>
     * <strong>WARNING:</strong> Returns based on a conversion using the operating system's default {@link TimeZone}
     * ({@code user.timezone}) if not set; if it is, that value will be as the default time zone.
     * @param d {@link Date}
     * @return {@link LocalTime}
     */
    public static LocalTime dateToLocalTime(Date d)
    {
        return dateToLocalTime(d, TimeZone.getDefault().toZoneId());
    }

    /**
     * Transform a {@link Date} to a {@link LocalTime} based on a {@link TimeZone} ID.
     * @param d {@link Date}
     * @param z {@link TimeZone}
     * @return {@link LocalTime} of {@link Date} within {@link TimeZone}
     */
    public static LocalTime dateToLocalTime(Date d, ZoneId z)
    {
        return d.toInstant().atZone(z).toLocalTime();
    }
}
