package util;

import main.ClockBrain;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Provides calculations for converting between standard time and halachic time.
 */
public class TimeConversion
{
    private ZonedDateTime start;
    private ZonedDateTime end;
    private int numHours;

    private Duration duration;

    private Duration halachicHourLength;
    private Duration cheilekLength;
    private Duration halachicMinuteLength;
    private Duration halachicSecondLength;

    /**
     * Creates the conversion class.
     * @param start The starting time for the period on which the calculations will be applied.
     * @param end The starting time for the period on which the calculations will be applied.
     * @param numHours How many hours the period should be divided into.
     *
     * @throws IllegalArgumentException if {@code end} is not after {@code start}
     */
    public TimeConversion(ZonedDateTime start, ZonedDateTime end, int numHours)
    {
        if (end.isEqual(start) || end.isBefore(start))
            throw new IllegalArgumentException("A positive non-zero difference in time must exist between the first " +
                    "time and the second.");

        this.start = start;
        this.end = end;
        this.numHours = numHours;

        runConversions();
    }

    private void runConversions()
    {
        duration = Duration.between(start, end);

        // calculate halachic lengths
        halachicHourLength = duration.dividedBy(numHours);
        cheilekLength = halachicHourLength.dividedBy(Constants.CHALAKIM_PER_SHAAH);
        halachicMinuteLength = halachicHourLength.dividedBy(60); // 60 "minutes" per "hour"
        halachicSecondLength = halachicMinuteLength.dividedBy(60); // 60 "seconds" per "minute"
    }

    // current length of time period (done)

    // sha'ah length (done)
    // length of a cheilek (done)

    // 1 standard hour = x halachic time
    // 1 halachic hour = x standard time (done)

    // 1 standard minute = x halachic time
    // 1 halachic minute = x standard time (done)

    // 1 cheilek = x standard time (done)

    // 1 standard second = x halachic time
    // 1 halachic second = x standard time (done)

    // -------------------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------------------

    /**
     * Returns the starting time of the period.
     * @return starting {@link ZonedDateTime}
     */
    public ZonedDateTime getStart()
    {
        return start;
    }

    /**
     * Returns the ending time of the period.
     * @return ending {@link ZonedDateTime}
     */
    public ZonedDateTime getEnd()
    {
        return end;
    }

    /**
     * Returns the number of variable-length hours the period is split into.
     * @return {@code int} of "hours" in the period
     */
    public int getNumHours()
    {
        return numHours;
    }

    /**
     * Returns the {@link Duration} of time between the start and end of this period.
     * @return {@link Duration}
     */
    public Duration getDuration()
    {
        return duration;
    }

    /**
     * Returns the {@link Duration} of how long a halachic hour (sha'ah) would be during the period.  This is calculated by
     * taking the {@code duration} of this period and dividing it by the number of halachic hours composing this period.
     * @return {@link Duration} of sha'ah
     */
    public Duration getHalachicHourLength()
    {
        return halachicHourLength;
    }

    /**
     * Returns the {@link Duration} of a cheilek, or 1/1080th, of a halachic hour of this period.
     * @return {@link Duration} of cheilek
     */
    public Duration getCheilekLength()
    {
        return cheilekLength;
    }

    /**
     * Returns the {@link Duration} of a halachic "minute", or 1/60th of a halachic hour of this period.
     * @return {@link Duration} of a halachic minute
     */
    public Duration getHalachicMinuteLength()
    {
        return halachicMinuteLength;
    }

    /**
     * Returns the {@link Duration} of a halachic "second", or 1/60th of a halachic minute of this period.
     * @return {@link Duration} of a halachic second
     */
    public Duration getHalachicSecondLength()
    {
        return halachicSecondLength;
    }

    // -------------------------------------------------------------------------------------

    public static void main(String[] args)
    {
        ClockBrain clock = ClockBrain.getInstance();
        SolarTimes st = clock.getSolarTimes();
        ZonedDateTime now = clock.getCurrentDateTime();

        TimeConversion tc = new TimeConversion(st.getTekufahStart(now), st.getTekufahEnd(now), 12); // sunrise-sunset

        printRow("Start", tc.getStart());
        printRow("End", tc.getEnd());
        printRow("Num hours", tc.getNumHours());
        printRow("Duration", durationToString(tc.getDuration()));
        printRow("Halachic hour", durationToString(tc.getHalachicHourLength()));
        printRow("Cheilek length", durationToString(tc.getCheilekLength()));
        printRow("Hahachic min", durationToString(tc.getHalachicMinuteLength()));
        printRow("Halachic sec", durationToString(tc.getHalachicSecondLength()));
        System.out.println();
        Duration elapsedDuration = Duration.between(tc.getStart(), now);
        printRow("Standard elapsed", durationToString(elapsedDuration));

        long hourLength = tc.getHalachicHourLength().toNanos();
        long minLength = tc.getHalachicMinuteLength().toNanos();
        long secLength = tc.getHalachicSecondLength().toNanos();

        long rem = elapsedDuration.toNanos();
        long hours = rem / hourLength;
        rem = rem % hourLength;
        long chalakim = rem / tc.getCheilekLength().toNanos();
        long mins = rem / minLength;
        rem = rem % minLength;
        long secs = rem / secLength;
        rem = rem % secLength;

        printRow("Halachic stand", hours + ":" + mins + ":" + secs + "." + rem);
        printRow("Halachic cheil", hours + " " + chalakim + "/1080");
    }

    private static void printRow(String text, Object value)
    {
        int leading = 20;
        System.out.printf("%" + leading + "s: %s%n", text, value);
    }

    private static String durationToString(Duration d)
    {
        return String.format("%02d:%02d:%02d.%d", d.toHoursPart(), d.toMinutesPart(), d.toSecondsPart(), d.toNanosPart());
    }
}
