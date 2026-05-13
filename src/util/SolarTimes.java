package util;

import com.kosherjava.zmanim.AstronomicalCalendar;
import com.kosherjava.zmanim.ComplexZmanimCalendar;
import events.ClockEvent;
import events.ClockEventManager;
import main.ClockBrain;
import util.enums.Zman;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
            Date get(AstronomicalCalendar cal, Twilight twilight) { return twilight.getBegin(cal); }
        },
        /** The period of twilight after sunset. */
        DUSK {
            @Override
            Date get(AstronomicalCalendar cal, Twilight twilight) { return twilight.getEnd(cal); }
        };

        abstract Date get(AstronomicalCalendar cal, Twilight twilight);
    }


    /**
     * The three different types of twilight, when the sun is below the horizon: civil (0°-6°), nautical (6°-12°), and
     * astronomical (12°-18°).
     */
    public enum Twilight
    {
        /** When the sun is 0°-6° below the horizon. */
        CIVIL {
            Date getBegin(AstronomicalCalendar cal) { return cal.getBeginCivilTwilight(); }
            Date getEnd(AstronomicalCalendar cal) { return cal.getEndCivilTwilight(); }
        },
        /** When the sun is 6°-12° below the horizon. */
        NAUTICAL {
            Date getBegin(AstronomicalCalendar cal) { return cal.getBeginNauticalTwilight(); }
            Date getEnd(AstronomicalCalendar cal) { return cal.getEndNauticalTwilight(); }
        },
        /** When the sun is 12°-18° below the horizon. */
        ASTRONOMICAL {
            Date getBegin(AstronomicalCalendar cal) { return cal.getBeginAstronomicalTwilight(); }
            Date getEnd(AstronomicalCalendar cal) { return cal.getEndAstronomicalTwilight(); }
        };

        abstract Date getBegin(AstronomicalCalendar cal);
        abstract Date getEnd(AstronomicalCalendar cal);
    }


    private final AstronomicalCalendar cal;
    private final ZoneId zone;

    /**
     * Preps a class that allows easy calculation of sunrise, sunset, and twilight times.  The date for calculations
     * can be changed using the {@code setDate} method, and should be used immediately after creation.
     * @param cal the {@code AstronomicalCalendar} containing the location data and methods
     */
    public SolarTimes(AstronomicalCalendar cal)
    {
        this.cal = (AstronomicalCalendar) cal.clone();
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
    }

    /**
     * Get the sunrise time for the day.
     * @return {@code ZonedDateTime}
     */
    public ZonedDateTime getSunrise()
    {
        return dateToZonedDateTime(cal.getSunrise());
    }

    /**
     * Get the sunset time for the day.
     * @return {@code ZonedDateTime}
     */
    public ZonedDateTime getSunset()
    {
        return dateToZonedDateTime(cal.getSunset());
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

        System.out.println("Start Astronomical: " + st.getTwilight(Period.DAWN, Twilight.ASTRONOMICAL));
        System.out.println("Start Nautical: " + st.getTwilight(Period.DAWN, Twilight.NAUTICAL));
        System.out.println("Start Civil: " + st.getTwilight(Period.DAWN, Twilight.CIVIL));
        System.out.println("Sunrise: " + st.getSunrise());
        System.out.println("Sunset: " + st.getSunset());
        System.out.println("End Civil: " + st.getTwilight(Period.DUSK, Twilight.CIVIL));
        System.out.println("End Nautical: " + st.getTwilight(Period.DUSK, Twilight.NAUTICAL));
        System.out.println("End Astronomical: " + st.getTwilight(Period.DUSK, Twilight.ASTRONOMICAL));
    }
}
