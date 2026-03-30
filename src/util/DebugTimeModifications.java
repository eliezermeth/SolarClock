package util;

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
     * Allows <code>HOURS</code>, <code>MINS</code>, and <code>SECS</code>.
     */
    public enum TIME_OFFSET
    {;
        public static final boolean enabled = false;
        public static final int HOURS = 12;
        public static final int MINS =  0;
        public static final int SECS =  0;
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
