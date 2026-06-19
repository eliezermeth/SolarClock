package gui;

import main.ClockBrain;
import org.shredzone.commons.suncalc.MoonIllumination;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.time.ZonedDateTime;
import java.util.Date;

public class Moon extends JPanel
{
    private static final int DEFAULT_SIZE = 400;
    private static final int DEFAULT_DISPLAY_SIZE = 25;
    private static final int SUPERSAMPLING = 4;

    private static final Color DARK_MOON = new Color(55, 55, 55);
    private static final Color LIGHT_MOON = new Color(205, 205, 195);

    private BufferedImage moonImage;

    /**
     * Create a moon panel using the default displayed size.
     */
    public Moon()
    {
        this(DEFAULT_DISPLAY_SIZE);
    }

    /**
     * Create a moon panel using the given displayed size.
     *
     * @param displaySize width and height of the displayed moon panel, in pixels
     * @throws IllegalArgumentException if {@code displaySize} is not positive
     */
    public Moon(int displaySize)
    {
        if (displaySize <= 0)
            throw new IllegalArgumentException("displaySize must be positive");

        setOpaque(false);
        setPreferredSize(new Dimension(displaySize, displaySize));
    }

    /**
     * Generate and display the moon for the given date and time.
     *
     * @param dateTime date and time to render the moon for
     */
    public void setMoonTime(ZonedDateTime dateTime)
    {
        setMoonImage(generateMoon(dateTime));
    }

    /**
     * Set the high-resolution moon image that should be scaled when this panel is painted.
     *
     * @param moonImage image to display
     * @throws IllegalArgumentException if {@code moonImage} is {@code null}
     */
    public void setMoonImage(BufferedImage moonImage)
    {
        if (moonImage == null)
            throw new IllegalArgumentException("moonImage cannot be null");

        this.moonImage = moonImage;
        revalidate();
        repaint();
    }

    /**
     * Paint the high-resolution moon image scaled to the current panel size.
     *
     * @param g graphics context used for painting
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (moonImage == null)
            return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int squareSize = Math.min(getWidth(), getHeight());
        int x = (getWidth() - squareSize) / 2;
        int y = (getHeight() - squareSize) / 2;
        g2d.drawImage(moonImage, x, y, squareSize, squareSize, null);
        g2d.dispose();
    }

    /**
     * Generate an image of the moon at the given date and time using the default image size.
     *
     * @param dateTime date and time to render the moon for
     * @return image of the moon with a transparent background
     */
    public BufferedImage generateMoon(ZonedDateTime dateTime)
    {
        return generateMoon(dateTime, DEFAULT_SIZE);
    }

    /**
     * Generate an image of the moon at the given date and time.
     * <p>
     * The returned image has a transparent background, a dark disk for the unlit portion of the moon, and a silver
     * illuminated region based on the moon phase at {@code dateTime}.
     *
     * @param dateTime date and time to render the moon for
     * @param size width and height of the returned square image, in pixels
     * @return image of the moon with a transparent background
     * @throws IllegalArgumentException if {@code dateTime} is {@code null} or {@code size} is not positive
     */
    public BufferedImage generateMoon(ZonedDateTime dateTime, int size)
    {
        if (dateTime == null)
            throw new IllegalArgumentException("dateTime cannot be null");
        if (size <= 0)
            throw new IllegalArgumentException("size must be positive");

        MoonIllumination illumination = MoonIllumination.compute()
                .on(Date.from(dateTime.toInstant()))
                .execute();

        return renderMoon(illumination, size);
    }

    /**
     * Render the moon image from a precomputed {@link MoonIllumination}.
     *
     * @param illumination moon illumination data used to determine the lit portion of the disk
     * @param size width and height of the returned square image, in pixels
     * @return rendered moon image
     */
    private BufferedImage renderMoon(MoonIllumination illumination, int size)
    {
        int renderSize = size * SUPERSAMPLING;
        BufferedImage rendered = new BufferedImage(renderSize, renderSize, BufferedImage.TYPE_INT_ARGB);

        double moonPadding = SUPERSAMPLING;
        double moonSize = renderSize - (moonPadding * 2.0);
        Ellipse2D moonDisk = new Ellipse2D.Double(moonPadding, moonPadding, moonSize, moonSize);
        double fraction = illumination.getFraction();
        double phaseAngle = Math.toRadians(Math.abs(illumination.getPhase()));
        double terminatorWidth = moonSize * Math.abs(Math.cos(phaseAngle));

        Color baseColor = fraction < 0.5 ? DARK_MOON : LIGHT_MOON;
        Color overlayColor = fraction < 0.5 ? LIGHT_MOON : DARK_MOON;
        boolean waxing = illumination.getPhase() < 0.0;
        boolean overlayIsLit = fraction < 0.5;
        boolean overlayOnRight = overlayIsLit ? waxing : !waxing;

        Graphics2D g2d = rendered.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2d.setColor(baseColor);
        g2d.fill(moonDisk);

        if (fraction > 0.0 && fraction < 1.0)
        {
            g2d.setColor(overlayColor);
            g2d.fill(createPhaseSegment(moonDisk, terminatorWidth, overlayOnRight));
        }
        g2d.dispose();

        return downsample(rendered, size);
    }

