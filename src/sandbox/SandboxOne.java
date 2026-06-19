package sandbox;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import main.ClockBrain;
import org.shredzone.commons.suncalc.MoonIllumination;

import java.time.*;
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

    public void getMoonInfo()
    {
        ClockBrain clock = ClockBrain.getInstance();
        Date d = Date.from(clock.getCurrentDateTime().toInstant());

        // for the moon illumination
        MoonIllumination illum = MoonIllumination.compute().on(d).execute();
        // fraction of moon that illuminated
        double fraction = illum.getFraction(); // how much of the disk is lit - 0.0 new moon; 1.0 full moon
        // position in lunar cycle
        double phase = illum.getPhase(); // -180 new moon waxing; 0 full moon, 180 new moon waning

        System.out.println("Fraction: " + fraction);
        System.out.println("Phase: " + phase);

    }

    public static void main(String[] args)
    {
        SandboxOne s1 = new SandboxOne();
        //s1.hebrewDate();
        //s1.multipleTwilights();
        s1.getMoonInfo();
    }
}
