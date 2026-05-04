package util.enums;

public enum Zman
{
    /** Dawn: 72 minutes at 16.1 degrees */
    ALOS_16_POINT_1_DEGREES(
            "Dawn: 72 minutes at 16.1 degrees",
            "",
            "getAlos16Point1Degrees"),
    /** Earliest talis & tefillin (sun is 10.2 degrees below horizon) */
    MISHEYAKIR_10_POINT_2_DEGREES(
            "Earliest talis & tefillin (sun is 10.2 degrees below horizon)",
            "",
            "getMisheyakir10Point2Degrees"),
    /** Sunrise */
    SUNRISE(
            "Sunrise",
            "",
            "Sunrise"),
    /** SZK"Sh'ma (M"A): (72 minutes at) 16.1 degrees */
    SOF_ZMAN_SHMA_MGA_16_POINT_1_DEGREES(
            "SZK\"Sh'ma (M\"A): (72 minutes at) 16.1 degrees",
            "",
            "getSofZmanShmaMGA16Point1Degrees"),
    /** SZ"Tfila (Gra & Ball HaTanya) */
    SOF_ZMAN_TFILA_GRA(
            "SZ\"Tfila (Gra & Ball HaTanya)",
            "",
            "getSofZmanTfilaGRA"),
    /** Midday / Chatzos HaYom */
    MIDDAY(
            "Midday / Chatzos HaYom",
            "",
            "getChatzos"),
    /** Earliest Mincha (lechumra) */
    MINCHA_GEDOLA(
            "Earliest mincha (lechumra)",
            "",
            "getMinchaGedola"),
    /** Plag Ha"Mincha (Gra & Baal HaTanya) */
    PLAH_HAMINCHA_GRA(
            "Plag Ha\"Mincha (Gra & Baal HaTanya)",
            "",
            "getPlagHamincha"),
    /** Sunset */
    SUNSET(
            "Sunset",
            "",
            "getSunset"),
    /** Nightfall - 3 stars emerge (36 minutes as degrees) */
    TZAIS_3_STARS_36_MINS_AS_DEGREES(
            "Nightfall - 3 stars emerge (36 minutes as degrees)",
            "",
            "getTzais"),
    /** Nightfall - 72 minutes */
    TZAIS_72_MINS(
            "Nightfall - 72 minutes",
            "",
            "getTzais72"),
    /** Solar Midnight / Chatzos HaLailah */
    SOLAR_MIDNIGHT(
            "Chatzos HaLailah (Solar Midnight)",
            "",
            "getSolarMidnight");

    private final String title;
    private final String description;
    private final String methodName;

    /**
     * Constructor.
     * @param title Title/basic description or name of zman.
     * @param description More detailed description of what the zman is or how it is calculated.
     * @param methodName the method name (for reflection) which returns this zman, when called in
     * {@code ComplexZmanimCalendar} (or parent)
     */
    Zman(String title, String description, String methodName)
    {
        this.title = title;
        this.description = description;
        this.methodName = methodName;
    }

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
}
