package util;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // .matches() = whether entire input matches the pattern
    // .find() = finds next occurrence of pattern; get by printing materh.group()
    // .group() = returns the text that matched

    private static final char[] SECTIONS = {'d', 'H', 'm', 's'}; // in order
    private static final TimeUnit[] UNITS = {TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS};

    private static final Pattern CHARS =
            Pattern.compile("^[dHms:.+\\-0-9]+$"); // ensure no unexpected characters
    private static final Pattern SPLIT =
            Pattern.compile("^([^.]*)(?:\\.(.*))?$"); // split time portion from (optional) fractional portion
    private static final Pattern FRACTION =
            Pattern.compile("^0?[1-9]?\\+?-?$"); // if decimal point exists, validate what follows it
    private static final Pattern TOKEN =
            Pattern.compile("(d+|H+|m+|s+|:)"); // extract labeled fields
    private static final Pattern ORDER =
            Pattern.compile("^d*:?H*:?m*:?s*$"); // require them to be in proper order

    public static String format(Duration duration,String pattern)
    {
        long totalSeconds = duration.getSeconds();
        // for first item, can use TimeUnit.DAYS, etc.

        StringBuffer result = new StringBuffer();

        Context context = new Context(duration, pattern);

        validateAndParse(context);

        return null;
    }

    private static void validateAndParse(Context context)
    {
        // only valid characters
        if (!testOnlyPermittedChars(context.format))
            throw new IllegalArgumentException("Invalid characters present in formatting String " + context.format);

        // split groups of formatting string
        Matcher split = SPLIT.matcher(context.format);
        if (split.matches())
        {
            context.gTime = split.group(1);
            context.gFraction = split.group(2);
        }

        // validate general order of time segments
        if (!testGeneralTimeOrder(context.gTime))
            throw new IllegalArgumentException("Invalid time order");

        // split time format based on semicolons
        context.aTimeParts = context.gTime.split(":", -1); // as many groups as possible

        // determine starting time unit; may throw error
        findStartingUnit(context);

        // ensure 3+ characters can only be in first slot
        for (int i = 1; i < context.aTimeParts.length; i++)
            if (context.aTimeParts[i].length() > 2)
                System.out.println("Cannot have more than two characters in slot " + context.aTimeParts[i]);

        // test fractional section
        if (!testFractionalSection(context.gFraction))
            throw new IllegalArgumentException("Fractional section invalid.");

        // create time portion

        // create fractional portion
        if (context.gFraction != null); // TODO
    }

    /**
     * Verify all characters present in string are permitted.
     * @param patten
     * @return
     */
    private static boolean testOnlyPermittedChars(String patten)
    {
        return CHARS.matcher(patten).matches();
    }

    /**
     * Test that the general order of the time segment is valid.
     * @param time
     * @return
     */
    private static boolean testGeneralTimeOrder(String time)
    {
        return ORDER.matcher(time).matches();
    }

    private static boolean allSameChar(String s)
    {
        char c = s.charAt(0);
        for (int i = 1; i < s.length(); i++)
            if (s.charAt(i) != c)
                return false;
        return true;
    }

    private static int findStartingUnit(Context context)
    {
        int lowestUnit = findLowestUnit(context.aTimeParts, (context.gFraction != null));

        // ensure proper incremental order of time segments
        int starting = -1;

        // work backward on the time array
        for (int i = 1; i < context.aTimeParts.length + 1; i++)
        {
            String part = context.aTimeParts[context.aTimeParts.length - i];
            char expectedChar = SECTIONS[lowestUnit - i + 1]; // add 1 to offset the starting 1


            if (!part.isEmpty()) // text exists in segment
            {
                char first = part.charAt(0);
                // must match expected character
                if (first != expectedChar)
                    throw new IllegalArgumentException("Time segment " + part + " is incorrect.");
                // must all be the same character
                if (!allSameChar(part))
                    throw new IllegalArgumentException("Time segments may only be composed of a single type of unit");
            }

            starting = lowestUnit - i + 1;
        }

        context.startingUnitIndex = starting;
        return starting;
    }

    /**
     * Find the lowest unit in the time unit section, be it days, hours, minutes, or seconds.  This is returned via the
     * index of the unit in the {@code SECTIONS} array.  Does not complete correctly if the parts section is formatted
     * incorrectly.
     * @param aTimeParts
     * @param fractionalExists
     * @return
     */
    private static int findLowestUnit(String[] aTimeParts, boolean fractionalExists)
    {
        if (fractionalExists) // fractional exists
        {
            return SECTIONS.length - 1; // 's'
        }

        // else; fractional does not exist
        for (int i = 1; i < aTimeParts.length + 1; i++) // traverse backward
        {
            String part = aTimeParts[aTimeParts.length - i];

            if (!part.isEmpty()) // section is not empty
            {
                char firstChar = part.charAt(0);

                int sectionsIndex = -1; // index in SECTIONS with corresponding letter
                // find its position in SECTIONS by traversing forward
                for (int j = 0; j < SECTIONS.length && sectionsIndex == -1; j++)
                    if (firstChar == SECTIONS[j])
                        sectionsIndex = j;

                // determine how far to go back in units to the beginning of parts
                // sectionsIndex + i - 1 to get lowest unit; if beyond index; fail
                int potentialIndex = sectionsIndex + i - 1; // subtract 1 from i to avoid counting itself
                if (potentialIndex >= SECTIONS.length)
                    throw new IllegalArgumentException("Time section " + part + " is in an invalid location.");

                return potentialIndex; // lowest unit
            }
        }

        // no populated section found
        if (aTimeParts.length == SECTIONS.length)
            return SECTIONS.length - 1; // 's'
        else // less than max extent
            throw new IllegalArgumentException("Not enough context in time section.");
    }

    private static String createUnitTimePortion(Context context)
    {
        StringBuilder sb = new StringBuilder();
        int minFieldWidth = context.aTimeParts[0].length();

        for (int i = 0; i < context.aTimeParts.length; i++)
        {
            // determine the resultant number for the section
            long drawnNumber;
            if (sb.isEmpty()) // no text exists yet
                drawnNumber = to(context.time, UNITS[context.startingUnitIndex + i]);
            else
                drawnNumber = toPart(context.time, UNITS[context.startingUnitIndex + i]);

            if (minFieldWidth == 0) // attempt to change minimum field width if non-existent
                minFieldWidth = context.aTimeParts[i].length();

            if (minFieldWidth == 0) // section not required
            {
                if (drawnNumber > 0)
                {
                    sb.append(drawnNumber).append(":");
                    minFieldWidth = 2;
                }
                // else, do nothing
            }
            else // field required
            {
                sb.append(String.format("%0" + minFieldWidth + "d", drawnNumber)).append(":");
                minFieldWidth = 2;
            }
        }

        // remove trailing ":"
        if (!sb.isEmpty() && sb.charAt(sb.length() - 1) == ':')
            sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /**
     * Returns the number of {@link TimeUnit} within the {@link Duration}.
     * @param d
     * @param unit
     * @return
     */
    private static long to(Duration d, TimeUnit unit)
    {
        return switch (unit)
        {
            case DAYS           -> d.toDays();
            case HOURS          -> d.toHours();
            case MINUTES        -> d.toMinutes();
            case SECONDS        -> d.toSeconds();
            case MILLISECONDS   -> d.toMillis(); // should never be called
            case MICROSECONDS   -> d.toNanos() / 1000; // should never be called
            case NANOSECONDS    -> d.toNanos();
        };
    }

    /**
     * Returns the number of {@link TimeUnit} within the {@link Duration}, discounting any that complete a higher
     * {@link TimeUnit}.
     * @param d
     * @param unit
     * @return
     */
    private static long toPart(Duration d, TimeUnit unit)
    {
        return switch (unit)
        {
            case DAYS           -> d.toDaysPart();
            case HOURS          -> d.toHoursPart();
            case MINUTES        -> d.toMinutesPart();
            case SECONDS        -> d.toSecondsPart();
            case MILLISECONDS   -> d.toMillisPart(); // should never be called
            case MICROSECONDS   -> d.toNanosPart() / 1000; // should never be called
            case NANOSECONDS    -> d.toNanosPart();
        };
    }

    private static boolean testFractionalSection(String gFraction)
    {
        if (gFraction != null)
            return FRACTION.matcher(gFraction).matches();
        return true; // if there is no fraction
    }

    private static String createFractionalPortion(Context context)
    {
        long totalNanos = context.time.toNanosPart(); // get total number of fractional units; max size

        boolean requireDecimal = context.gFraction.contains("0");
        boolean allowExpansion = context.gFraction.contains("+");
        boolean allowCollapse = context.gFraction.contains("-");
        // isolate if there is a non-zero numeral, and if so, what
        int numPlaces = -1; // initialize
        for (char ch : context.gFraction.toCharArray()) // find if non-zero character
            if (ch >= '1' && ch <= '9')
            {
                numPlaces = ch - '0';
                break;
            }

        // begin work with 9-digit fractional string
        String fraction = String.format("%09d", totalNanos);

        // find last non-zero digit
        int lastNonZero = -1;
        for (int i = 8; i >= 0; i--)
        {
            if (fraction.charAt(i) != '0')
            {
                lastNonZero = i;
                break;
            }
        }

        // if user didn't specify precision, use the significant length
        if (numPlaces < 0)
            numPlaces = lastNonZero + 1;

        // initial length
        int length = numPlaces;

        // expansion
        if (allowExpansion && lastNonZero >= 0)
            length = Math.max(length, lastNonZero + 1);

        length = Math.min(length, 9);

        String result = fraction.substring(0, length);

        // collapse
        if (allowCollapse)
        {
            if (lastNonZero < 0)
                result = "";
            else
            {
                int minLength = allowExpansion ? lastNonZero + 1 : 0;

                int end = result.length();
                while (end > minLength && result.charAt(end - 1) == '0')
                    end--;

                result = result.substring(0, end);
            }
        }

        // decimal point
        if (!result.isEmpty())
            return "." + result;
        // else
        return requireDecimal ? "." : "";
    }

    /**
     * Helper class.
     */
    private static class Context
    {
        final Duration time;
        final String format;

        String gTime;
        String[] aTimeParts;
        int startingUnitIndex;

        String gFraction;

        Context(Duration time, String format)
        {
            this.time = time;
            this.format = format;
        }
    }

    public static void main(String[] args)
    {
        String[] tests = new String[] {"HH:mm:ss.1", "m:ss", "::s", ":::", "mmmm:.", ":H::ss.3+",
                "test", "HHHH", "sss.3", ".11", "H:dd", "H:s", "::::", "mm:ss.1-", ":ss.1+-"};
        // last gives wrong error message (gives invalid time order), but acceptable
        Duration d = Duration.ofHours(3).plusMinutes(15);

        for (String t : tests)
        {
            System.out.println("Test pattern: " + t);
            Context context = new Context(d, t);

            try
            {
                validateAndParse(context);

                System.out.println("\t" + context.gTime + " (" + context.aTimeParts.length + " groups):");
                System.out.println("\tStarting unit: " + UNITS[context.startingUnitIndex]);
                System.out.println("\t" + context.gFraction + " (fractional): ");
                System.out.println("\t" + createUnitTimePortion(context));

            } catch (IllegalArgumentException e)
            {
                System.out.println("\t" + e);
            }
        }
    }
}


// TODO add - by fraction to allow less