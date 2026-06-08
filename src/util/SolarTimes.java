package util;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import events.ClockEvent;
import events.ClockEventManager;
import main.ClockBrain;
import util.enums.Elevation;
import util.enums.MidpointMode;
import util.enums.Zman;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;

/**
 * Calculates and provides the times for sunrise, sunset, zenith/nadir, and all twilights.
 */
public class SolarTimes
{
    /**
     * The section of twilights: dawn (before sunrise) and dusk (after sunset).
     */
    public enum Period
    {
        /** The period of twilight before sunrise. */
        DAWN {
            @Override
            Date get(ComplexZmanimCalendar cal, Twilight twilight) { return twilight.getBegin(cal); }
        },
        /** The period of twilight after sunset. */
        DUSK {
            @Override
            Date get(ComplexZmanimCalendar cal, Twilight twilight) { return twilight.getEnd(cal); }
        };

        abstract Date get(ComplexZmanimCalendar cal, Twilight twilight);
    }

    /**
     * The three different types of twilight, when the sun is below the horizon: civil (0°-6°), nautical (6°-12°), and
     * astronomical (12°-18°).
     */
    public enum Twilight
    {
        /** When the sun is 0°-6° below the horizon. */
        CIVIL {
            Date getBegin(ComplexZmanimCalendar cal) { return cal.getBeginCivilTwilight(); }
            Date getEnd(ComplexZmanimCalendar cal) { return cal.getEndCivilTwilight(); }
        },
        /** When the sun is 6°-12° below the horizon. */
        NAUTICAL {
            Date getBegin(ComplexZmanimCalendar cal) { return cal.getBeginNauticalTwilight(); }
            Date getEnd(ComplexZmanimCalendar cal) { return cal.getEndNauticalTwilight(); }
        },
        /** When the sun is 12°-18° below the horizon. */
        ASTRONOMICAL {
            Date getBegin(ComplexZmanimCalendar cal) { return cal.getBeginAstronomicalTwilight(); }
            Date getEnd(ComplexZmanimCalendar cal) { return cal.getEndAstronomicalTwilight(); }
        };

        abstract Date getBegin(ComplexZmanimCalendar cal);
        abstract Date getEnd(ComplexZmanimCalendar cal);
    }

    private final ComplexZmanimCalendar cal;
    private final ZoneId zone;

    Method calcSunrise;
    Method calcSunset;

    // Solar times that may be commonly called for calculations; others called via methods
    private ZonedDateTime pastSunset, nadir,
            sunrise, zenith, sunset,
            nextNadir, nextSunrise;

    /**
     * Preps a class that allows easy calculation of sunrise, sunset, and twilight times.  The date for calculations
     * can be changed using the {@link SolarTimes#setDate} method, and must be used after creation to prime certain
     * times.
     * @param cal the {@link ComplexZmanimCalendar} containing the location data and methods
     */
    public SolarTimes(ComplexZmanimCalendar cal)
    {
        this.cal = (ComplexZmanimCalendar) cal.clone();
        this.zone = cal.getCalendar().getTimeZone().toZoneId();
    }

