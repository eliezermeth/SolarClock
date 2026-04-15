package sandbox;

import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import main.ClockBrain;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    }

    public static void main(String[] args)
    {
        SandboxOne s1 = new SandboxOne();
        s1.hebrewDate();
    }
}