    /**
     * Create the smaller phase segment that is drawn over the base moon disk.
     *
     * @param moonDisk outer moon disk bounds
     * @param terminatorWidth width of the terminator ellipse, in pixels
     * @param rightSide {@code true} if the segment should be drawn on the right side of the moon
     * @return path bounded by the moon edge and the inner terminator arc
     */
    private Path2D createPhaseSegment(Ellipse2D moonDisk, double terminatorWidth, boolean rightSide)
    {
        Path2D path = new Path2D.Double();
        double outerExtent = rightSide ? -180.0 : 180.0;
        Arc2D outerArc = new Arc2D.Double(moonDisk.getBounds2D(), 90.0, outerExtent, Arc2D.OPEN);

        path.append(outerArc, false);

        if (terminatorWidth <= 0.001)
        {
            path.lineTo(moonDisk.getCenterX(), moonDisk.getMinY());
        }
        else
        {
            double terminatorX = moonDisk.getCenterX() - terminatorWidth / 2.0;
            Ellipse2D terminatorBounds = new Ellipse2D.Double(
                    terminatorX, moonDisk.getY(), terminatorWidth, moonDisk.getHeight()
            );
            double terminatorExtent = rightSide ? 180.0 : -180.0;
            Arc2D terminatorArc = new Arc2D.Double(
                    terminatorBounds.getBounds2D(), -90.0, terminatorExtent, Arc2D.OPEN
            );
            path.append(terminatorArc, true);
        }

        path.closePath();
        return path;
    }

    /**
     * Downsample a supersampled image to the requested final image size.
     *
     * @param source larger source image
     * @param size width and height of the returned square image, in pixels
     * @return downsampled image
     */
    private BufferedImage downsample(BufferedImage source, int size)
    {
        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < size; y++)
        {
            for (int x = 0; x < size; x++)
            {
                result.setRGB(x, y, averagePixels(source, x * SUPERSAMPLING, y * SUPERSAMPLING));
            }
        }

        return result;
    }

    /**
     * Average one supersampled block into a single premultiplied-alpha ARGB pixel.
     *
     * @param source supersampled source image
     * @param startX left edge of the source block
     * @param startY top edge of the source block
     * @return averaged ARGB pixel
     */
    private int averagePixels(BufferedImage source, int startX, int startY)
    {
        double alphaSum = 0.0;
        double redSum = 0.0;
        double greenSum = 0.0;
        double blueSum = 0.0;
        int sampleCount = SUPERSAMPLING * SUPERSAMPLING;

        for (int y = startY; y < startY + SUPERSAMPLING; y++)
        {
            for (int x = startX; x < startX + SUPERSAMPLING; x++)
            {
                int argb = source.getRGB(x, y);
                double alpha = ((argb >>> 24) & 0xff) / 255.0;

                alphaSum += alpha;
                redSum += ((argb >>> 16) & 0xff) * alpha;
                greenSum += ((argb >>> 8) & 0xff) * alpha;
                blueSum += (argb & 0xff) * alpha;
            }
        }

        int alpha = (int) Math.round((alphaSum / sampleCount) * 255.0);
        if (alpha == 0)
            return 0;

        int red = (int) Math.round(redSum / alphaSum);
        int green = (int) Math.round(greenSum / alphaSum);
        int blue = (int) Math.round(blueSum / alphaSum);

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static void main(String[] args)
    {
        ClockBrain clock = ClockBrain.getInstance();
        ZonedDateTime now = clock.getCurrentDateTime();
        Moon moon = new Moon(DEFAULT_DISPLAY_SIZE);
        moon.setMoonTime(now);

        SwingUtilities.invokeLater(() ->
        {
            JFrame frame = new JFrame("Moon");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(moon, BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
