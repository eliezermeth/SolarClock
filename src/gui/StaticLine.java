package gui;

import java.awt.*;
import java.time.LocalTime;

public class StaticLine
{
    protected LocalTime time;
    protected String label;
    protected int thickness;
    protected Color color;

    StaticLine(LocalTime time, String label, int thickness, Color color)
    {
        this.time = time;
        this.label = label;
        this.thickness = thickness;
        this.color = color;
    }
}
