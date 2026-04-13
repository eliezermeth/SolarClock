package gui;

import java.awt.*;
import java.time.LocalTime;

public class StaticLine
{
    /** <code>LocalTime</code> for the position of the line/information on the analog clock. */
    protected LocalTime time;
    /** Text for the label; if text is blank, the text portion should not be displayed. */
    protected String label;
    /** Thickness of the line; if width is <code>0</code>, the line portion should not be displayed. */
    protected int thickness;
    /** Color of the line. */
    protected Color color;

    /**
     * Information for a static line to be displayed on the analog clock.
     * @param time <code>LocalTime</code> for when information should be calculated.
     * @param label Text for label; if text is blank, the text portion should not be displayed.
     * @param thickness Thickness of the line; if width is <code>0</code>, the line portion should not be displayed.
     * @param color Color of the line.
     */
    StaticLine(LocalTime time, String label, int thickness, Color color)
    {
        this.time = time;
        this.label = label;
        this.thickness = thickness;
        this.color = color;
    }
}
