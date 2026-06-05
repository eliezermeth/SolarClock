package util;

/**
 * Constants that may be used by the rest of the program.
 */
public class Constants
{
    // Halachic
    public static final int SHAOS_PER_TEKUFAH = 12;
    public static final int CHALAKIM_PER_SHAAH = 1080;

    // Millisecond timings
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long MILLIS_PER_DAY = 86_400_000L;

    // Nanosecond timings
    /** The number of nanoseconds in a standard hour; {@code 3,600,000,000,000}. */
    public static final long HOUR_NANOS = 3_600_000_000_000L;
    /** The number of nanoseconds in a standard minute; {@code 60,000,000,000}. */
    public static final long MINUTE_NANOS = 60_000_000_000L;
    /** The number of nanoseconds in a standard second; {@code 1,000,000,000}. */
    public static final long SECOND_NANOS = 1_000_000_000L;

    public static final long HOURS_PER_TERMINATOR = 12;
}
