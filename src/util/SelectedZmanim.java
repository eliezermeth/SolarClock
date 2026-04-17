package util;

/**
 * Functions as a "Settings" file for the types of zmanim that may be requested by the program.  Does not get the zmanim
 * from <code>ComplexZmanimCalendar</code>, but hold the toggleable values for the different options.
 */
public class SelectedZmanim
{
    /** Dawn: 72 minutes at 16.1 degrees */
    public static boolean ALOS_16_POINT_1_DEGREES = true;
    /** Earliest talis & tefillin (sun is 10.2 degrees below horizon) */
    public static boolean MISHEYAKIR_10_POINT_2_DEGREES = true;
    /** Sunrise */
    public static boolean SUNRISE = true;
    /** SZK"Sh'ma (M"A): (72 minutes at) 16.1 degrees */
    public static boolean SOF_ZMAN_SHMA_MGA_16_POINT_1_DEGREES = true;
    /** SZK"Sh'ma  (Gra & Baal HaTanya) */
    public static boolean SOF_ZMAN_SHMA_GRA = true;
    /** SZ"Tfila (Gra & Ball HaTanya) */
    public static boolean SOF_ZMAN_TFILA_GRA = true;
    /** Midday */
    public static boolean CHATZOS = true;
    /** Earliest mincha (lechumra) */
    public static boolean MINCHA_GEDOLA = true;
    /** Plag Ha"Mincha (Gra & Baal HaTanya) */
    public static boolean PLAG_HAMINCHA = true;
    /** Sunset */
    public static boolean SUNSET = true;
    /** Nightfall - 3 stars emerge (36 minutes as degrees) */
    public static boolean TZAIS = true;
    /** Nightfall - 72 minutes */
    public static boolean TZAIS_72 = true;
}
