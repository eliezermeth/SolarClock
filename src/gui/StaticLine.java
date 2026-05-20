package gui;

import java.awt.*;
import java.time.ZonedDateTime;

public class StaticLine
{
    /** <code>ZonedDateTime</code> for the position of the line/information on the analog clock. */
    protected ZonedDateTime time;
    /** Text for the label; if text is blank, the text portion should not be displayed. */
    protected String label;
    /** Thickness of the line; if width is <code>0</code>, the line portion should not be displayed. */
    protected int thickness;
    /** Color of the line. */
    protected Color color;
    /** If line should be a dotted line. */
    protected boolean isDotted = false;
    /** The <code>BasicStroke</code> style to be used by the dotted line. */
    protected BasicStroke stroke;

    /**
     * Information for a static line to be displayed on the analog clock.  Defaults to solid line, but can be changed
     * to be <code>dotted</code>.
     * @param time <code>ZonedDateTime</code> for when information should be calculated.
     * @param label Text for label; if text is blank, the text portion should not be displayed.
     * @param thickness Thickness of the line; if width is <code>0</code>, the line portion should not be displayed.
     * @param color Color of the line.
     */
    StaticLine(ZonedDateTime time, String label, int thickness, Color color)
    {
        this.time = time;
        this.label = label;
        this.thickness = thickness;
        this.color = color;
    }

    /**
     * Sets line to be a dotted line with the passed parameters.
     * @param dotLength The length of the solid sections of the line.
     * @param spaceLength The length of the empty sections of the line.
     */
    public void setDotted(float dotLength, float spaceLength)
    {
        isDotted = true;

        stroke = new BasicStroke(
                thickness,                      // line width
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                1f,
                new float[] {dotLength, spaceLength},
                0f
        );
    }
}
