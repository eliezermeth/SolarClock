package sandbox;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import gui.Moon;
import main.ClockBrain;
import org.shredzone.commons.suncalc.MoonPosition;
import org.shredzone.commons.suncalc.MoonTimes;
import util.GeoData;
import util.Regions;

import java.time.*;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class SandboxOne
{
    public void hebrewDate()
    {
        ClockBrain cb = ClockBrain.getInstance();

        JewishCalendar jc = new JewishCalendar();

        // set to specific date if needed
        //jc.setDate(gregorianCalendar);

        // to manually advance the jewish date
        //if (currentTime.after(czc.getSunset()))
        //    jc.forward(Calendar.Date, 1);

        HebrewDateFormatter hdf = new HebrewDateFormatter();
        hdf.setHebrewFormat(true); // output in hebrew

        System.out.println("Full date:\n" + hdf.format(jc));
        System.out.println();

        System.out.println("Day and month:\n" +
                hdf.formatHebrewNumber(jc.getJewishDayOfMonth()) + " " + hdf.formatMonth(jc));
        System.out.println();

        Date now = new Date();
        System.out.println("Now:\n" + now);
        System.out.println("Sunset:\n" + cb.getComplexZmanimCalendar().getSunset());


        if (now.after(cb.getComplexZmanimCalendar().getSunset()))
        {
            System.out.println("ליל");
        }
        // NOTE: Does not, as of yet, know it is the next day when calling after sunset.  Will need to manually advance
        // day to print properly.
    }

    public void multipleTwilights()
    {
        ClockBrain clock = ClockBrain.getInstance();
        ComplexZmanimCalendar czc = clock.getComplexZmanimCalendar();

        System.out.println("Start Astronomical: " + czc.getBeginAstronomicalTwilight());
        System.out.println("Start Nautical: " + czc.getBeginNauticalTwilight());
        System.out.println("Start Civil: " + czc.getBeginCivilTwilight());
        System.out.println("Sunrise: " + czc.getSunrise());
        System.out.println("Sunset: " + czc.getSunset());
        System.out.println("End Civil: " + czc.getEndCivilTwilight());
        System.out.println("End Nautical: " + czc.getEndNauticalTwilight());
        System.out.println("End Astronomical: " + czc.getEndAstronomicalTwilight());
    }

    public void getMoonInfo()
    {
        ClockBrain clock = ClockBrain.getInstance();
        ZonedDateTime now = clock.getCurrentDateTime();
        Date d = Date.from(now.toInstant());

        GeoData location = Regions.getLocation("Pikesville");
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        MoonPosition position = MoonPosition.compute().on(d).at(latitude, longitude).execute();
        System.out.println("Altitude: " + position.getAltitude()); // elevation above/below the horizon (degrees)
        System.out.println("ParallacticAngle: " + position.getParallacticAngle()); // tilt of moon
        System.out.println();
        // note: use rise-set times for dial, as altitude does not go to 90

        List<MoonEvent> events = getLunarTimes(now, latitude, longitude);

        MoonEvent previous = null, upcoming = null, after = null;

        // find what event is after
        for (int i = 0; i < events.size(); i++)
        {
            if (events.get(i).time().isAfter(now))
            {
                previous = events.get(i - 1);
                upcoming = events.get(i);
                after = events.get(i + 1);
                break; // don't search further
            }
        }

        System.out.println(previous);
        System.out.println("Current moon status: " + (previous.rise() ? "up" : "down"));
        System.out.println(upcoming);
        System.out.println(after);
        System.out.println();
    }

    private List<MoonEvent> getLunarTimes(ZonedDateTime now, double latitude, double longitude)
    {
        List<MoonTimes> timesBySolarDay = new ArrayList<>();
        List<MoonEvent> events = new ArrayList<>();

        int dayOffset = 0;

        // add current day before entering loop
        timesBySolarDay.add(MoonTimes.compute().on(Date.from(now.toInstant())).at(latitude, longitude).execute());

        // enter loop; require at least 3 lunar events, where second event is before (or) now, and
        // second-to-last is after (or) now
        do {
            dayOffset++; // increase tested day by 1

            // add lunar event times on a solar day
            timesBySolarDay.add(MoonTimes.compute().on(Date.from(now.minusDays(dayOffset).toInstant()))
                    .at(latitude, longitude).execute());
            timesBySolarDay.add(MoonTimes.compute().on(Date.from(now.plusDays(dayOffset).toInstant()))
                    .at(latitude, longitude).execute());

            // add to events list
            for (MoonTimes mt : timesBySolarDay)
            {
                if (mt.getRise() != null)
                    events.add(new MoonEvent(mt.getRise(), true));
                if (mt.getSet() != null)
                    events.add(new MoonEvent(mt.getSet(), false));
            }
            timesBySolarDay.clear(); // remove days in case required to loop again
            events.sort(Comparator.comparing(MoonEvent::time)); // sort chronologically

            // require second to be before now, and second-to-last after
        } while (events.size() < 3 && !events.get(1).time().isBefore(now) &&
                !events.get(events.size() - 2).time().isAfter(now));

        return events;
    }

    public static void main(String[] args)
    {
        SandboxOne s1 = new SandboxOne();
        //s1.hebrewDate();
        //s1.multipleTwilights();
        s1.getMoonInfo();
    }
}

record MoonEvent(ZonedDateTime time, boolean rise) { }