    /**
     * Calculate the solar times for a specific day.  The day will be calculated at the midnight midpoint; that is,
     * times at or after a true midnight until the next true midnight are considered one day.  Times will be calculated
     * for that day.
     *<p>
     * To force certainty regarding which day, the {@link ClockEvent} must be a {@link Zman} type of
     * {@link Zman#SUNRISE}, {@link Zman#MIDDAY}, {@link Zman#SUNSET}, or {@link Zman#SOLAR_MIDNIGHT}.  While it is easy
     * to determine which day the first three are, a {@link Zman#SOLAR_MIDNIGHT} will have its time bumped forward 12
     * hours to place it inside a daylight period, and calculations will be run after that point.  The time event must
     * also occur in the same time zone as the class's {@link ComplexZmanimCalendar}.
     * <p>
     * Calculations will use the settings determined by {@link Settings#MIDPOINT_MODE} and
     * {@link Settings#ELEVATION}.  The elevation will determine the sunrise/sunset, and may influence the midpoint
     * mode, if it is to be determined via the median time between those.  As of now, it will not affect the zenith.
     * @param event {@link ClockEvent} with {@link Zman} of permitted type, containing a {@link ZonedDateTime} around
     *                                which the date for times will be set
     *
     * @throws IllegalArgumentException if {@link ClockEvent} is not of correct type
     */
    public void setDate(ClockEvent event)
    {
        EnumSet<Zman> permitted = EnumSet.of(Zman.SUNRISE, Zman.MIDDAY, Zman.SUNSET, Zman.SOLAR_MIDNIGHT);
        if (!permitted.contains(event.getZman()))
            throw new IllegalArgumentException("Expected sunrise, midday, sunset, or solar midnight event.");
        if (zone != event.getTime().getZone())
            throw new IllegalArgumentException("ZoneId of ClockEvent does not match calendar.");

        ZonedDateTime time = event.getTime();
        if (event.getZman() == Zman.SOLAR_MIDNIGHT) // midnight, so start of next day
        {
            time = time.plusHours(12); // force to middle of next day for ease of calculations
        }

        // valid type of ClockEvent, proceed with calculations
        cal.setCalendar(GregorianCalendar.from(time));

        // reset method reflections each day (should this be changed?)
        try {
            if (Settings.ELEVATION == Elevation.ACTUAL)
            {
                calcSunrise = ComplexZmanimCalendar.class.getMethod("getSunrise");
                calcSunset = ComplexZmanimCalendar.class.getMethod("getSunset");
            }
            else // Elevation.SEA_LEVEL
            {
                calcSunrise = ComplexZmanimCalendar.class.getMethod("getSeaLevelSunrise");
                calcSunset = ComplexZmanimCalendar.class.getMethod("getSeaLevelSunset");
            }
        } catch (NoSuchMethodException e) { // never should happen, since methods are predetermined
            throw new RuntimeException("Unable to determine method for calculation.", e);
        }

        setCommonTimes();
    }

    /**
     * Set the solar times for events commonly called for calculations.  These are when the sun would be found at a
     * pure horizontal/vertical of the circle.
     */
    private void setCommonTimes()
    {
        // create duplicates to allow modifications and avoid errors for future calculations
        ComplexZmanimCalendar yesterday = (ComplexZmanimCalendar) cal.clone();
        yesterday.getCalendar().add(Calendar.DAY_OF_MONTH, -1);
        ComplexZmanimCalendar tomorrow = (ComplexZmanimCalendar) cal.clone();
        tomorrow.getCalendar().add(Calendar.DAY_OF_MONTH, 1);

        // get sunrises and sunsets
        try {
            pastSunset = dateToZonedDateTime((Date) calcSunset.invoke(yesterday));
            sunrise = dateToZonedDateTime((Date) calcSunrise.invoke(cal));
            sunset = dateToZonedDateTime((Date) calcSunset.invoke(cal));
            nextSunrise = dateToZonedDateTime((Date) calcSunrise.invoke(tomorrow));
        } catch (InvocationTargetException | IllegalAccessException e)
        {
            throw new RuntimeException("Failed to invoke sunrise/sunset method for day.", e);
        }


        zenith = dateToZonedDateTime(cal.getChatzos());

        // get astronomical midnights; make certain they are correct
        Date temp = cal.getSolarMidnight();
        if (temp.before(cal.getSunrise())) // does not require the precise time; can use default sunrise
        {
            nadir = dateToZonedDateTime(temp);
            nextNadir = dateToZonedDateTime(tomorrow.getSolarMidnight());
        }
        else // the temp midnight was after sunrise; need to go back a day
        {
            nextNadir = dateToZonedDateTime(temp);
            nadir = dateToZonedDateTime(yesterday.getSolarMidnight());
        }
    }

    /**
     * Get the sunset time for the previous day.
     * @return {@link ZonedDateTime}
     */
    public ZonedDateTime getPastSunset()
    {
        return pastSunset;
    }

    /**
     * Get the midnight time for the day.  As the default method, it will return the nadir or median, depending on
     * {@link Settings#MIDPOINT_MODE}.
     * @return {@link ZonedDateTime}
     */
    public ZonedDateTime getMidnight()
    {
        return getMidnight(Settings.MIDPOINT_MODE);
    }

    /**
     * Return a specific midnight time.
     * @param mode {@link MidpointMode#ASTRONOMICAL} (when the sun is at its nadir) or {@link MidpointMode#MEDIAN} (the
     *                                              midpoint between sunset and sunrise
     * @return midnight
     */
    public ZonedDateTime getMidnight(MidpointMode mode)
    {
        if (mode == MidpointMode.ASTRONOMICAL)
            return nadir;
        else // MEDIAN
            return TimeUtil.midpoint(pastSunset, sunrise);
    }

