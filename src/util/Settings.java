package util;

import java.awt.*;

public class Settings
{
    public static String location = "Pikesville";

    // Analog clock settings
    // View mode is the clock type - sundial, full day, etc
    public static ViewMode viewMode = ViewMode.SUNDIAL;

    // Analog clock colors
    public static Color DAY_COLOR = new Color(255, 255, 200);
    public static Color NIGHT_COLOR = new Color(100, 100, 255);
    public static Color TIME_HAND_COLOR = Color.RED;
}
