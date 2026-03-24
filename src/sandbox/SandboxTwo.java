package sandbox;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import com.kosherjava.zmanim.util.GeoLocation;

import java.time.*;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

// to test ZmanimCalendar
public class SandboxTwo
{
    public String region = "America/New_York";

    private final int HOURS_PER_DAY = 12;
    private final int CHALAKIM_PER_HOUR = 1080;

    // shaah zman - temporal hour; daylight / 12

    public static void main(String[] args)
    {
        SandboxTwo s2 = new SandboxTwo();
        s2.run();
    }

    public void run()
    {
        ComplexZmanimCalendar czc = new ComplexZmanimCalendar(new GeoLocation("Pikesville, MD", 39.37427,
                -76.72247, TimeZone.getTimeZone(region)));

        LocalTime sunrise = dateToLocalTime(czc.getSunrise());
        System.out.println("Sunrise: " + sunrise);

        LocalTime sunset = dateToLocalTime(czc.getSunset());
        System.out.println("Sunset: " + sunset);

        Duration dayTime = Duration.between(sunrise, sunset);
        System.out.println("Day time: " + durationToString(dayTime));

        Duration hourTime = dayTime.dividedBy(HOURS_PER_DAY);
        System.out.println("Hour time: " + durationToString(hourTime));

        long shaahZmanis = czc.getShaahZmanisGra();
        System.out.println(shaahZmanis);
        System.out.printf("%02d:%02d:%02d.%d%n",
                TimeUnit.MILLISECONDS.toHours(shaahZmanis),
                TimeUnit.MILLISECONDS.toMinutes(shaahZmanis),
                TimeUnit.MILLISECONDS.toSeconds(shaahZmanis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(shaahZmanis)),
                shaahZmanis % 1000
        );

        Duration cheilekTime = hourTime.dividedBy(CHALAKIM_PER_HOUR);
        System.out.println("Cheilek time: " + durationToString(cheilekTime));

        /*
        System.out.println();

        // (3 * Math.PI / 2) = 270 degrees
        // (Math.PI / 2) = 90 degrees

        long totalSeconds = Math.abs(ChronoUnit.SECONDS.between(sunrise, sunset));
        long currentSeconds = Math.abs(ChronoUnit.SECONDS.between(sunrise, sunrise.plusHours(13)));
        System.out.println(totalSeconds);
        System.out.println(currentSeconds);
        double relativeAngle = Math.PI * (double) currentSeconds / totalSeconds;
        double adjustedAngle = relativeAngle + (3 * Math.PI / 2);
        System.out.println("\nRadians (angle): " + adjustedAngle);
        System.out.println("Degrees:" + Math.toDegrees(adjustedAngle));

        System.out.println(czc.getSunrise());
        Calendar instance = czc.getCalendar();
        instance.add(Calendar.DAY_OF_YEAR, 1); // add 1 day for tomorrow
        czc.setCalendar(instance);
        System.out.println(czc.getSunrise());
        // go back to yesterday
        instance.add(Calendar.DAY_OF_YEAR, -1);
        czc.setCalendar(instance);
        System.out.println(czc.getSunrise());
         */

    }

    public LocalTime dateToLocalTime(Date d)
    {
        return d.toInstant().atZone(ZoneId.of(region)).toLocalTime();
    }

    public String durationToString(Duration d)
    {
        long hours = d.toHours();
        long minutes = d.toMinutes() % 60;
        long seconds = d.getSeconds() % 60;
        long millis = d.toMillis() % 1000;

        return String.format("%02d:%02d:%02d.%d", hours, minutes, seconds, millis);
    }
}
