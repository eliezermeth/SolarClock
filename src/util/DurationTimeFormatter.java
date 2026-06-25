package util;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DurationTimeFormatter
{
    private DurationTimeFormatter() {}

    // date field
    // y = year
    // M = month
    // d = day

    // time
    // H = hour
    // m = minute
    // s = second
    // S = fraction-of-second

    // modifier
    // ' escape literal text
    // '' literal singel quote
    // [ optional section start
    // ] optional section end
    // p pad next field
    private static char[] validChars = "dHms:.+1234567890".toCharArray();
    private char[] fullUnitOrder = new char[] { 'd', 'H', 'm', 's' };
    // number for fraction-of-second
    private char[] symbolOrder = new char[] { ':', '.' }; // does not include []

    public static String format(Duration duration,String pattern)
    {
        long totalSeconds = duration.getSeconds();

        StringBuffer result = new StringBuffer();

        // verify all are valid characters
        List<Character> characterList = new ArrayList<>();
        for (char c : pattern.toCharArray())
            characterList.add(c);
        for (char c : validChars)
            characterList.remove(c);
        if (!characterList.isEmpty())
            throw new IllegalArgumentException("Invalid characters in pattern.");

        // test number of colons
        int numColons = pattern.length() - pattern.replace(":", "").length();
        if (numColons > 3)
            throw new IllegalArgumentException("Too many colons in pattern format.");

        // determine fractions-of-second
        int decimalIndex = pattern.indexOf(".");
        int patternLength = pattern.length();
        if (decimalIndex != -1) // decmial exists
        {
            if (decimalIndex == patternLength - 1) // last character
            {
                // do nothing; valid
            }
            else if (decimalIndex == patternLength - 2) // second-to-last
            {
                // must be .n
                if (!Character.isDigit(pattern.charAt(patternLength - 1))) // if not n
                    throw new IllegalArgumentException("Invalid pattern format; see fractions-of-second.");
            }
            else if (decimalIndex == patternLength - 3) // third-to-last
            {
                // must be .n+
                if (!Character.isDigit(pattern.charAt(patternLength - 1)) || // if not n
                    pattern.charAt(patternLength - 1) != '+') // or last not +
                {
                    throw new IllegalArgumentException("Invalid pattern format; see fractions-of-second.");
                }
            }
            else
            {
                throw new IllegalArgumentException("Invalid pattern format; check decimal placement.")
            }
        }

        // determine if letters are valid and properly separated
        TimeUnit firstUnit = null;

        // determine letter, colon, decmimal separator, and fraction-of-second options
        char[] exploded = pattern.toCharArray();

        boolean optionalSection = false;
        for (int i = 0; i < exploded.length; i++)
        {
            if (exploded[i] == '[')
            {
                optionalSection = true;
                continue;
            }
            else if (exploded[i] == ']')
            {
                optionalSection = false;
                continue;
            }
        }
    }
}