    /**
     * Get the sunrise time for the day.
     * @return {@link ZonedDateTime}
     */
    public ZonedDateTime getSunrise()
    {
        return sunrise;
    }

    /**
     * Get the midday time for the day.  As the default method, this returns zenith or median, depending on
     * {@link Settings#MIDPOINT_MODE}.
     * @return {@link ZonedDateTime}
     */
    public ZonedDateTime getMidday()
    {
        return getMidday(Settings.MIDPOINT_MODE);
    }

    /**
     * Return a specific midday time.
     * @param mode {@link MidpointMode#ASTRONOMICAL} (when the sun is at its zenith) or {@link MidpointMode#MEDIAN} (the
     *                                              midpoint between sunrise and sunset
     * @return midday
     */
    public ZonedDateTime getMidday(MidpointMode mode)
    {
        if (mode == MidpointMode.ASTRONOMICAL)
            return zenith;
        else // MEDIAN
            return TimeUtil.midpoint(sunrise, sunset);
    }

    /**
     * Get the sunset time for the day.
     * @return {@link ZonedDateTime}
     */
    public ZonedDateTime getSunset()
    {
        return sunset;
    }

    /**
     * Get the midnight time for the next day.  As the default method, it will return the nadir or median, depending on
     * {@link Settings#ANALOG_MIDPOINT_MODE}.
     * @return {@link ZonedDateTime}
     */
    public ZonedDateTime getNextMidnight()
    {
        return getNextMidnight(Settings.MIDPOINT_MODE);
    }

    /**
     * Return a specific midnight time for the next day.
     * @param mode {@link MidpointMode#ASTRONOMICAL} (when the sun is at its nadir) or {@link MidpointMode#MEDIAN} (the
     *                                              midpoint between sunset and sunrise
     * @return midnight
     */
    public ZonedDateTime getNextMidnight(MidpointMode mode)
    {
        if (mode == MidpointMode.ASTRONOMICAL)
            return nextNadir;
        else // MEDIAN
            return TimeUtil.midpoint(sunset, nextSunrise);
    }

    /**
     * Get the sunrise time for the next day.
     * @return {@link ZonedDateTime}
     */
    public ZonedDateTime getNextSunrise()
    {
        return nextSunrise;
    }

    /**
     * Returns the start/end time for a period of twilight.  If the period is before sunrise, it will provide the
     * starting time of the specific twilight period; if after sunset, the ending time for the specific twilight period.
     * @param period {@link Period#DAWN} or {@link Period#DUSK}
     * @param twilight the twilight period {@link Twilight#CIVIL}, {@link Twilight#NAUTICAL}, or
     *          {@link Twilight#ASTRONOMICAL}
     * @return the start/end time for the period of twilight
     */
    public ZonedDateTime getTwilight (Period period, Twilight twilight)
    {
        return dateToZonedDateTime(period.get(cal, twilight));
    }

    /**
     * Convert a {@link Date} into a {@link ZonedDateTime} based on the zone of the saved calendar.
     * @param d starting {@link Date}
     * @return resultant {@link ZonedDateTime}
     */
    private ZonedDateTime dateToZonedDateTime(Date d)
    {
        return ZonedDateTime.ofInstant(d.toInstant(), zone);
    }

    // -------------------------------------------------------------------------------------
    // Other methods
    // -------------------------------------------------------------------------------------

    /**
     * Returns the time span (milliseconds) of a tekufah between sunrise-sunset terminators.  This does not yet allow
     * tekufos to terminate at any other time.  This means that the zenith/nadir does not yet factor into the
     * calculation.
     * @param time the {@link ZonedDateTime} for the tekufah to be calculated
     * @return milliseconds of the calculated tekufah
     *
     * @throws IllegalArgumentException if {@code time} is outside the timespan permitted of past sunset to next sunrise
     */
    public long getTekufahSpan(ZonedDateTime time)
    {
        return Duration.between(getTekufahStart(time), getTekufahEnd(time)).toMillis();
    }

    /**
     * Returns the time span (milliseconds) of a sha'ah (one-twelfth, or hour) of a tekufah between sunrise-sunset
     * terminators.  This does not yet allow tekufos to terminate at any other time.  This means that zenith/nadir does
     * not yet factor into the calculation.
     * @param time the {@link ZonedDateTime} for the tekufah to be calculated
     * @return milliseconds of the calculated tekufah
     *
     * @throws IllegalArgumentException if {@code time} is outside the timespan permitted of past sunset to next sunrise
     */
    public long getTekufahShaah(ZonedDateTime time)
    {
        return getTekufahSpan(time) / Constant.HOURS_PER_TERMINATOR;
    }

