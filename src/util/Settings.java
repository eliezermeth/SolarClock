package util;

import util.enums.SHAAH_TICK_MARK_STYLE;
import util.enums.ViewMode;

import java.awt.*;
import java.nio.file.Path;

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
    /** The color of the sha'ah tick marks of the analog clock. */
    public static Color ANALOG_SHAAH_TICK_MARKS_COLOR = Color.LIGHT_GRAY;
    /** Width for the tick mark marking the sha'ah. */
    public static byte ANALOG_SHAAH_TICK_MARK_WIDTH = 1;
    /** Whether the tick marks for the sha'os should have their standard times displayed on the analog clock. */
    public static boolean ANALOG_SHAAH_TIME_MARKINGS = true;

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

    /**
     * {@code Path} to config file containing zmanim settings; used by {@code ZmanOptionsConfigManager}.
     */
    public static Path zmanConfigFile = Path.of("src/util/ZmanimOptions");

}
