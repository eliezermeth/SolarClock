package util;

import java.time.Duration;

/**
 * Debugging options to be used when testing items; no other use.
 */
public class DebugTimeModifications
{
    /**
     * Controls this entire class; if <code>false</code>, unlikely for other elements to work
     */
    public static final boolean DEBUG = false;

    /**
     * If time should be offset from current time.
     * Check <code>enabled</code> to see if offset time is enabled.
     * Stored in <code>Duration</code>, which contains days, hours, minutes, and seconds.
     * Allows <code>HOURS</code>, <code>MINS</code>, and <code>SECS</code>.
     */
    public enum TIME_OFFSET
    {;
        public static final boolean enabled = false;
        private static final int DAYS = 0;
        private static final int HOURS = 12;
        private static final int MINS =  0;
        private static final int SECS =  0;
        public static final Duration duration = Duration.ofDays(DAYS).plusHours(HOURS).plusMinutes(MINS).plusSeconds(SECS);
    }

    /**
     * Whether time should be accelerated.  If so, it will do so by <code>SECONDS</code> per iteration.
     */
    public enum TIME_ACCELERATION
    {;
      public static final boolean enabled = false;
      public static final int SECONDS = 1;
    }
}
