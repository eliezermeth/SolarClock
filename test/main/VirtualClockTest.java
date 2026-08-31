package main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.debug.DebugOption;
import util.debug.DebugTimeModifications;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualClockTest
{
    // constants
    final Duration SECOND = Duration.ofSeconds(1);
    final Duration DECISECOND = Duration.ofMillis(100);
    final Duration CENTISECOND = Duration.ofMillis(10);
    final Duration MILLISECOND = Duration.ofMillis(1);

    // test code
    ZoneId zoneId = ZoneId.of("America/New_York");

    // set base values for tests
    boolean baseDebugState = false;
    Object[] baseZdt = new Object[] { false,
            ZonedDateTime.of(
            2026, 8, 20,
            12, 0, 0, 0,
            zoneId)};
    Object[] baseTimeOffset = new Object[] { false, Duration.ofHours(1).plusSeconds(1) };
    Object[] baseSpeed = new Object[] { false, 2.0 };
    Object[] baseInterval = new Object[] { false, Duration.ofHours(1) };

    DebugOption[] debugPointers = new DebugOption[] {
            DebugTimeModifications.ZdtOffset, DebugTimeModifications.TimeOffset,
            DebugTimeModifications.Speed, DebugTimeModifications.Increment };
    Object[] baseValues = new Object[] { baseZdt, baseTimeOffset, baseSpeed, baseInterval };

    // before each test, reset DebugTimeModifications to known values
    @BeforeEach
    void setUp()
    {
        DebugTimeModifications.DEBUG = baseDebugState;

        for (int i = 0; i < debugPointers.length; i++)
        {
            debugPointers[i].setEnabled((Boolean) ((Object[]) baseValues[i])[0]);
            debugPointers[i].set(((Object[]) baseValues[i])[1]);
        }
    }

    @Test
    void constructor() throws InterruptedException
    {
        // test different constructors

        // basic / run = true
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.ZdtOffset.setEnabled(true);
        VirtualClock vc = new VirtualClock(zoneId, true);
        ZonedDateTime t1 = vc.now();
        Thread.sleep(10); // any delay
        ZonedDateTime t2 = vc.now();
        assertNotEquals(t1, t2);

        // run = false
        setUp();
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.ZdtOffset.setEnabled(true);
        vc = new VirtualClock(zoneId, false);
        t1 = vc.now();
        Thread.sleep(10); // any delay
        t2 = vc.now();
        assertEquals(t1, t2);
    }

    @Test
    void now() throws InterruptedException
    {
        // test with clock in base state, no debug
        VirtualClock vc = new VirtualClock(zoneId);
        // allow 1/10th second difference due to computer run time
        assertZdtEquals(ZonedDateTime.now(zoneId), vc.now(), DECISECOND);

        // ZdtOffset enabled, TimeOffset disabled
        setUp(); // reset
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.ZdtOffset.setEnabled(true);
        vc = new VirtualClock(zoneId);
        assertZdtEquals((ZonedDateTime) baseZdt[1], vc.now(), DECISECOND);

        // TimeOffset enabled, ZdtOffset disabled
        setUp();
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.TimeOffset.setEnabled(true);
        vc = new VirtualClock(zoneId);
        assertZdtEquals(ZonedDateTime.now(zoneId).plus((Duration) baseTimeOffset[1]), vc.now(), DECISECOND);

        // ZdtOffset and TimeOffset enabled; only ZdtOffset should be applied
        setUp();
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.ZdtOffset.setEnabled(true);
        DebugTimeModifications.TimeOffset.setEnabled(true);
        vc = new VirtualClock(zoneId);
        assertZdtEquals((ZonedDateTime) baseZdt[1], vc.now(), DECISECOND);

        // check with sleep and all debugs disabled
        setUp();
        Duration sleepTime = Duration.ofSeconds(2);
        vc = new VirtualClock(zoneId);
        ZonedDateTime start = vc.now();
        Thread.sleep(sleepTime);
        assertZdtEquals(start.plus(sleepTime), vc.now(), DECISECOND);

        // check time speed
    }

    @Test
    void update()
    {
    }

    @Test
    void step()
    {
    }

    @Test
    void getLocalTime()
    {
    }

    @Test
    void getLocalDate()
    {
    }

    @Test
    void setSpeed()
    {
    }

    @Test
    void getSpeed()
    {
    }

    @Test
    void getSavedSpeed()
    {
    }

    @Test
    void setIncrement()
    {
    }

    @Test
    void getIncrement()
    {
    }

    @Test
    void isPaused()
    {
    }

    @Test
    void pause()
    {
    }

    @Test
    void resume()
    {
    }

    @Test
    void setTime()
    {
    }

    /**
     * Test that two {@link ZonedDateTime}s contain the same time.  The {@code delta} is the acceptable difference
     * between the two times, given due to computer work time between measurements.  Times are not ordered - it is the
     * absolute value of the duration between the times, not direction-based.
     *
     * @param expected expected {@link ZonedDateTime}
     * @param actual actual measured {@link ZonedDateTime}
     * @param delta permissible difference between the two {@link ZonedDateTime}s
     */
    void assertZdtEquals(ZonedDateTime expected, ZonedDateTime actual, Duration delta)
    {
        assertTrue(Duration.between(expected, actual).abs().toNanos() <= delta.toNanos());
    }
}