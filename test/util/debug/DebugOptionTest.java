package util.debug;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DebugOptionTest
{
    final double startDouble = 2.0;
    final Duration startDuration = Duration.ofHours(1).plusMinutes(2);
    final ZonedDateTime startZdt = ZonedDateTime.of(
            2026, 8, 20,
            12, 0, 0, 0,
            ZoneId.of("America/New_York"));

    DebugOption<Double> doDouble;
    DebugOption<Duration> doDuration;
    DebugOption<ZonedDateTime> doZdt;


    // create standard slate before each test
    @BeforeEach
    void setUp()
    {
        doDouble = new DebugOption<>(true, startDouble)
        { 
            @Override
            public void set(Double value)
            {
                if (value == null) throw new IllegalArgumentException("Speed may not be null.");
                super.set(value);
            }
        };
        doDuration = new DebugOption<>(false, startDuration) { };
        doZdt = new DebugOption<>(true, startZdt) { };
    }

    @Test
    void isEnabled()
    {
        assertTrue(doDouble.isEnabled());
        assertFalse(doDuration.isEnabled());
        assertTrue(doZdt.isEnabled());
    }

    @Test
    void setEnabled()
    {
        // copy of isEnabled() test
        assertTrue(doDouble.isEnabled());
        assertFalse(doDuration.isEnabled());
        assertTrue(doZdt.isEnabled());

        // change values
        doDouble.setEnabled(false);
        doDuration.setEnabled(true);
        doZdt.setEnabled(true); // keep the same; test that doesn't always flip value

        // test
        assertFalse(doDouble.isEnabled());
        assertTrue(doDuration.isEnabled());
        assertTrue(doZdt.isEnabled());
    }

    @Test
    void get()
    {
        assertEquals(startDouble, doDouble.get());
        assertEquals(startDuration, doDuration.get());
        assertEquals(startZdt, doZdt.get());
    }

    @Test
    void set()
    {
        // copy of get() test
        assertEquals(startDouble, doDouble.get());
        assertEquals(startDuration, doDuration.get());
        assertEquals(startZdt, doZdt.get());
        
        // prep test values
        double testDouble = -12.0;
        Duration testDuration = Duration.ofNanos(1);
        ZonedDateTime testZdt = ZonedDateTime.of(
                1970, 1, 1,
                0, 0, 0, 0,
                ZoneId.of("America/New_York"));
        
        // set test values
        doDouble.set(testDouble);
        doDuration.set(testDuration);
        doZdt.set(testZdt);

        // test
        assertEquals(testDouble, doDouble.get());
        assertEquals(testDuration, doDuration.get());
        assertEquals(testZdt, doZdt.get());
        
        // set values to null
        assertThrows(IllegalArgumentException.class, () -> doDouble.set(null));
        assertDoesNotThrow(() -> doDuration.set(null));
        assertDoesNotThrow(() -> doZdt.set(null));

        // test
        assertEquals(testDouble, doDouble.get());
        assertNull(doDuration.get());
        assertNull(doZdt.get());
    }
}