package gui;

import javax.swing.*;

/**
 * For use with GridBagLayouts; allows for a single panel to cover multiple sections in a grid.
 */
public class GridRegion
{
    public final int x, y;
    public final int width, height;
    public final JPanel panel;

    /**
     * For use with GridBagLayouts; allows for a single panel to cover multiple sections in a grid.
     * @param x x-position in grid of start of large cell
     * @param y y-position in grid of start of large cell
     * @param width width of large cell
     * @param height height of large cell
     * @param panel <code>JPanel</code> to be used for large cell
     */
    public GridRegion(int x, int y, int width, int height, JPanel panel)
    {
        this.x = x; this.y = y;
        this.width = width; this.height = height;
        this.panel = panel;
    }
}
