package util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Debugging options to be used when testing items; no other use.
 */
public class DebugTimeModifications
{
    /**
     * Controls this entire class; if {@code false}, unlikely for other elements to work
     */
    public static final boolean DEBUG = true;

    /**
     * Initialize the {@link main.VirtualClock} to a specific {@link ZonedDateTime}.
     * Test {@code enabled} to see if offset is enabled.
     * Value stored in {@link ZDT_OFFSET#offset}.
     */
    public enum ZDT_OFFSET
    {;
        public static final boolean enabled = false;
        // if null, start from ZonedDateTime.now()
        // if value, VirtualClock should start at exactly that time
        ZonedDateTime offset = LocalDateTime
                .parse("2026-08-20T09:00:00").atZone(ZoneId.of("America/New_York"));
    }

    /**
     * If time should be offset from current time.
     * Check {@code enabled} to see if offset time is enabled.
     * Stored in {@link Duration} {@code duration}, which contains days, hours, minutes, and seconds.
     * Allows changes to days, hours, minutes, and seconds.
     */
    public enum TIME_OFFSET
    {;
        public static final boolean enabled = false;
        /** [ DAYS, HOURS, MINUTES, SECONDS ] */
        private static final int[] offset = new int[] {1, 0, 0, 0};
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
