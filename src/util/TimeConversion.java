package util;

import main.ClockBrain;
import static util.Constants.*;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Provides calculations for converting between standard time and halachic time.
 */
public class TimeConversion
{
    private final ZonedDateTime start;
    private final ZonedDateTime end;
    private final int numHours;

    private final Duration duration;

    private final Duration halachicHourLength;
    private final Duration halachicCheilekLength;
    private final Duration halachicMinuteLength;
    private final Duration halachicSecondLength;

    private final Duration standardHourContainsLength;
    private final Duration standardMinuteContainsLength;
    private final Duration standardSecondContainsLength;

    /** The multiplying number when converting from a halachic unit of time into a standard unit. */
    private final double conversionRatioHalachicToStandard;
    /** The multiplying number when converting from a standard unit of time into a halachic unit. */
    private final double conversionRatioStandardToHalachic;

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

        duration = Duration.between(start, end);

        // calculate halachic lengths
        halachicHourLength = duration.dividedBy(numHours);
        halachicCheilekLength = halachicHourLength.dividedBy(Constants.CHALAKIM_PER_SHAAH);
        halachicMinuteLength = halachicHourLength.dividedBy(60); // 60 "minutes" per "hour"
        halachicSecondLength = halachicMinuteLength.dividedBy(60); // 60 "seconds" per "minute"

        // calculate the conversion ratios between the times
        // Assuming halachic hour = 75 standard minutes, then 1 standard hour = 60 / 75 = 0.8 halachic hours.
        // Thus, if the returned value is less than 1, the halachic version is longer than standard, and vice versa.
        conversionRatioHalachicToStandard = (double) duration.toNanos() / Duration.ofHours(numHours).toNanos();
        conversionRatioStandardToHalachic = (double) Duration.ofHours(numHours).toNanos() / duration.toNanos();

