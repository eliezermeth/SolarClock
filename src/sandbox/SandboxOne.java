package sandbox;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SandboxOne
{
    public static void main(String[] args)
    {
        SandboxOne s1 = new SandboxOne();
        s1.ownCode();
    }

    public void ownCode()
    {
        final int HOURS_PER_DAY = 12;
        final int CHALAKIM_PER_HOUR = 1080;

        String stringSunrise = "6:46:07";
        String stringSunset = "4:54:31";

        // Formatter to handle both "H:mm:ss" and "HH:mm:ss"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[H][HH]:mm:ss");

        LocalTime sunrise = LocalTime.parse(stringSunrise, formatter);
        LocalTime sunset = LocalTime.parse(stringSunset, formatter);
        sunset = sunset.plusHours(12); // swap to PM
        System.out.println("Sunrise time: " + sunrise);
        System.out.println("Sunset time: " + sunset);

        System.out.println();

        Duration dayTime = Duration.between(sunrise, sunset);
        System.out.println("Day time: " + durationToString(dayTime));

        Duration hourTime = dayTime.dividedBy(HOURS_PER_DAY);
        System.out.println("Hour time: " + durationToString(hourTime));

        Duration cheilekTime = hourTime.dividedBy(CHALAKIM_PER_HOUR);
        System.out.println("Cheilek time: " + durationToString(cheilekTime));

        LocalTime add = sunrise;
        for (int i = 0; i < 6; i++)
            add = add.plus(hourTime);
        System.out.println("Chatzos (standard): " + add);
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
