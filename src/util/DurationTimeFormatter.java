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
            Pattern.compile("^\\d?\\+?-?$"); // if decimal point exists, validate what follows it
    private static final Pattern TOKEN =
            Pattern.compile("(d+|H+|m+|s+|:)"); // extract labeled fields
    private static final Pattern ORDER =
            Pattern.compile("^d*:?H*:?m*:?s*$"); // require them to be in proper order

    public static String format(Duration duration,String pattern)
    {
        long totalSeconds = duration.getSeconds();
        // for first item, can use TimeUnit.DAYS, etc.

        StringBuffer result = new StringBuffer();

        /*
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
         */

        return null;
    }

    private static boolean validate(String patten)
    {
        boolean status = true;

        // verify all characters are permitted
        if (!CHARS.matcher(patten).matches())
        {
            // System.out.println("\tIllegal characters present in formatting sequence: " + patten);
            // throw new IllegalArgumentException("Illegal characters present in formatting sequence.");
            status = false;
        }

        return status;
    }

    private static boolean allSameChar(String s)
    {
        char c = s.charAt(0);
        for (int i = 1; i < s.length(); i++)
            if (s.charAt(i) != c)
                return false;
        return true;
    }

    private static boolean findStartingUnit(String[] parts, boolean fractionalExists)
    {
        char lowestUnit = findLowestUnit(parts, fractionalExists);

        // ensure proper incremental order of time segments
        TimeUnit starting = null;

        // work backward on the time array
        for (int i = 1; i < parts.length + 1; i++)
        {
            String part = parts[parts.length - i];
            char expectedChar = SECTIONS[SECTIONS.length - i]; // TODO wrong

            if (!part.isEmpty()) // text exists in segment
            {
                char first = part.charAt(0);
                // must match expected character
                if (first != expectedChar)
                    throw new IllegalArgumentException("Time segment " + part + " is incorrect.");
                // must all be the same character
                for (int j = 1; j < part.length(); j++)
                    if (part.charAt(j) != first)
                        throw new IllegalArgumentException("Time segments may only be composed of a single type of unit");
            }

            starting = UNITS[UNITS.length - i];
        }
    }

    /**
     * Find the lowest unit in the time unit section, be it days, hours, minutes, or seconds.  Does not complete
     * correctly if the parts section is formatted incorrectly.
     * @param parts
     * @param fractionalExists
     * @return
     */
    private static char findLowestUnit(String[] parts, boolean fractionalExists)
    {
        if (fractionalExists)
        {
            return SECTIONS[SECTIONS.length - 1]; // 's'
        }

        // else; fractional does not exist
        for (int i = 1; i < parts.length + 1; i++) // traverse backward
        {
            String part = parts[parts.length - i];

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

                return SECTIONS[potentialIndex]; // lowest unit
            }
        }

        // no populated section found
        if (parts.length == SECTIONS.length)
            return SECTIONS[SECTIONS.length - 1];
        else // less than max extent
            throw new IllegalArgumentException("Not enough context in time section.");
    }

    public static void main(String[] args)
    {
        String[] tests = new String[] {"HH:mm:ss.1", "m:ss", "::s", ":::", "mmmm:.", ":H::ss.3+",
                "test", "HHHH", "sss.3", ".11", "H:dd", "H:s", "::::", "mm:ss.1-", ":ss.1+-"};
        // last gives wrong error message (gives invalid time order), but acceptable
        Duration d = Duration.ofHours(3);

        for (String t : tests)
        {
            System.out.println("Test pattern: " + t);

            // only valid char
            boolean onlyValidChars = CHARS.matcher(t).matches();
            if (!onlyValidChars)
            {
                System.out.println("\tInvalid characters");
                continue;
            }

            // split groups of string
            String gTime = null, gFraction = null;
            Matcher split = SPLIT.matcher(t);
            if (split.matches())
            {
                gTime = split.group(1);
                gFraction = split.group(2);
            }

            // validate general order of time segments
            boolean order = ORDER.matcher(gTime).matches();
            if (!order)
            {
                System.out.println("\tInvalid time order");
                continue;
            }

            // split based on semicolons
            String[] parts = gTime.split(":", -1); // as many groups as possible
            System.out.println("\t" + gTime + " (" + parts.length + " groups):");

            // ensure incremental order of time segments
            TimeUnit starting = null;
            if (gFraction != null) // lowest must be seconds, etc.
            {
                boolean stillValid = true;

                // work backward on the time array
                for (int i = 1; i < parts.length + 1 && stillValid; i++)
                {
                    String part = parts[parts.length - i];
                    char expected = SECTIONS[SECTIONS.length - i];

                    if (!part.isEmpty())
                    {
                        char first = part.charAt(0);
                        // must match expected character
                        if (first != expected)
                        {
                            stillValid = false;
                            System.out.println("\tExisting character is not expected in slot " + expected);
                        }

                        // must all be same character
                        for (int j = 1; j < part.length() && stillValid; j++)
                            if (part.charAt(j) != first)
                            {
                                stillValid = false;
                                System.out.println("\tAll characters must be same in slot " + expected);
                                break;
                            }
                    }
                    starting = UNITS[UNITS.length - i];
                }
                System.out.println("\tStarting unit: " + starting);
            }
            else // no fractional portion; don't know what the lowest is yet
            {
                int passed;
                char found;
                boolean noCharFound = true;

                // traverse backward (so that can be integrated with with-fractional code
                for (int i = 1; i < parts.length + 1 && noCharFound; i++)
                {
                    if (!parts[parts.length - i].isEmpty()) // section is not empty
                    {
                        // pull out first character as proper unit
                        int validCharIndex = -1;
                        char pos = parts[parts.length - i].charAt(0);
                        for (int j = 0; j < SECTIONS.length; j++)
                            if (pos == SECTIONS[j])
                            {
                                validCharIndex = j;
                                break;
                            }
                    }
                }
            }
            // ensure 3+ characters can only be in first slot
            for (int i = 1; i < parts.length; i++)
                if (parts[i].length() > 2)
                    System.out.println("\tCannot have more than two characters in slot " + parts[i]);

            // validate fractional characters
            if (gFraction != null)
            {
                System.out.println("\t" + gFraction + " (fractional): " + FRACTION.matcher(gFraction).matches());
            }
        }
    }
}
// TODO add - by fraction to allow less