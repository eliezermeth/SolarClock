package sandbox;

import com.kosherjava.zmanim.AstronomicalCalendar;
import com.kosherjava.zmanim.ComplexZmanimCalendar;
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import main.ClockBrain;

import java.lang.reflect.Method;
import java.time.*;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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

    public static void main(String[] args)
    {
        SandboxOne s1 = new SandboxOne();
        //s1.hebrewDate();
        s1.multipleTwilights();
    }
}
