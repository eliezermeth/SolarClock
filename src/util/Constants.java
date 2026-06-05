package util;

import java.time.Duration;

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
    public static final long MILLIS_PER_DAY = 86400000L;

    // Nanosecond timings
    /** The number of nanoseconds in a standard hour; {@code 3,600,000,000,000}. */
    public static final long HOUR_NANOS = Duration.ofHours(1).toNanos();
    /** The number of nanoseconds in a standard minute; {@code 60,000,000,000}. */
    public static final long MINUTE_NANOS = Duration.ofMinutes(1).toNanos();
    /** The number of nanoseconds in a standard second; {@code 1,000,000,000}. */
    public static final long SECOND_NANOS = Duration.ofSeconds(1).toNanos();

    public static final long HOURS_PER_TERMINATOR = 12;
}