    /**
     * Returns the {@link ZonedDateTime} of the start of the current tekufah, or period between sunrise/sunset
     * terminators.  This does not yet allow tekufos to terminate at any other time; zenith/nadir does not yet factor
     * into the calculation.
     * @param time the {@link ZonedDateTime} for the tekufah to be calculated
     * @return the starting time of the current tekufah
     *
     * @throws IllegalArgumentException if {@code time} is outside the timespan permitted of past sunset to next sunrise
     */
    public ZonedDateTime getTekufahStart(ZonedDateTime time)
    {
        if (time.isBefore(pastSunset)) // too early
            throw new IllegalArgumentException("Given time is too early.");
        else if (time.isBefore(getSunrise()))
            return pastSunset;
        else if (time.isBefore(getSunset()))
            return sunrise;
        else if (time.isBefore(nextSunrise) || time.isEqual(nextSunrise))
            return sunset;
        else // too late
            throw new IllegalArgumentException("Given time is too early.");
    }

    /**
     * Returns the {@link ZonedDateTime} of the end of the current tekufah, or period between sunrise/sunset
     * terminators.  This does not yet allow tekufos to terminate at any other time; zenith/nadir does not yet factor
     * into the calculation.
     * @param time the {@link ZonedDateTime} for the tekufah to be calculated
     * @return the starting time of the current tekufah
     *
     * @throws IllegalArgumentException if {@code time} is outside the timespan permitted of past sunset to next sunrise
     */
    public ZonedDateTime getTekufahEnd(ZonedDateTime time)
    {
        if (time.isBefore(pastSunset)) // too early
            throw new IllegalArgumentException("Given time is too early.");
        else if (time.isBefore(getSunrise()))
            return sunrise;
        else if (time.isBefore(getSunset()))
            return sunset;
        else if (time.isBefore(nextSunrise) || time.isEqual(nextSunrise))
            return nextSunrise;
        else // too late
            throw new IllegalArgumentException("Given time is too early.");
    }


    public static void main(String[] args)
    {
        ClockBrain clock = ClockBrain.getInstance();
        ComplexZmanimCalendar czc = clock.getComplexZmanimCalendar();
        ClockEventManager events = clock.getEventManager();

        SolarTimes st = new SolarTimes(czc);
        st.setDate(events.getFirst(events.getAllEvents(), Zman.SUNRISE));
        System.out.println(String.format("%20s", "Midnight: ") + st.getMidnight());
        System.out.println(String.format("%20s", "Start Astronomical: ") + st.getTwilight(Period.DAWN, Twilight.ASTRONOMICAL));
        System.out.println(String.format("%20s", "Start Nautical: ") + st.getTwilight(Period.DAWN, Twilight.NAUTICAL));
        System.out.println(String.format("%20s", "Start Civil: ") + st.getTwilight(Period.DAWN, Twilight.CIVIL));
        System.out.println(String.format("%20s", "Sunrise: ") + st.getSunrise());
        System.out.println(String.format("%20s", "Midday: ") + st.getMidday());
        System.out.println(String.format("%20s", "Sunset: ") + st.getSunset());
        System.out.println(String.format("%20s", "End Civil: ") + st.getTwilight(Period.DUSK, Twilight.CIVIL));
        System.out.println(String.format("%20s", "End Nautical: ") + st.getTwilight(Period.DUSK, Twilight.NAUTICAL));
        System.out.println(String.format("%20s", "End Astronomical: ") + st.getTwilight(Period.DUSK, Twilight.ASTRONOMICAL));
        System.out.println(String.format("%20s", "Midnight: ") + st.getNextMidnight());

        System.out.println();
        System.out.println(String.format("%20s", "Astronomical noon: ") + st.getMidday(MidpointMode.ASTRONOMICAL));
        System.out.println(String.format("%20s", "Median noon: ") + st.getMidday(MidpointMode.MEDIAN));

        System.out.println();
        System.out.println(String.format("%20s", "Now: ") + clock.getCurrentDateTime());
        System.out.println(String.format("%20s", "Midnight: ") + st.getMidnight());
    }
}
