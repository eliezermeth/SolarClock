package util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DurationTimeFormatterTest
{
    Duration d1;
    Map<String, String> d1test;

    @BeforeEach
    void setup()
    {
        d1 = Duration
                .ofHours(3)
                .plusMinutes(15)
                .plusSeconds(2)
                .plusNanos(123456789);

        // set up test strings and proper results
        d1test = new HashMap<>();
        d1test.put("HH:mm:ss.1", "03:15:02.1");
        d1test.put("m:ss", "195:02");
        d1test.put("::s", "3:15:02");
        d1test.put(":::", "3:15:02");
        d1test.put("mmmm:.", "0195:02.123456789");
        d1test.put(":H::ss.3+", "3:15:02.123456789");
        d1test.put("test", null); // throws error; possibly should be more specific
        d1test.put("HHHH", "0003");
        d1test.put("sss.3", "11702.123");
        d1test.put(".11", null);
        d1test.put("H:dd", null);
        d1test.put("::::", null);
        d1test.put("mm:ss.1-", "195:02.1");
        d1test.put(":ss.1+-", "195:02.123456789");
    }

    @Test
    void format()
    {
        d1test.forEach((format, result) -> {
            runTest(d1, format, result);
        });
    }

    private void runTest(Duration d, String format, String result)
    {
        if (result == null)
            assertThrows(IllegalArgumentException.class, () -> DurationTimeFormatter.format(d, format));
        else
            assertEquals(result, DurationTimeFormatter.format(d, format));
    }
}