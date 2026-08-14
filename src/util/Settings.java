package util;

import util.enums.Elevation;
import util.enums.MidpointMode;
import util.enums.SHAAH_TICK_MARK_STYLE;
import util.enums.ViewMode;

import java.awt.*;
import java.nio.file.Path;
import java.time.LocalTime;

public class Settings
{
    public static String location = "Pikesville";

    // ------------------------------------------------------------------
    // Analog clock settings
    // ------------------------------------------------------------------
    // View mode is the clock type - sundial, full day, etc
    public static ViewMode viewMode = ViewMode.SUNDIAL;
    /** If {@link ViewMode#DIAL} is selected, the time that should be on the top of the clock. */
    public static LocalTime dialModeTop = LocalTime.of(9, 0, 0);
    /** How {@link ViewMode#DIAL} is represented.  If {@code true}, then {@link Settings#dialModeTop} represents the
     * standard-clock time that will be on the top.  If {@code false}, it represents a halachic-clock time (in 24-hour
     * format), where 0:00 - 12:00 (not included) are the daylight hours, 12:00 - 18:00 represents the 6 halachic hours
     * from sunset until true midnight, and 18:00 - 24:00 the 6 halachic hours from true midnight until sunrise.  Please
     * note that in 24-hour representations, the clock starts from true midnight, so dawnNight occurs chronologically
     * before duskNight.
     */
    public static boolean dialModeStandard = true;

    // Analog clock colors
    /** Color for the day segment of the analog clock. */
    public static Color DAY_COLOR = new Color(255, 255, 200);
    /** Color for the twilight (dawn & dusk) segments of the clock when sun is between 0° and 6° below the horizon. */
    public static Color CIVIL_TWILIGHT_COLOR = new Color(199, 199, 220);
    /** Color for the twilight (dawn & dusk) segments of the clock when sun is between 6° and 12° below the horizon. */
    public static Color NAUTICAL_TWILIGHT_COLOR = new Color(170, 170, 230);
    /** Color for the twilight (dawn & dusk) segments of the clock when sun is between 12° and 18° below the horizon. */
    public static Color ASTRONOMICAL_TWILIGHT_COLOR = new Color(128, 128, 245);
    /** Color for the night segment of the analog clock. */
    public static Color NIGHT_COLOR = new Color(100, 100, 255);
    /** Color for the current-time hand of the analog clock. */
    public static Color TIME_HAND_COLOR = Color.RED;

    /** If the tick marks for the sha'os should be displayed on the analog clock. */
    public static boolean ANALOG_SHAAH_TICK_MARKS_ENABLED = true;
    /** How the hourly tick marks should be delineated; see enum <code>SHAAH_TICK_MARK_STYLE</code> for options. */
    public static SHAAH_TICK_MARK_STYLE ANALOG_SHAAH_TICK_MARK_STYLE = SHAAH_TICK_MARK_STYLE.ONE_TWELFTH_OF_TEKUFAH;
    /** The mainColor of the sha'ah tick marks of the analog clock. */
    public static Color ANALOG_SHAAH_TICK_MARKS_COLOR = Color.LIGHT_GRAY;
    /** Width for the tick mark marking the sha'ah. */
    public static byte ANALOG_SHAAH_TICK_MARK_WIDTH = 1;
    /** Whether the tick marks for the sha'os should have their standard times displayed on the analog clock. */
    public static boolean ANALOG_SHAAH_TIME_MARKINGS = true;
    /** If the midpoints should be centered between sunrise/sunset, or via the sun's zenith/nadir. */
    public static MidpointMode ANALOG_MIDPOINT_MODE = MidpointMode.ASTRONOMICAL;
    /** If it should use the user's current elevation, or sea level. */
    public static Elevation ANALOG_ELEVATION = Elevation.ACTUAL;
    /** If the twilights should be distinct color sections, or have a continuous gradient to appear as one period. */
    public static boolean DISTINCT_TWILIGHT = true;

    // ------------------------------------------------------------------
    // Other (should later be segmented)
    // ------------------------------------------------------------------
    /**
     * How many times per second the clock should update.<br>
     * Default: {@code 10}<br><br>
     * <b>Note for users:</b> If this number is set to {@code 1}, it will only update one time per second.  While this may
     * not seem to cause any difference for the standard clock, it will affect the displayed numbers for the halachic
     * clock sections, along with those displays with countdowns.  They will appear stuttery, due to their times not
     * necessarily lining up to the standard one-second length.  As such, higher numbers will provide a smoother
     * experience, but will increase system load.<br>
     * <b>Programmer's note:</b> This should eventually be moved into a settings file to allow for it to change.
     */
    public static int clockUpdatesPerSecond = 10;

    /** If the midpoints should be centered between sunrise/sunset, or via the sun's zenith/nadir. */
    public static MidpointMode MIDPOINT_MODE = MidpointMode.MEDIAN;
    /** If zmanim calculations should take the user's current elevation into account, or use sea level. */
    public static Elevation ELEVATION = Elevation.SEA_LEVEL;

    /** {@link Path} to config file containing zmanim settings; used by {@link ZmanOptionsConfigManager}. */
    public static Path zmanConfigFile = Path.of("src/util/ZmanimOptions");

}
