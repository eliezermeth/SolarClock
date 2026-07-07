package util;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationTimeFormatter
{
    /**
     * Constructor made privete to prevent its use.
     */
    private DurationTimeFormatter() {}

    private static final char[] SECTIONS = {'d', 'H', 'm', 's'}; // in order
    private static final TimeUnit[] UNITS = {TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS};

    private static final Pattern CHARS =
            Pattern.compile("^[dHms:.+\\-0-9]+$"); // ensure no unexpected characters
    private static final Pattern SPLIT =
            Pattern.compile("^([^.]*)(?:\\.(.*))?$"); // split time portion from (optional) fractional portion
    private static final Pattern FRACTION =
            Pattern.compile("^0?[1-9]?\\+?-?$"); // if decimal point exists, validate what follows it
    private static final Pattern ORDER =
            Pattern.compile("^d*:?H*:?m*:?s*$"); // require them to be in proper order

    /**
     * Formats a {@link Duration} into a {@link String} of a more human-readable format.  The formatting can work with
     * the following segments: Days, minutes, hours, seconds, and fractions of seconds.  An example of the requested
     * format is {@code H:MM:SS.3+}.  The highest unit displayed (be it days, hours, minutes, or seconds) can have any
     * number of units; after that, the display will standardize to two units for each of the remainder to be displayed.
     *   The fractional units (after the decmial point) is different; its exact rules are explained below.
     * <p>
     * Sections (between days, hours, minutes, and seconds) must always be separated by a semicolon ({@code :}).  The
     * separation between seconds and fractional parts is delineated by a decmial point ({@code .}).  The highest time
     * section it will print is to the left of the first semicolon; after that, the remaining sections will proceed
     * linearly.
     * <p>
     * <ol>
     *     <li>The highest section it can print is days.  The number of {@code d}s is the minimum field width used for
     *     the display; as such, {@code dd} will cause a minimum field width of two (which may cause sub-ten day counts
     *     to be displayed as {@code 03:...}).  However, if this section should only display if there is at least one
     *     unit, then {@code :} should be used.  If there do exist units, then it will be formatted with no leading
     *     zeros.</li>
     *     <li>The next highest section it can print is hours.  This follows the same rules as does hours; however, if
     *     hours are a certain display ({code d:} used), then this section will always display with a field width of
     *     two.  If hours are an uncertain display ({@code :} used), then there are three options: {@code HH} if there
     *     should be two digits used as a certain display for digits, {@code H} if this may be the highest display (and
     *     should display) and will use a variable-width minimum field length, or {@code :} if this should only display
     *     if it is a non-zero number (and no higher section is displaying).</li>
     *     <li>The next section is minutes ({@code m}).  The same rules apply as for hours.</li>
     *     <li>The last section is seconds ({@code s}).  The same rules apply as for hours.</li>
     * If all three {@code :} are present, no units need be specified, as all units will be used.  If the decimal is
     * specified, then units need not be specified (even if less than three {@code :}), as they will be calculated using
     * seconds in the rightmost full-unit slot and calculated from there.
     * <p>
     * The decimal point ({@code .}) is not required after seconds; however, if it was used, remaining fractional units
     * may be displayed.  Fractional parts operate on a different set of rules:
     * {@code [force decimal display][precision][expansion][collapse]}
     * <ol>
     *     <li>The option {@code 0} may only appear in the first slot.  If present, it will force the decimal point to
     *     always display, regardless of later options that may change the formatting.</li>
     *     <li>The optional precision {@code 1 - 9} specifies how precise the result should be.  {@code 1} will
     *     return one-tenth seconds, {@code 2} one-one hundredths, up to {@code 9}, resulting in nanoseconds, the max
     *     precision available to {@link Duration}.  If this is not specified, it will default to the most precise value
     *     available within the given {@link Duration}, without the trailing zeros.</li>
     *     <li>The option {@code +} allows the precision to expand beyond the specified number to the most precise
     *     value available within the given {@link Duration} without the trailing zeros.  If both precision and
     *     expansion are specified (and not collapse), precision represents be the lower bound of units returned.</li>
     *     <li>The option {@code -} allows the precision to collapse beyond the specified number by removing any
     *     trailing zeros within the specified precision.  If both precision and collapse are specified (and not
     *     expansion), precision represents the upper bound of units returned.</li>
     * </ol>
     * Both {@code +} and {@code -} may be present within the same pattern.  If the decimal is not forced and it
     * collapses because of no significant return value, the decimal portion (along with the decimal) of the number will
     * not be displayed.
     *
     * @param duration {@link Duration} to be printed in the specified {@code pattern}
     * @param pattern {@link String} specifying the pattern based on the above rules
     * @return {@link Duration} in the format specified by the {@code pattern}
     */
    public static String format(Duration duration, String pattern)
    {
        validateOnlyPermittedChars(pattern);
        String[] aTimeAndFractionSegments = splitFormattingString(pattern);
        validateGeneralTimeOrder(aTimeAndFractionSegments[0]);

        // split time format based on semicolons
        String[] aTimeParts = aTimeAndFractionSegments[0].split(":", -1); // as many groups as possible

        // find starting unit index
        int startingUnitIndex = findStartingUnit(aTimeParts, aTimeAndFractionSegments[1]);

        validateOnlyFirstSlotExpanded(aTimeParts);
        validateFractionalSection(aTimeAndFractionSegments[1]);

        return createUnitTimePortion(duration, aTimeParts, startingUnitIndex) +
                createFractionalPortion(duration, aTimeAndFractionSegments[1]);
    }

    // ----------------------------------------------------------------------------------------------
    // Validation methods
    // ----------------------------------------------------------------------------------------------

    /**
     * Verify all characters present in string are permitted.
     * @param patten formatting pattern for {@link Duration}
     *
     * @throws IllegalArgumentException if invalid characters are present
     */
    private static void validateOnlyPermittedChars(String patten)
    {
        if (!CHARS.matcher(patten).matches())
            throw new IllegalArgumentException("Invalid characters present in formatting String " + patten);
    }

    /**
     * Test that the general order of the time segment is valid.
     * @param timeSegment the section of the time portion with the lowest possible unit of seconds
     *
     * @throws IllegalArgumentException if order is invalid
     */
    private static void validateGeneralTimeOrder(String timeSegment)
    {
        if (!ORDER.matcher(timeSegment).matches())
            throw new IllegalArgumentException("Invalid time order");
    }


    /**
     * Ensure that the only slot that can have 3+ characters is the first slot.
     *
     * @param aTimeParts {@link String} array of all full time parts
     *
     * @throws IllegalArgumentException if any slot except the leftmost contains greater than two characters
     */
    private static void validateOnlyFirstSlotExpanded(String[] aTimeParts)
    {
        for (int i = 1; i < aTimeParts.length; i++)
            if (aTimeParts[i].length() > 2)
                throw new IllegalArgumentException("Cannot have more than two characters in slot " + aTimeParts[i]);
    }

    /**
     * Ensure the fractional section is valid.
     *
     * @param fractional the portion of the format after the decimal point
     *
     * @throws IllegalArgumentException if the portion exists but does not match valid character orders
     */
    private static void validateFractionalSection(String fractional)
    {
        if (fractional != null && !FRACTION.matcher(fractional).matches())
            throw new IllegalArgumentException("Fractional section invalid.");
    }

    // ----------------------------------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------------------------------

    /**
     * Split the formatting {@code pattern} into its time (days:hours:minutes:seconds) and fractional (.mmm+) segments.
     *
     * @param pattern the formatting pattern for the {@link Duration} time that was given by the user
     * @return a {@link String} array containing the [time portion, fractional portion] in that order
     */
    private static String[] splitFormattingString(String pattern)
    {
        String[] aTimeAndFractionSegments = new String[2];
        Matcher split = SPLIT.matcher(pattern);
        if (split.matches())
        {
            aTimeAndFractionSegments[0] = split.group(1);
            aTimeAndFractionSegments[1] = split.group(2);
        }
        return aTimeAndFractionSegments;
    }

    /**
     * Find the lowest unit in the time unit section, be it days, hours, minutes, or seconds.  This is returned via the
     * index of the unit in the {@code SECTIONS} array.  Does not complete correctly if the parts section is formatted
     * incorrectly.
     *
     * @param aTimeParts the {@link String} array containing all parts of the full time units
     * @param fractionalExists if there is a fractional section requested for the pattern
     * @return the {@code SECTIONS} index of the rightmost unit within {@code aTimeParts}
     *
     * @throws IllegalArgumentException if time units are in invalid locations or not enough context is available to
     *                                  determine the leftmost unit
     */
    private static int findLowestUnit(String[] aTimeParts, boolean fractionalExists)
    {
        if (fractionalExists) // fractional exists
            return SECTIONS.length - 1; // 's'

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

    /**
     * Find the rightmost (starting) unit within the pattern requested.
     *
     * @param aTimeParts the {@link String} array containing all parts of the full time units
     * @param fractionalPart the fractional part of the requested pattern
     *
     * @return the {@code SECTIONS} index of the leftmost unit within {@code aTimeParts}
     *
     * @throws IllegalArgumentException if time units are in invalid locations or multiple units compose a single segment
     */
    private static int findStartingUnit(String[] aTimeParts, String fractionalPart)
    {
        int lowestUnit = findLowestUnit(aTimeParts, (fractionalPart != null));

        // ensure proper incremental order of time segments
        int starting = -1;

        // work backward on the time array
        for (int i = 1; i < aTimeParts.length + 1; i++)
        {
            String part = aTimeParts[aTimeParts.length - i];
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

        return starting;
    }

    /**
     * Test if a {@link String} is composed of a unique character.
     *
     * @param s the {@link String} to test
     * @return {@code true} if only one character; otherwise {@code false}
     */
    private static boolean allSameChar(String s)
    {
        char c = s.charAt(0);
        for (int i = 1; i < s.length(); i++)
            if (s.charAt(i) != c)
                return false;
        return true;
    }

    // ----------------------------------------------------------------------------------------------
    // Time conversion methods
    // ----------------------------------------------------------------------------------------------

    /**
     * Returns the number of {@link TimeUnit} within the {@link Duration}.
     *
     * @param d {@link Duration}
     * @param unit the {@link TimeUnit} to have its total extracted from the {@link Duration}
     * @return the number of {@link TimeUnit}s in {@link Duration}
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
     *
     * @param d {@link Duration}
     * @param unit the {@link TimeUnit} to have its total extracted from the {@link Duration}
     * @return the number of {@link TimeUnit}s in {@link Duration} below the next highest {@link TimeUnit}
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

    // ----------------------------------------------------------------------------------------------
    // String creation methods
    // ----------------------------------------------------------------------------------------------

    /**
     * Change the {@link Duration} into its {@link String} within the full-unit section (lowest unit seconds) based on
     * the format given in {@code timeFormatArray}.
     *
     * @param time {@link Duration}
     * @param timeFormatArray {@link String} array containing the formatting for the {@link Duration}
     * @param startingUnitIndex the index of the leftmost unit requested by the pattern, in the {@code UNITS} array
     * @return the {@link String} of the full time units in the requested pattern
     */
    private static String createUnitTimePortion(Duration time, String[] timeFormatArray, int startingUnitIndex)
    {
        StringBuilder sb = new StringBuilder();
        int minFieldWidth = timeFormatArray[0].length();

        for (int i = 0; i < timeFormatArray.length; i++)
        {
            // determine the resultant number for the section
            long drawnNumber;
            if (sb.isEmpty()) // no text exists yet
                drawnNumber = to(time, UNITS[startingUnitIndex + i]);
            else
                drawnNumber = toPart(time, UNITS[startingUnitIndex + i]);

            if (minFieldWidth == 0) // attempt to change minimum field width if non-existent
                minFieldWidth = timeFormatArray[i].length();

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
     * Change the fractional (less than a second) portion of the {@link Duration} into its {@link String} within the
     * fractional section of the format given iby {@code fractionPortion}
     * @param time {@link Duration}
     * @param fractionPortion the pattern for the fractional section
     * @return the {link String} of the fractional units in the requested pattern
     */
    private static String createFractionalPortion(Duration time, String fractionPortion)
    {
        // ensure valid fractional
        if (fractionPortion == null)
            return "";

        long totalNanos = time.toNanosPart(); // get total number of fractional units; max size

        boolean requireDecimal = fractionPortion.contains("0");
        boolean allowExpansion = fractionPortion.contains("+");
        boolean allowCollapse = fractionPortion.contains("-");
        // isolate if there is a non-zero numeral, and if so, what
        int numPlaces = -1; // initialize
        for (char ch : fractionPortion.toCharArray()) // find if non-zero character
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

    // ----------------------------------------------------------------------------------------------

    public static void main(String[] args)
    {
        String[] tests = new String[] {"HH:mm:ss.1", "m:ss", "::s", ":::", "mmmm:.", ":H::ss.3+",
                "test", "HHHH", "sss.3", ".11", "H:dd", "H:s", "::::", "mm:ss.1-", ":ss.1+-"};
        // last gives wrong error message (gives invalid time order), but acceptable
        Duration d = Duration.ofHours(3).plusMinutes(15).plusSeconds(2).plusNanos(123456789);

        for (String t : tests)
        {
            System.out.println("Test pattern: " + t);

            try
            {
                System.out.println("\tResult: " + format(d, t));

            } catch (IllegalArgumentException e)
            {
                System.out.println("\t" + e);
            }
        }
    }
}