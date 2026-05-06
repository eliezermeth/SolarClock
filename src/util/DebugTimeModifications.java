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
    public static final boolean DEBUG = true;

    /**
     * If time should be offset from current time.
     * Check {@code enabled} to see if offset time is enabled.
     * Stored in {@code Duration}, which contains days, hours, minutes, and seconds.
     * Allows {@code HOURS}, {@code MINS}, and {@code SECS}.
     */
    public enum OFFSET
    {;
        public static final boolean enabled = true;
        private static final int DAYS = 0;
        private static final int HOURS = 1;
        private static final int MINS =  0;
        private static final int SECS =  0;
        public static final Duration duration = Duration.ofDays(DAYS).plusHours(HOURS).plusMinutes(MINS).plusSeconds(SECS);
    }

    /**
     * Whether time should be accelerated.  If so, it will operate at the set speed, where {@code 1.0} is normal.
     */
    public enum SPEED
    {;
      public static final boolean enabled = false;
      public static final double speed = 3.0;
    }
}
