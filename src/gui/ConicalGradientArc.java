package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

/**
 * Renders angular (conical) gradient arc segments using slice-based approximation.
 */
public class ConicalGradientArc
{
    // upper xy coordinates, width, and height of containing square of arc
    private int x, y, w, h;

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
     * Fill an angular arc with a gradient of colors.
     * @param g2d Graphics contexts
     * @param centerX center x of circle
     * @param centerY center y of circle
     * @param radius radius of circle
     * @param startAngle start angle in radians (clockwise system expected)
     * @param endAngle end angle in radians (clockwise system expected)
     * @param startingFadeColor color that {@code mainColor} has halfway faded to at {@code startAngle}
     * @param mainColor main color for arc
     * @param endingFadeColor color that {@code mainColor} has halfway faded to at {@code endAngle}
     * @param startFadePercentOfArc percent of the arc's span that is used for this fade; {@code 1.0} for the full arc
     * @param endFadePercentOfArc percent of the arc's span that is used for this fade; {@code 1.0} for the full arc
     * @param stepsForFadeRegions number of slices ({@code -1} for max; otherwise, higher = smoother but slower)
     */
    public void fill(Graphics2D g2d, int centerX, int centerY, int radius, double startAngle, double endAngle,
                     Color startingFadeColor, Color mainColor, Color endingFadeColor,
                     double startFadePercentOfArc, double endFadePercentOfArc, int stepsForFadeRegions)
    {
        double span = normalizeClockwiseSpan(startAngle, endAngle);

        if ((span <= 0.0001) || (stepsForFadeRegions != -1 && stepsForFadeRegions <= 0)) return; // do not draw

        // prevent overlap for fade regions
        double startFadeRatio = Math.clamp(startFadePercentOfArc, 0, 1);
        double endFadeRatio = Math.clamp(endFadePercentOfArc, 0, 1);
        // prevent invalid geometries
        if (startFadeRatio + endFadeRatio > 1.0)
        {
            double scale = 1.0 / (startFadeRatio + endFadeRatio);
            startFadeRatio *= scale;
            endFadeRatio *= scale;
        }

        double startFadeDegrees = span * startFadeRatio;
        double endFadeDegrees = span * endFadeRatio;
        double middleDegrees = span - startFadeDegrees - endFadeDegrees; // remaining for solid middle

        // final slice count
        int startSlices = stepsForFadeRegions, endSlices = stepsForFadeRegions; // preset to parameter
        if (stepsForFadeRegions == -1) // modify if for max
        {
            startSlices = (int) Math.ceil(radius * Math.toRadians(startFadeDegrees));
            endSlices = (int) Math.ceil(radius * Math.toRadians(endFadeDegrees));
        }

        // Draw start gradient
        for (int i = 0; i < startSlices; i++)
        {
            float t = (startSlices <= 1) ? 1f : (float) i / (startSlices - 1);
            float mappedT = 0.5f + 0.5f * t; // x -> y, second half only
            Color blended = interpolate(startingFadeColor, mainColor, mappedT);
            double sliceStart = startAngle + (startFadeDegrees * i / startSlices);
            double sliceExtent = startFadeDegrees / startSlices + 0.5;
            Arc2D.Double slice = new Arc2D.Double(
                    centerX - radius, centerY - radius, radius * 2.0, radius * 2.0,
                    sliceStart, sliceExtent, Arc2D.PIE);
            g2d.setColor(blended);
            g2d.fill(slice);
        }

        // Draw solid middle
        Arc2D.Double middleArc = new Arc2D.Double(
                centerX - radius, centerY - radius, radius * 2.0, radius * 2.0,
                startAngle + startFadeDegrees, middleDegrees, Arc2D.PIE);
        g2d.setColor(mainColor);
        g2d.fill(middleArc);

        // Draw end gradient
        for (int i = 0; i < endSlices; i++)
        {
            float t = (endSlices <= 1) ? 1f : (float) i / (endSlices - 1);
            float mappedT = 0.5f * t; // y -> z, first half only
            Color blended = interpolate(mainColor, endingFadeColor, mappedT);
            double sliceStart = startAngle + startFadeDegrees + middleDegrees + (endFadeDegrees * i / endSlices);
            double sliceExtent = endFadeDegrees / endSlices + 0.5;
            Arc2D.Double slice = new Arc2D.Double(
                    centerX - radius, centerY - radius, radius * 2.0, radius * 2.0,
                    sliceStart, sliceExtent, Arc2D.PIE);
            g2d.setColor(blended);
            g2d.fill(slice);
        }

        // still need to fix seams
    }

    /**
     * Ensures always returns a clockwise positive span in radians.
     * @param start
     * @param end
     * @return
     */
    private double normalizeClockwiseSpan(double start, double end)
    {
        return (start - end + (2 * Math.PI)) % (2 * Math.PI);
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

    private int clamp(int v)
    {
        return Math.clamp(v, 0, 255);
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
                    Graphics2D g2d = (Graphics2D) g;

                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    int radius = 150;

                    // IMPORTANT: convert to degrees
                    double startAngle = 0;          // 0° = 3 o'clock
                    double endAngle = -270;         // clockwise 270° sweep

                    arc.fill(
                            g2d,
                            centerX,
                            centerY,
                            radius,
                            Math.toRadians(startAngle),
                            Math.toRadians(endAngle),
                            Color.PINK.darker(),
                            Color.BLUE,
                            Color.CYAN,
                            0.2,
                            0.2,
                            200
                    );
                }
            };

            frame.setContentPane(panel);
            frame.setVisible(true);
        });
    }
}
