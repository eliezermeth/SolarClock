package util;

import util.enums.SHAAH_TICK_MARK_STYLE;
import util.enums.ViewMode;

import java.awt.*;

public class Settings
{
    public static String location = "Pikesville";



    // ------------------------------------------------------------------
    // Analog clock settings
    // ------------------------------------------------------------------
    // View mode is the clock type - sundial, full day, etc
    public static ViewMode viewMode = ViewMode.SUNDIAL;

    // Analog clock colors
    /** Color for the day segment of the analog clock. */
    public static Color DAY_COLOR = new Color(255, 255, 200);
    /** Color for the night segment of the analog clock. */
    public static Color NIGHT_COLOR = new Color(100, 100, 255);
    /** Color for the current-time hand of the analog clock. */
    public static Color TIME_HAND_COLOR = Color.RED;

    /** If the tick marks for the sha'os should be displayed on the analog clock. */
    public static boolean ANALOG_SHAAH_TICK_MARKS_ENABLED = true;
    /** How the hourly tick marks should be delineated; see enum <code>SHAAH_TICK_MARK_STYLE</code> for options. */
    public static SHAAH_TICK_MARK_STYLE ANALOG_SHAAH_TICK_MARK_STYLE = SHAAH_TICK_MARK_STYLE.ONE_TWELFTH_OF_TEKUFAH;
    /** The color of the sha'ah tick marks of the analog clock. */
    public static Color ANALOG_SHAAH_TICK_MARKS_COLOR = Color.LIGHT_GRAY;
    /** Width for the tick mark marking the sha'ah. */
    public static byte ANALOG_SHAAH_TICK_MARK_WIDTH = 1;
    /** Whether the tick marks for the sha'os should have their standard times displayed on the analog clock. */
    public static boolean ANALOG_SHAAH_TIME_MARKINGS = true;

    /** How many days zmanim events should be retained for */
    public static int EVENT_RETENTION_DAYS = 2;
}
