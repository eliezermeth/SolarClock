package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

/**
 * Renders angular (conical) gradient arc segments using slice-based approximation.  The bounds of the arc (top-right XY
 * position and size of containing circle) should be set before drawing an arc.  These settings will persist until
 * changed, and may be modified with {@link ConicalGradientArc#setArc2DDoubleDimensions(int, int, int, int)}.  The
 * direction of drawing may also be modified from counterclockwise to clockwise.
 */
public class ConicalGradientArc
{
    private int x, y, w, h; // upper xy coordinates, width, and height of containing square of arc
    private boolean clockwiseDraw = false;

    /**
     * Draw a conical gradient arc changing from one color into the second.  It will start with the first color at full
     * saturation, and transition until it ends with the second color at full saturation.
     *
     * @param g {@link Graphics} object allowing arc to be drawn
     * @param c1 Starting {@link Color}
     * @param c2 Ending {@link Color}
     * @param startDegree The starting angle (in degrees) of the circle where the arc should begin, where {@code 0} is
     *                    the right side, {code 90} is the top, etc.
     * @param spanDegrees How many degrees the arc should cover from the {code startDegree}.  The primary direction will
     *                    be orchestrated by the {@code reversedDraw} variable.  A normal draw will draw in the
     *                    counterclockwise direction, and a reversed draw will draw in the clockwise direction.  Passing
     *                    a negative number will reverse the direction the arc is draw based on the {@code reversedDraw}
     *                    variable.
     * @param steps The number of arcs to be drawn to create the gradient.  Positive numbers will cause a smoother
     *              transition the larger the number is; negative numbers will draw a dynamic number of arcs based on
     *              the maximum possible arcs to be drawn based on the radians available.  {@code -1} will divide that
     *              number by {@code 1} (allowing maximum smoothness), {@code -2} by {@code 2}, etc.
     *
     * @throws IllegalArgumentException if attempting to draw an arc while the dimensions of the bounding square have a
     *                                  width or height of 0
     */
    public void drawGradientArc(Graphics g, Color c1, Color c2, double startDegree, double spanDegrees, int steps)
    {
        // TODO if solid color
        if (w == 0 || h == 0) // should this worry about xy coordinates off the pane?
            throw new IllegalArgumentException("Arc must have height and width.");

        Graphics2D g2d = (Graphics2D) g.create(); // create a clone to avoid messing with the original
        setRenderingHints(g2d);

        int numSlices = calculateSteps(Math.abs(spanDegrees), steps);
        double sliceSize = spanDegrees / numSlices;
        double HIDE_RENDERING_GAPS = 0.5;
        // 0.2 = minimal (clean, high slice count), 0.5 = balanced, 1.0 = aggressive (safe, slightly distorts geometry)

        // set variables based on reversedDraw
        double startAngle = (clockwiseDraw) ? (startDegree + spanDegrees) : startDegree; // if true, start at end
        int direction = (clockwiseDraw) ? -1 : 1;

        for (int i = 0; i < numSlices; i++)
        {
            double t = Math.clamp((double) i / (numSlices - 1), 0.0, 1.0); // calculate position in range of slices
            Color blended = interpolate(c1, c2, t); // get color at specified position between the two colors
            double sliceStart = startAngle + (sliceSize * i * direction); // compute where slice starts in the circle
            Arc2D.Double slice = new Arc2D.Double(x, y, w, h, sliceStart, sliceSize + HIDE_RENDERING_GAPS,
                    Arc2D.PIE);
            g2d.setColor(blended);
            g2d.fill(slice);
        }
    }

    // ---------------------------------------------------------------------------------------------------
    // Getters / setters
    // ---------------------------------------------------------------------------------------------------

    /**
     * Sets the location parameters for the {@link Arc2D.Double} arc.
     * @param x The X coordinate of the upper-left corner of the arc's framing rectangle.
     * @param y The Y coordinate of the upper-left corner of the arc's framing rectangle.
     * @param w The overall width of the full ellipse of which this arc is a partial section.
     * @param h The overall height of the full ellipse of which this arc is a partial section.
     */
    public void setArc2DDoubleDimensions(int x, int y, int w, int h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    /**
     * Arcs are normally drawn in a counterclockwise direction.  This method allows the arcs to be drawn in a clockwise
     * direction.
     * @param clockwiseDraw {@code true} if the arc should be drawn in a clockwise direction
     */
    public void setClockwiseDraw(boolean clockwiseDraw)
    {
        this.clockwiseDraw = clockwiseDraw;
    }

    /**
     * Returns whether the drawing direction of arcs has been reversed; that is, instead of drawing in the normal
     * counterclockwise direction, arcs will be drawn in the clockwise direction.
     * @return  {@code true} if the arc is to be drawn in a clockwise direction
     */
    public boolean getClockwiseDraw()
    {
        return clockwiseDraw;
    }

    // ---------------------------------------------------------------------------------------------------
    // Utility methods
    // ---------------------------------------------------------------------------------------------------

    /**
     * Set {@link Graphics2D} rendering hints for proper drawing
     * @param g2d {@link Graphics2D} object
     */
    private void setRenderingHints(Graphics2D g2d)
    {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * Returns a {@link Color} at the blend point {@code t} between the two given colors.
     * @param c1 Starting color.
     * @param c2 Ending color.
     * @param t Blend amount between {@code 0.0} and {@code 1.0} ({@code 0.0} = {@code c1}, {@code 1.0} = {@code c2})
     * @return blended {@link Color}
     */
    private Color interpolate(Color c1, Color c2, double t)
    {
        int r = (int) (c1.getRed() * (1 - t) + c2.getRed() * t);
        int g = (int) (c1.getGreen() * (1 - t) + c2.getGreen() * t);
        int b = (int) (c1.getBlue() * (1 - t) + c2.getBlue() * t);

        return new Color(r, g, b);
    }

    /**
     * Returns the number of slices to be drawn over an arc.
     * @param totalDegrees The total number of degrees covered by the arc.
     * @param steps The number of slices to be drawn.  If {@code >= 0} (a positive number), then it will return that
     *              number to be drawn as the slices.  If {@code < 0} (negative), then it will calculate the number of
     *              radians covered by the arc. {@code -1} will return that number to be drawn as slices (which is the
     *              maximum number of real slices to cover the arc), {@code -2} will return half that value, {@code -3}
     *              one-third, etc.
     * @return The number of slices to be drawn in the gradient arc.
     */
    public int calculateSteps(double totalDegrees, int steps)
    {
        if (steps >= 0)
            return steps;

        // steps is negative
        // calculate number of 1-pixel arc segments along the outer edge
        int maxSlices = (int) Math.ceil(Math.abs(Math.toRadians(totalDegrees) * (Math.min(w, h) / 2.0)));
        return Math.max(1, maxSlices / -steps); // return a minimum of 1 slice
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Gradient Arc Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 500);

            ConicalGradientArc arc = new ConicalGradientArc();

            JPanel panel = new JPanel()
            {
                @Override
                protected void paintComponent(Graphics g)
                {
                    super.paintComponent(g);

                    arc.setArc2DDoubleDimensions(100, 100, 100, 100);
                    arc.setClockwiseDraw(false);
                    arc.drawGradientArc(g, Color.YELLOW, Color.BLUE, 0, 90, -1);

                    arc.setArc2DDoubleDimensions(300, 100, 100, 100);
                    arc.setClockwiseDraw(true);
                    arc.drawGradientArc(g, Color.YELLOW, Color.BLUE, 0, 90, -1);
                }
            };

            frame.setContentPane(panel);
            frame.setVisible(true);
        });
    }
}
