package util.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Zman
{
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // IMPORTANT: Any change made to IDs here must be mirrored in ZmanimOptions file.
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    /** Dawn: 72 minutes at 16.1 degrees */
    ALOS_16_POINT_1_DEGREES(
            "alos-16.1-degrees",
            "Dawn: 72 minutes at 16.1 degrees",
            "",
            "getAlos16Point1Degrees"),
    /** Earliest talis & tefillin (sun is 10.2 degrees below horizon) */
    MISHEYAKIR_10_POINT_2_DEGREES(
            "misheyakir-10.2-degrees",
            "Earliest talis & tefillin (sun is 10.2 degrees below horizon)",
            "",
            "getMisheyakir10Point2Degrees"),
    /** Sunrise */
    SUNRISE(
            "sunrise",
            "Sunrise",
            "",
            "getSunrise"),
    /** SZK"Sh'ma (M"A): (72 minutes at) 16.1 degrees */
    SOF_ZMAN_SHMA_MGA_16_POINT_1_DEGREES(
            "szks-mga-16.1-degrees",
            "SZK\"Sh'ma (M\"A): (72 minutes at) 16.1 degrees",
            "",
            "getSofZmanShmaMGA16Point1Degrees"),
    /** SZ"Tfila (Gra & Ball HaTanya) */
    SOF_ZMAN_TFILA_GRA(
            "szt-gra",
            "SZ\"Tfila (Gra & Ball HaTanya)",
            "",
            "getSofZmanTfilaGRA"),
    /** Midday / Chatzos HaYom */
    MIDDAY(
            "chatzos-hayom",
            "Midday / Chatzos HaYom",
            "",
            "getChatzos"),
    /** Earliest Mincha (lechumra) */
    MINCHA_GEDOLA(
            "mincha-gedolah",
            "Earliest mincha (lechumra)",
            "",
            "getMinchaGedola"),
    /** Plag Ha"Mincha (Gra & Baal HaTanya) */
    PLAH_HAMINCHA_GRA(
            "plag-hamincha-gra",
            "Plag Ha\"Mincha (Gra & Baal HaTanya)",
            "",
            "getPlagHamincha"),
    /** Sunset */
    SUNSET(
            "sunset",
            "Sunset",
            "",
            "getSunset"),
    /** Nightfall - 3 stars emerge (36 minutes as degrees) */
    TZAIS_3_STARS_36_MINS_AS_DEGREES(
            "tzais-3stars-36min-as-degrees",
            "Nightfall - 3 stars emerge (36 minutes as degrees)",
            "",
            "getTzais"),
    /** Nightfall - 72 minutes */
    TZAIS_72_MINS(
            "tzais-72min",
            "Nightfall - 72 minutes",
            "",
            "getTzais72"),
    /** Solar Midnight / Chatzos HaLailah */
    SOLAR_MIDNIGHT(
            "chatzos-halailah",
            "Chatzos HaLailah (Solar Midnight)",
            "",
            "getSolarMidnight");

    private final String id;
    private final String title;
    private final String description;
    private final String methodName;

    /**
     * Constructor.
     * @param id Static id for zman.
     * @param title Title/basic description or name of zman.
     * @param description More detailed description of what the zman is or how it is calculated.
     * @param methodName the method name (for reflection) which returns this zman, when called in
     * {@code ComplexZmanimCalendar} (or parent)
     */
    Zman(String id, String title, String description, String methodName)
    {
        this.id = id;
        this.title = title;
        this.description = description;
        this.methodName = methodName;
    }

    /**
     * Returns the static ID for the zman.  This ID is not changed, and is the key to referencing a saved zman.
     * @return static ID of zman
     */
    public String getId() { return id; }
    /**
     * Returns the title/name for this {@code Zman}.
     * @return title
     */
    public String getTitle() { return title; }
    /**
     * Returns the explanation for this {@code Zman}.
     * @return description of this {@code Zman}
     */
    public String getDescription() { return description; }

    /**
     * Returns the method name to call in {@code ComplexZmanimCalendar} to get zman (may use parent).
     * @return method name for reflection
     */
    public String getMethodName() { return methodName; }

    // Lookup map
    private static final Map<String, Zman> BY_ID =
            Arrays.stream(values()).collect(Collectors.toMap(Zman::getId, z -> z));

    /**
     * Returns the {@code enum} of the proper zman from its id.
     * @param id static ID of zman
     * @return {@code Zman}
     */
    public static Zman fromId(String id) { return BY_ID.get(id); }
}
