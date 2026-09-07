package main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.debug.DebugOption;
import util.debug.DebugTimeModifications;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

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
    Object[] baseIncrement = new Object[] { false, Duration.ofHours(1) };

    DebugOption[] debugPointers = new DebugOption[] {
            DebugTimeModifications.ZdtOffset, DebugTimeModifications.TimeOffset,
            DebugTimeModifications.Speed, DebugTimeModifications.Increment };
    Object[] baseValues = new Object[] { baseZdt, baseTimeOffset, baseSpeed, baseIncrement};

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
        setUp();
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.Speed.setEnabled(true);
        int speed = ((Double) baseSpeed[1]).intValue();
        vc = new VirtualClock(zoneId);
        start = vc.now();
        Thread.sleep(sleepTime);
        sleepTime.multipliedBy(speed);
        assertZdtEquals(start.plus(sleepTime.multipliedBy(speed)), vc.now(), DECISECOND);
    }

    @Test
    void update()
    {
        ZonedDateTime currentTime = (ZonedDateTime) baseZdt[1];
        Duration increment = (Duration) baseIncrement[1];

        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.ZdtOffset.setEnabled(true);
        DebugTimeModifications.Increment.setEnabled(true);

        VirtualClock vc = new VirtualClock(zoneId, false);
        assertZdtEquals(currentTime, vc.now(), DECISECOND);

        vc.update(); // increment once
        currentTime = currentTime.plus(increment);
        assertZdtEquals(currentTime, vc.now(), DECISECOND);
        vc.update();
        vc.update();
        currentTime = currentTime.plus(increment).plus(increment);
        assertZdtEquals(currentTime, vc.now(), DECISECOND);

        // test when it should do nothing
        setUp();
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.ZdtOffset.setEnabled(true);
        currentTime = (ZonedDateTime) baseZdt[1];
        // increment = false (null)
        vc = new VirtualClock(zoneId, false);
        assertZdtEquals(currentTime, vc.now(), DECISECOND);
        vc.update();
        assertZdtEquals(currentTime, vc.now(), DECISECOND);
    }

    @Test
    void step()
    {
        // increase the time of a clock by a prescribed amount
        Duration t1 = Duration.ofMinutes(3),
                t2 = Duration.ofHours(1),
                t3 = Duration.ofSeconds(5);
        ZonedDateTime startTime = (ZonedDateTime) baseZdt[1];

        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.ZdtOffset.setEnabled(true);
        VirtualClock vc = new VirtualClock(zoneId, false);
        // paused so easier to test, and run with a specific ZdtOffset to begin with known value

        // test starting (current) time
        assertZdtEquals(startTime, vc.now(), DECISECOND);
        vc.step(t1);
        assertZdtEquals(startTime.plus(t1), vc.now(), DECISECOND);
        vc.step(t2);
        assertZdtEquals(startTime.plus(t1).plus(t2), vc.now(), DECISECOND);
        vc.step(t3);
        assertZdtEquals(startTime.plus(t1).plus(t2).plus(t3), vc.now(), DECISECOND);
    }

    @Test
    void getLocalTime()
    {
        VirtualClock vc = new VirtualClock(zoneId);
        assertTrue(Duration.between(LocalTime.now(zoneId),
                vc.getLocalTime()).abs().toNanos()
                <= DECISECOND.toNanos());

        setUp();
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.ZdtOffset.setEnabled(true);
        vc = new VirtualClock(zoneId);
        assertTrue(Duration.between(((ZonedDateTime) baseZdt[1]).toLocalTime(),
                vc.getLocalTime()).abs().toNanos()
                <= DECISECOND.toNanos());

        setUp();
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.TimeOffset.setEnabled(true);
        vc = new VirtualClock(zoneId);
        assertTrue(Duration.between(LocalTime.now().plus((Duration) baseTimeOffset[1]),
                vc.getLocalTime()).abs().toNanos()
                <= DECISECOND.toNanos());
    }

    @Test
    void getLocalDate()
    {
        VirtualClock vc = new VirtualClock(zoneId);
        vc.getLocalDate();
        assertEquals(LocalDate.now(zoneId), vc.getLocalDate());

        setUp();
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.ZdtOffset.setEnabled(true);
        vc = new VirtualClock(zoneId);
        assertEquals(((ZonedDateTime) baseZdt[1]).toLocalDate(), vc.getLocalDate());

        setUp();
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.TimeOffset.setEnabled(true);
        Duration customOffset = Duration.ofDays(3).plusMinutes(15);
        DebugTimeModifications.TimeOffset.set(customOffset);
        vc = new VirtualClock(zoneId);
        assertEquals(LocalDateTime.now(zoneId).plus(customOffset).toLocalDate(), vc.getLocalDate());
    }

    @Test
    void testGetAndSetSpeed()
    {
        // not debug, and changing speed
        VirtualClock vc = new VirtualClock(zoneId);
        assertEquals(1.0, vc.getSpeed());
        vc.setSpeed(2);
        assertEquals(2, vc.getSpeed());
        vc.pause();
        assertEquals(0, vc.getSpeed());
        vc.resume();
        assertEquals(2, vc.getSpeed());
        vc.setSpeed(-1);
        assertEquals(-1, vc.getSpeed());

        // check when clock is paused at start
        setUp();
        vc = new VirtualClock(zoneId, false);
        assertEquals(0, vc.getSpeed());
        vc.resume();
        assertEquals(1, vc.getSpeed());

        // check debug speed is set
        setUp();
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.Speed.setEnabled(true);
        vc = new VirtualClock(zoneId);
        assertEquals((Double) baseSpeed[1], vc.getSpeed());

        // check debug speed is set when clock is paused at start
        setUp();
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.Speed.setEnabled(true);
        vc = new VirtualClock(zoneId, false);
        assertEquals(0, vc.getSpeed());
        vc.resume();
        assertEquals((Double) baseSpeed[1], vc.getSpeed());
    }

    @Test
    void getSavedSpeed()
    {
        VirtualClock vc = new VirtualClock(zoneId);
        assertEquals(1, vc.getSavedSpeed());
        vc.setSpeed(2);
        assertEquals(2, vc.getSavedSpeed());
        vc.pause();
        assertEquals(2, vc.getSavedSpeed());
        vc.resume();
        assertEquals(2, vc.getSavedSpeed());

        vc = new VirtualClock(zoneId, false);
        assertEquals(1, vc.getSavedSpeed());
        vc.resume();
        assertEquals(1, vc.getSavedSpeed());

        setUp();
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.Speed.setEnabled(true);
        vc = new VirtualClock(zoneId);
        assertEquals((Double) baseSpeed[1], vc.getSavedSpeed());

        setUp();
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.Speed.setEnabled(true);
        vc = new VirtualClock(zoneId, false);
        assertEquals(2, vc.getSavedSpeed());
        vc.resume();
        assertEquals(2, vc.getSavedSpeed());
    }

    @Test
    void testGetAndSetIncrement()
    {
        // base
        VirtualClock vc = new VirtualClock(zoneId);
        assertNull(vc.getIncrement());
        Duration inc = Duration.ofHours(1);
        vc.setIncrement(inc);
        assertEquals(inc, vc.getIncrement());

        // debug = false but increment = true
        setUp();
        DebugTimeModifications.Increment.setEnabled(true);
        vc = new VirtualClock(zoneId);
        assertNull(vc.getIncrement());
        vc.setIncrement(inc);
        assertEquals(inc, vc.getIncrement());

        // debug = true; increment = true
        setUp();
        DebugTimeModifications.DEBUG = true;
        DebugTimeModifications.Increment.setEnabled(true);
        vc = new VirtualClock(zoneId);
        assertEquals(baseIncrement[1], vc.getIncrement());
    }

    @Test
    void testPauseAndResume() // pause(), isPaused(), resume()
    {
        VirtualClock vc = new VirtualClock(zoneId);
        assertFalse(vc.isPaused());
        vc.pause();
        assertTrue(vc.isPaused());
        vc.pause();
        assertTrue(vc.isPaused());
        vc.resume();
        assertFalse(vc.isPaused());

        vc = new VirtualClock(zoneId, true);
        assertFalse(vc.isPaused());

        vc = new VirtualClock(zoneId, false);
        assertTrue(vc.isPaused());
        vc.resume();
        assertFalse(vc.isPaused());
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