        // calculate the length of halachic times contained within standard units
        standardHourContainsLength =
                Duration.ofNanos((long) (HOUR_NANOS * conversionRatioStandardToHalachic));
        standardMinuteContainsLength =
                Duration.ofNanos((long) (MINUTE_NANOS * conversionRatioStandardToHalachic));
        standardSecondContainsLength =
                Duration.ofNanos((long) (SECOND_NANOS * conversionRatioStandardToHalachic));
    }

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

    // -------------------------------------------------------------------------------------
    // Getters for halachic times.  This returns, in standard units of time, how long the
    // halachic period would be during this period.  For example, if a halachic hour would
    // be composed of 75 minutes, it would return a duration of 1:15:00, which is the
    // standard-time length covered by a single halachic hour.
    // -------------------------------------------------------------------------------------

    /**
     * Returns the {@link Duration} of how long a halachic hour (sha'ah) would be during the period.  This is calculated
     * by taking the {@code duration} of this period and dividing it by the number of halachic hours composing this
     * period.
     * <p>
     * For example, if a halachic hour would be composed of 75 minutes, it would return a {@link Duration} of
     * 01:15:00.000, which is the standard-length time covered by a single halachic hour.
     *
     * @return {@link Duration} of sha'ah
     */
    public Duration getHalachicHourLength()
    {
        return halachicHourLength;
    }

    /**
     * Returns the {@link Duration} of a cheilek, or 1/1080th, of a halachic hour of this period.
     * <p>
     * For example, if a cheilek would be composed of 3.5 seconds, it would return a {@link Duration} of 00:00:03.500,
     * which is the standard-length time covered by a single cheilek.
     *
     * @return {@link Duration} of cheilek
     */
    public Duration getHalachicCheilekLength()
    {
        return halachicCheilekLength;
    }

    /**
     * Returns the {@link Duration} of a halachic "minute", or 1/60th of a halachic hour of this period.
     * <p>
     * For example, if a halachic minute would be composed of 65 seconds, it would return a {@link Duration} of
     * 00:01:05.000, which is the standard-length time covered by a single halachic minute.
     *
     * @return {@link Duration} of a halachic minute
     */
    public Duration getHalachicMinuteLength()
    {
        return halachicMinuteLength;
    }

    /**
     * Returns the {@link Duration} of a halachic "second", or 1/60th of a halachic minute of this period.
     * <p>
     * For example, if a halachic second would be composed of 1.333... seconds, it would return a {@link Duration} of
     * 00:00:01.333..., which is the standard-length time covered by a single halachic second.
     *
     * @return {@link Duration} of a halachic second
     */
    public Duration getHalachicSecondLength()
    {
        return halachicSecondLength;
    }

    // -------------------------------------------------------------------------------------
    // Getters for standard times.  This returns, in halachic units of time (given in the
    // Duration format of HH:MM:SS...), how long is covered over the given span during this
    // period.  For example, if the amount of halachic time elapsed during a single standard
    // hour is requested, it would return a duration of 00:50:00 if a halachic hour was 72
    // minutes.
    // -------------------------------------------------------------------------------------

    /**
     * Returns the {@link Duration}, in halachic units of time, of the length of a standard-time hour.
     * <p>
     * For example, if a halachic hour was 72 minutes, it would return a {@link Duration} of 00:50:00.000, which is the
     * halachic-time elapsed during a standard hour.
     *
     * @return halachic {@link Duration} of a standard hour
     */
    public Duration getStandardHourContainsLength()
    {
        return standardHourContainsLength;
    }

    /**
     * Returns the {@link Duration}, in halachic units of time, of the length of a standard-time minute.
     * <p>
     * For example, if a halachic minute was 72 seconds, it would return a {@link Duration} of 00:00:50.000, which is
     * the halachic-time elapsed during a standard minute.
     *
     * @return halachic {@link Duration} of a standard minute
     */
    public Duration getStandardMinuteContainsLength()
    {
        return standardMinuteContainsLength;
    }

    /**
     * Returns the {@link Duration}, in halachic units of time, of the length of a standard-time second.
     * <p>
     * For example, if a halachic second was 1.5 seconds, it would return a {@link Duration} of 00:00:00.666..., which
     * is the halachic-time elapsed during a standard second.
     *
     * @return halachic {@link Duration} of a standard second
     */
    public Duration getStandardSecondContainsLength()
    {
        return standardSecondContainsLength;
    }

    // -------------------------------------------------------------------------------------
    // Getters for conversion rations.
    // -------------------------------------------------------------------------------------

    /**
     * Returns the conversion factor from halachic units of time to standard units of time.  If the returned number is
     * greater than 1.0, then the halachic unit occupies a greater period of time than the standard-unit counterpart -
     * as an example, a single halachic hour may span seventy-five minutes, or one-and-a-quarter standard hours.
     * Conversely, if less than 1.0, the halachic span will be less than the standard-unit counterparts (such as a
     * halachic hour only taking 45 minutes).
     * <p>
     * In use, this number will be multiplied with a standard unit {@link Duration}'s nanos (or millis, etc.) to render
     * its halachic counterpart, which can then be cast back into {@link Duration} to calculate its time.
     *
     * @return {@code double} conversion factor from halachic units to standard
     */
    public double getConversionRationHalachicToStandard()
    {
        return conversionRatioHalachicToStandard;
    }

    /**
     * Returns the conversion factor from standard units of time to halachic units of time.  If the returned number is
     * greater than 1.0, then the standard unit occupies a greater period of time than the halachic-unit counterpart -
     * as an example, a single standard hour may span seventy-five halachic minutes, or one-and-a-quarter halachic
     * hours.  Conversely, if less than 1.0, the standard span will be less than the halachic-unit counterparts (such as
     * a standard hour only taking 45 halachic minutes).
     * <p>
     * In use, this number will be multiplied with a halachic unit {@link Duration}'s nanos (or millis, etc.) to render
     * its standard counterpart, which can then be cast back into {@link Duration} to calculate its time.
     *
     * @return {@code double} conversion factor from standard units to halachic
     */
    public double getConversionRatioStandardToHalachic()
    {
        return conversionRatioStandardToHalachic;
    }

    // -------------------------------------------------------------------------------------
    // Time converters.
    // -------------------------------------------------------------------------------------

    /**
     * Convert a {@link ZonedDateTime} during the current span into its halachic counterpart in the HH:MM:SS format.
     * @param time the standard {@link ZonedDateTime} time
     *
     * @return the halachic time, as a {@link Duration}
     *
     * @throws IllegalArgumentException if the {@code time} is before the start of the conversion period or
     *                                  after the end of the conversion period
     */
    public Duration toHalachicStandardTime(ZonedDateTime time)
    {
        if (time.isBefore(start) || time.isAfter(end))
            throw new IllegalArgumentException("Time must be within the current conversion span.");

        return Duration.ofNanos((long) (Duration.between(start, time).toNanos() * conversionRatioStandardToHalachic));
    }

    /**
     * Convert a {@link ZonedDateTime} during the current span into its halachic counterpart in the hours-and-chalakim
     * format.  It is returned as an {code int[]} where the first element is the hours and the second the chalakim.
     * @param time the standard {@link ZonedDateTime} time
     *
     * @return the halachic time, in an array {@code int[hours, chalakim]}
     *
     * @throws IllegalArgumentException if the {@code time} is before the start of the conversion period or
     *                                  after the end of the conversion period
     */
    public int[] toHalachicCheilekTime(ZonedDateTime time)
    {
        if (time.isBefore(start) || time.isAfter(end))
            throw new IllegalArgumentException("Time must be within the current conversion span.");

        int[] hoursChalakim = new int[2];

        long elapsed = Duration.between(start, time).toNanos();
        hoursChalakim[0] = (int) (elapsed / halachicHourLength.toNanos());
        hoursChalakim[1] = (int) (elapsed % halachicHourLength.toNanos() / halachicCheilekLength.toNanos());

        return hoursChalakim;
    }

    /**
     * Converts a {@link Duration} of standard time into its halachic-time counterpart based on the conversion factor of
     * the current time period.  As this uses a static conversion factor with no further connection to zmanim, it may
     * return a time that extends beyond the current tekufah.  Caution should be taken when using this method.
     * @param time standard {@link Duration} to be converted to halachic time
     *
     * @return halachic {@link Duration}
     *
     * @throws IllegalArgumentException if the time is negative
     */
    public Duration toHalachicTime(Duration time)
    {
        if (time.isNegative())
            throw new IllegalArgumentException("Time must be positive.");

        return Duration.ofNanos((long) (time.toNanos() * conversionRatioStandardToHalachic));
    }

    /**
     * Converts a {@link Duration} of halachic time into its standard-time counterpart based on the conversion factor of
     * the current time period.  As this uses a static conversion factor with no further connection to zmanim, it may
     * return a time that extends beyond the current tekufah.  It will also accept times longer than the current tekfuah
     * without error, and convert them using the ratio for the present tekfuah to calculate standard time, despite it
     * resulting in an improper time (as some portion should rather be calculated with a different ratio, for whatever
     * span it occurs in).  Caution should be taken when using this method.
     * @param time halachic {@link Duration} to be converted to standard time
     *
     * @return standard {@link Duration}
     *
     * @throws IllegalArgumentException if the time is negative
     */
    public Duration toStandardTime(Duration time)
    {
        if (time.isNegative())
            throw new IllegalArgumentException("Time must be positive.");

        return Duration.ofNanos((long) (time.toNanos() * conversionRatioHalachicToStandard));
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
        System.out.println();

        printRow("Halachic hour", durationToString(tc.getHalachicHourLength()));
        printRow("Cheilek length", durationToString(tc.getHalachicCheilekLength()));
        printRow("Halachic min", durationToString(tc.getHalachicMinuteLength()));
        printRow("Halachic sec", durationToString(tc.getHalachicSecondLength()));
        System.out.println();

        printRow("Standard hour", durationToString(tc.getStandardHourContainsLength()));
        printRow("Standard min", durationToString(tc.getStandardMinuteContainsLength()));
        printRow("Standard sec", durationToString(tc.getStandardSecondContainsLength()));
        System.out.println();

        Duration elapsedDuration = Duration.between(tc.getStart(), now);
        printRow("Standard elapsed", durationToString(elapsedDuration));
        printRow("Halachic stand", durationToString(tc.toHalachicStandardTime(clock.getCurrentDateTime())));
        int[] hoursChalakim = tc.toHalachicCheilekTime(clock.getCurrentDateTime());
        printRow("Halachic cheil", hoursChalakim[0] + " " + hoursChalakim[1] + "/1080");
        System.out.println();

        printRow("Ratio hal->stand", tc.getConversionRationHalachicToStandard());
        printRow("Ratio stand->hal", tc.getConversionRatioStandardToHalachic());
        System.out.println();

        printRow("1h halach to stand", durationToString(tc.toStandardTime(Duration.ofHours(1))));
        printRow("1h stand to halach", durationToString(tc.toHalachicTime(Duration.ofHours(1))));
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
