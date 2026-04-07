package util;

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
    /** The color of the sha'ah tick marks of the analog clock. */
    public static Color ANALOG_SHAAH_TICK_MARKS_COLOR = Color.GRAY;
    /** Whether the tick marks for the sha'os should have their standard times displayed on the analog clock. */
    public static boolean ANALOG_SHAAH_TIME_MARKINGS = true;
}
