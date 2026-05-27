package sandbox;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

// conical gradient arc
public class GradientArcTest
{
    private int x, y, w, h;
    private boolean reversedDraw;

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
     * @param reversedDraw {@code true} if the arc should be drawn in a clockwise direction
     */
    private void setReversedDraw(boolean reversedDraw)
    {
        this.reversedDraw = reversedDraw;
    }
    /**
     * Returns whether the drawing direction of arcs has been reversed; that is, instead of drawing in the normal
     * counterclockwise direction, arcs will be drawn in the clockwise direction.
     * @return  {@code true} if the arc is to be drawn in a clockwise direction
     */
    private boolean getReversedDraw()
    {
        return reversedDraw;
    }

    // DEPRECIATED
    public void drawArc(Graphics g, Color c, double start, double extent)
    {
        Graphics2D g2d = (Graphics2D) g;
        Arc2D.Double arc2D = new Arc2D.Double(x, y, w, h, start, extent, Arc2D.PIE);
        g2d.setColor(c);
        g2d.fill(arc2D);
    }

    /**
     * Draw a conical gradient arc changing from one color into the second.  It will start with the first color at full
     * saturation, and transition until it ends with the second color at full saturation.
     *
     * @param g {@link Graphics} object allowing arc to be drawn
     * @param c1 Starting {@link Color}
     * @param c2 Ending {@link Color}
     * @param startDegree The starting angle (in degrees) of the circle where the arc should begin, where {@code 0} is
     *                    the right side, {code 90} is the top, etc.
     * @param spanDegrees How many degrees the arc should cover from the {code startDegree}.  Positive results in a
     *                    counterclockwise direction, while negative causes a clockwise draw.
     * @param steps The number of arcs to be drawn to create the gradient.  Positive numbers will cause a smoother
     *              transition the larger the number is; negative numbers will draw a dynamic number of arcs based on
     *              the maximum possible arcs to be drawn based on the radians available.  {@code -1} will divide that
     *              number by {@code 1} (allowing maximum smoothness), {@code -2} by {@code 2}, etc.
     */
    public void drawGradientArc(Graphics g, Color c1, Color c2, double startDegree, double spanDegrees, int steps)
    {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int numSlices = calculateSteps(Math.abs(spanDegrees), steps);
        double sliceSize = spanDegrees / numSlices;
        double HIDE_RENDERING_GAPS = 0.5;
        // 0.2 = minimal (clean, high slice count), 0.5 = balanced, 1.0 = aggressive (safe, slightly distorts geometry)

        for (int i = 0; i < numSlices; i++)
        {
            double t = Math.clamp((double) i / (numSlices - 1), 0.0, 1.0); // calculate position in range of slices
            Color blended = interpolate(c1, c2, t); // get color at specified position between the two colors
            double sliceStart = startDegree + (sliceSize * i); // compute where the slice starts in the circle
            Arc2D.Double slice = new Arc2D.Double(x, y, w, h, sliceStart, sliceSize + HIDE_RENDERING_GAPS,
                    Arc2D.PIE);
            g2d.setColor(blended);
            g2d.fill(slice);
        }
    }

    public void drawClockwiseGradientArc(Graphics g, Color c1, Color c2, double startDegree, double spanDegrees, int steps)
    {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int numSlices = calculateSteps(Math.abs(spanDegrees), steps);
        double sliceSize = spanDegrees / numSlices;
        double HIDE_RENDERING_GAPS = 0.5;
        // 0.2 = minimal (clean, high slice count), 0.5 = balanced, 1.0 = aggressive (safe, slightly distorts geometry)

        double startAngle = startDegree + spanDegrees;

        for (int i = 0; i < numSlices; i++)
        {
            double t = Math.clamp((double) i / (numSlices - 1), 0.0, 1.0); // calculate position in range of slices
            Color blended = interpolate(c1, c2, t); // get color at specified position between the two colors
            double sliceStart = startAngle + (sliceSize * i * -1);
            Arc2D.Double slice = new Arc2D.Double(x, y, w, h, sliceStart, sliceSize + HIDE_RENDERING_GAPS,
                    Arc2D.PIE);
            g2d.setColor(blended);
            g2d.fill(slice);
        }
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
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(900, 600);

            GradientArcTest arc = new GradientArcTest();
            arc.setArc2DDoubleDimensions(100, 100, 300, 300);

            JPanel panel = new JPanel()
            {
                @Override
                protected void paintComponent(Graphics g)
                {
                    super.paintComponent(g);
                    //arc.drawArc(g, Color.RED, 0, 100);
                    arc.drawGradientArc(g, Color.YELLOW, Color.BLUE, 0, 90, -1);
                    //arc.drawGradientArc(g, Color.BLUE, Color.YELLOW, 0, -90, -1);

                    arc.setArc2DDoubleDimensions(400, 100, 300, 300);
                    arc.drawClockwiseGradientArc(g, Color.YELLOW, Color.BLUE, 0, 90, -1);
                }
            };

            panel.setBackground(Color.GRAY);

            frame.add(panel);
            frame.setVisible(true);
        });
    }
}
