package util;

import java.time.Duration;

/**
 * Debugging options to be used when testing items; no other use.
 */
public class DebugTimeModifications
{
    /**
     * Controls this entire class; if {@code false}, unlikely for other elements to work
     */
    public static final boolean DEBUG = false;

    /**
     * If time should be offset from current time.
     * Check {@code enabled} to see if offset time is enabled.
     * Stored in {@code duration}, which contains days, hours, minutes, and seconds.
     * Allows changes to days, hours, minutes, and seconds.
     */
    public enum OFFSET
    {;
        public static final boolean enabled = true;
        /** [ DAYS, HOURS, MINUTES, SECONDS ] */
        private static final int[] offset = new int[] {186, 0, 0, 0};
        public static final Duration duration = Duration.ofDays(offset[0]).plusHours(offset[1])
                .plusMinutes(offset[2]).plusSeconds(offset[3]);
    }

    /**
     * Whether time should be accelerated.  If so, it will operate at the set speed, where {@code 1.0} is normal.
     */
    public enum SPEED
    {;
      public static final boolean enabled = false;
      public static final double speed = 10.0;
    }
}
