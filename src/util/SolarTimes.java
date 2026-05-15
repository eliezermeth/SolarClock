package util;

import com.kosherjava.zmanim.AstronomicalCalendar;
import com.kosherjava.zmanim.ComplexZmanimCalendar;
import events.ClockEvent;
import events.ClockEventManager;
import main.ClockBrain;
import util.enums.Zman;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;

/**
 * Calculates and provides the times for sunrise, sunset, and all twilights.
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

    // Solar times that may be commonly called for calculations; others called via methods
    private ZonedDateTime midnight,
            sunrise, midday, sunset,
            nextMidnight;

    /**
     * Preps a class that allows easy calculation of sunrise, sunset, and twilight times.  The date for calculations
     * can be changed using the {@code setDate} method, and must be used after creation to prime certain times.
     * @param cal the {@code ComplexZmanimCalendar} containing the location data and methods
     */
    public SolarTimes(ComplexZmanimCalendar cal)
    {
        this.cal = (ComplexZmanimCalendar) cal.clone();
        this.zone = cal.getCalendar().getTimeZone().toZoneId();
    }

    /**
     * Calculate the solar times for a specific day.  To force certainty regarding which day, this {@code ClockEvent}
     * is required to have a {@code Zman} type of {@code Zman.SUNRISE}, {@code Zman.MIDDAY}, or {@code Zman.SUNSET},
     * along with its time event being of the same time zone as the class's {@code AstronomicalCalendar}.
     * @param event {@code ClockEvent} with {@code Zman} of permitted type, containing a {@code ZonedDateTime} around
     *                                which the date for times will be set
     */
    public void setDate(ClockEvent event)
    {
        EnumSet<Zman> permitted = EnumSet.of(Zman.SUNRISE, Zman.MIDDAY, Zman.SUNSET);
        if (!permitted.contains(event.getZman()))
            throw new IllegalArgumentException("Expected sunrise, midday, or sunset event.");
        if (zone != event.getTime().getZone())
            throw new IllegalArgumentException("ZoneId of ClockEvent does not match calendar.");

        // valid type of ClockEvent, proceed with calculations
        cal.setCalendar(GregorianCalendar.from(event.getTime()));

        setCommonTimes();
    }

    private void setCommonTimes()
    {
        sunrise = dateToZonedDateTime(cal.getSunrise());
        midday = dateToZonedDateTime(cal.getChatzos());
        sunset = dateToZonedDateTime(cal.getSunset());

        // get midnights; make certain they are correct
        Date temp = cal.getSolarMidnight();
        if (temp.before(cal.getSunrise()))
        {
            midnight = dateToZonedDateTime(temp);
            ComplexZmanimCalendar czc = (ComplexZmanimCalendar) cal.clone(); // copy to prevent errors for later calculations
            czc.getCalendar().add(Calendar.DAY_OF_MONTH, 1);
            nextMidnight = dateToZonedDateTime(czc.getSolarMidnight());
        }
        else // the temp midnight was after sunrise; need to go back a day
        {
            nextMidnight = dateToZonedDateTime(temp);
            ComplexZmanimCalendar czc = (ComplexZmanimCalendar) cal.clone(); // copy to prevent errors for later calculations
            czc.getCalendar().add(Calendar.DAY_OF_MONTH, -1);
            midnight = dateToZonedDateTime(czc.getSolarMidnight());
        }
    }

    /**
     * Get the midnight time for the day.  As default, this returns astronomical midnight, when the sun is at its nadir.
     * @return {@code ZonedDateTime}
     */
    public ZonedDateTime getMidnight()
    {
        return midnight;
    }

    /**
     * Get the sunrise time for the day.
     * @return {@code ZonedDateTime}
     */
    public ZonedDateTime getSunrise()
    {
        return sunrise;
    }

    /**
     * Get the midday time for the day.  As default, this returns astronomical noon, when the sun is at its zenith.
     * @return {@code ZonedDateTime}
     */
    public ZonedDateTime getMidday()
    {
        return midday;
    }

    /**
     * Get the sunset time for the day.
     * @return {@code ZonedDateTime}
     */
    public ZonedDateTime getSunset()
    {
        return sunset;
    }

    /**
     * Get the midnight time for the next day.  As default, this returns astronomical midnight, when the sun is at its nadir.
     * @return {@code ZonedDateTime}
     */
    public ZonedDateTime getNextMidnight()
    {
        return nextMidnight;
    }

    /**
     * Returns the start/end time for a period of twilight.  If the period is before sunrise, it will provide the
     * starting time of the specific twilight period; if after sunset, the ending time for the specific twilight period.
     * @param period {@code DAWN} or {@code DUSK}
     * @param twilight the twilight period {@code CIVIL}, {@code NAUTICAL}, or {@code ASTRONOMICAL}
     * @return the start/end time for the period of twilight
     */
    public ZonedDateTime getTwilight (Period period, Twilight twilight)
    {
        return dateToZonedDateTime(period.get(cal, twilight));
    }

    /**
     * Convert a {@code Date} into a {@code ZonedDateTime} based on the zone of the saved calendar.
     * @param d starting {@code Date}
     * @return resultant {@code ZonedDateTime}
     */
    private ZonedDateTime dateToZonedDateTime(Date d)
    {
        return ZonedDateTime.ofInstant(d.toInstant(), zone);
    }

    public static void main(String[] args)
    {
        ClockBrain clock = ClockBrain.getInstance();
        ComplexZmanimCalendar czc = clock.getComplexZmanimCalendar();
        ClockEventManager events = clock.getEventManager();

        SolarTimes st = new SolarTimes(czc);
        st.setDate(events.getFirst(events.getUpcomingEvents(), Zman.SUNRISE));

        System.out.println("Midnight: " + st.getMidnight());
        System.out.println("Start Astronomical: " + st.getTwilight(Period.DAWN, Twilight.ASTRONOMICAL));
        System.out.println("Start Nautical: " + st.getTwilight(Period.DAWN, Twilight.NAUTICAL));
        System.out.println("Start Civil: " + st.getTwilight(Period.DAWN, Twilight.CIVIL));
        System.out.println("Sunrise: " + st.getSunrise());
        System.out.println("Midday: " + st.getMidday());
        System.out.println("Sunset: " + st.getSunset());
        System.out.println("End Civil: " + st.getTwilight(Period.DUSK, Twilight.CIVIL));
        System.out.println("End Nautical: " + st.getTwilight(Period.DUSK, Twilight.NAUTICAL));
        System.out.println("End Astronomical: " + st.getTwilight(Period.DUSK, Twilight.ASTRONOMICAL));
        System.out.println("Midnight: " + st.getNextMidnight());
    }
}
