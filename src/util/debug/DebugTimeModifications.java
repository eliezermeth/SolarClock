package util.debug;

import java.time.Duration;
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
     * Allows the {@link main.VirtualClock} to be initialized to a specific {@link ZonedDateTime}.
     */
    public static DebugOption<ZonedDateTime> ZdtOffset =
            new DebugOption<>(false,
                    ZonedDateTime.of(
                            2026, 8, 20,
                            12, 0, 0, 0,
                            ZoneId.of("America/New_York")
                    ))
            {
                /**
                 * Retrieve the {@link ZonedDateTime} the {@link main.VirtualClock} should be offset to.  If the return is
                 * {@code null}, use {@link ZonedDateTime#now()} for the current time.
                 * @return {@code null} or {@link ZonedDateTime}
                 */
                @Override
                public ZonedDateTime get()
                {
                    return super.get();
                }

                /**
                 * Set the value of the debug option; {@code null} for {@link ZonedDateTime#now()}.
                 *
                 * @param value {@code null} or {@link ZonedDateTime}
                 */
                @Override
                public void set(ZonedDateTime value)
                {
                    super.set(value);
                }
            };

    /**
     * Allows the {@link main.VirtualClock} to be initialized a specified {@link Duration} off of
     * {@link ZonedDateTime#now()}.
     */
    public static DebugOption<Duration> TimeOffset =
            new DebugOption<>(false,
                    Duration.ofDays(0).plusHours(0).plusMinutes(0).plusSeconds(0))
            {
                /**
                 * Retrieve the {@link Duration} the {@link main.VirtualClock} should be offset from.
                 * {@link ZonedDateTime#now()}.  If the return is {@code null}, no offset should be applied.
                 * @return {@code null} or {@link Duration}
                 */
                @Override
                public Duration get()
                {
                    return super.get();
                }

                /**
                 * Set the value of the debug option; {@code null} for no offset.
                 *
                 * @param value {@code null} or {@link Duration}
                 */
                @Override
                public void set(Duration value)
                {
                    super.set(value);
                }
            };

    /**
     * Changes the running speed of the clock.  A value of {@code 1.0} is normal speed, while a clock speed of
     * {@code 2.0} means the clock will run twice as fast.
     */
    public static DebugOption<Double> Speed =
            new DebugOption<>(false,
                    10.0)
            {
                /**
                 * Retrieve the speed at which the clock should run, where {@code 1.0} is normal speed.
                 *
                 * @return clock speed
                 */
                @Override
                public Double get()
                {
                    return super.get();
                }

                /**
                 * Set the value of the clock speed.
                 *
                 * @param value clock speed
                 */
                @Override
                public void set(Double value)
                {
                    if (value == null) return; // not permitted
                    super.set(value);
                }
            };

    /**
     * Allows precise control of the time advanced each clock tick.
     */
    public static DebugOption<Duration> Increment =
            new DebugOption<>(false,
                    Duration.ofHours(1))
            {
                /**
                 * Get the {@link Duration} the clock should advance each clock tick.  A value of {@code null} should
                 * result in real-time updates (precise interval control is disabled).
                 *
                 * @return clock increment value or {@code null}
                 */
                @Override
                public Duration get()
                {
                    return super.get();
                }

                /**
                 * Set the {@link Duration} the clock should advance each clock tick.  A value of {@code null} will
                 * result in real-time updates (precise interval control is disabled).
                 *
                 * @param value {@link Duration} to advance
                 */
                @Override
                public void set(Duration value)
                {
                    super.set(value);
                }
            };
}
