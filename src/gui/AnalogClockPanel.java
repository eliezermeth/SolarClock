package gui;

import interfaces.TerminatorObserver;
import interfaces.TimeObserver;
import interfaces.EqualViewOption;
import main.ClockBrain;
import util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AnalogClockPanel extends JPanel implements TimeObserver, TerminatorObserver, EqualViewOption
{
    private ClockBrain clock; // singleton; provider

    // for use to set lines at proper position in circle
    /**
     * Radians for the position of sunrise on the clock.
     */
    private double offsetSunrise;
    /**
     * Radians for the position of sunset on the clock.
     */
    private double offsetSunset;
    // range of day and night of circle
    /**
     * The distance clockwise from the sunrise angle to the sunset angle (e.g. day).
     */
    private double angularSpanDay;
    /**
     * The distance clockwise from the sunset angle to the sunrise angle (e.g. night).
     */
    private double angularSpanNight;

    // Cached layout; recalculated based on window size whenever changed
    private int diameter, radius, centerX, centerY;

    // Static elements
    private final List<StaticLine> staticLines = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Cached static image
    private BufferedImage staticImage;
    private int imageScale = 2;
    private final boolean USE_BUFFERED_IMAGE = false; // will need to search for all uses when completed

    public AnalogClockPanel()
    {
        clock = ClockBrain.getInstance();

        // register with ClockBrain as an observer
        clock.registerTimeObserver(this);
        clock.registerTerminatorObserver(this);

        // Listen to resize events to recalculate layout of clock components sizes
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                calculateLayoutDimensionPositions();
                createStaticImage();
                repaint();
            }
        });
        calculateLayoutDimensionPositions();

        // calculate proper angles for sunrise and sunset; recalculated when terminators changed / ViewMode changed
        calculateEqualDayNightView();

        if (USE_BUFFERED_IMAGE)
            createStaticImage();
    }

    @Override
    protected void paintComponent(Graphics g) // master control; can be collapsed when buffered image fixed
    {
        if (USE_BUFFERED_IMAGE)
            paintComponentBufferedImage(g);
        else
            paintComponentStandard(g);
    }

    protected void paintComponentBufferedImage(Graphics g) // OPTION 1 - part a
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // smoother edges
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY); // prioritize quality over speed
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE); // more accurate stroke rendering

        // Draw static image
        if (staticImage != null)
        {
            AffineTransform originalTransform = g2d.getTransform(); // save current g2d settings

            g2d.scale(1.0d / imageScale, 1.0d / imageScale); // change for bufferedImage
            g2d.drawImage(staticImage, 0, 0, null);

            g2d.setTransform(originalTransform); // return to original settings
        }

        // Draw dynamic current time hand
        if (clock.getCurrentTime() != null)
        {
            g2d.setColor(Settings.TIME_HAND_COLOR);
            g2d.setStroke(new BasicStroke(1));
            drawLineAtTime(g2d, centerX, centerY, radius, clock.getCurrentTime());
        }
    }

    private void createStaticImage() // BufferedImage OPTION 1 - part 2
    {
        if (getWidth() <= 0 || getHeight() <= 0) return; // window does not show

        staticImage = new BufferedImage(getWidth() * imageScale, getHeight() * imageScale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = staticImage.createGraphics();

        // High-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // smoother edges
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY); // prioritize quality over speed
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE); // more accurate stroke rendering
        g2d.scale(imageScale, imageScale); // scale down when drawing

        // Draw the top half of the circle
        Arc2D.Double dayArc = new Arc2D.Double(centerX - radius, centerY - radius, diameter, diameter,
                Math.toDegrees(offsetSunset),
                Math.toDegrees((offsetSunrise - offsetSunset + (2 * Math.PI)) % (2 * Math.PI)),
                Arc2D.PIE); // pie slice (filled); more accurate than fillArc()
        g2d.setColor(Settings.DAY_COLOR); // Light yellow
        g2d.fill(dayArc);

        // Draw the bottom half of the circle
        Arc2D.Double nightArc = new Arc2D.Double(centerX - radius, centerY - radius, diameter, diameter,
                Math.toDegrees(offsetSunrise),
                360 - Math.toDegrees((offsetSunrise - offsetSunset + (2 * Math.PI)) % (2 * Math.PI)),
                Arc2D.PIE); // pie slice (filled); more accurate than fillArc()
        g2d.setColor(Settings.NIGHT_COLOR); // Light deep blue
        g2d.fill(nightArc);

        // Draw static lines with optional labels
        for (StaticLine line : staticLines)
        {
            g2d.setColor(line.color);
            g2d.setStroke(new BasicStroke(line.thickness));
            drawLineAtTime(g2d, centerX, centerY, radius, line.time);

            if (line.label != null)
            {
                drawLabel(g2d, centerX, centerY, radius, line.time, line.label);
            }
        }

        // Draw circle outline
        drawBoundingOutline(g2d, centerX - radius, centerY - radius, diameter, diameter);

        g2d.dispose();
    }

    protected void paintComponentStandard(Graphics g) // normal draw OPTION 2 - part 1
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawClock(g2d);
    }

    private void drawClock(Graphics2D g2d) // normal draw OPTION 2 - part 2
    {

        // Draw the top half of the circle
        Arc2D.Double dayArc = new Arc2D.Double(centerX - radius, centerY - radius, diameter, diameter,
                Math.toDegrees(offsetSunset),
                Math.toDegrees((offsetSunrise - offsetSunset + (2 * Math.PI)) % (2 * Math.PI)),
                Arc2D.PIE); // pie slice (filled); more accurate than fillArc()
        g2d.setColor(Settings.DAY_COLOR); // Light yellow
        g2d.fill(dayArc);

        // Draw the bottom half of the circle
        Arc2D.Double nightArc = new Arc2D.Double(centerX - radius, centerY - radius, diameter, diameter,
                Math.toDegrees(offsetSunrise),
                360 - Math.toDegrees((offsetSunrise - offsetSunset + (2 * Math.PI)) % (2 * Math.PI)),
                Arc2D.PIE); // pie slice (filled); more accurate than fillArc()
        g2d.setColor(Settings.NIGHT_COLOR); // Light deep blue
        g2d.fill(nightArc);

        // Draw static lines with optional labels
        for (StaticLine line : staticLines)
        {
            g2d.setColor(line.color);
            g2d.setStroke(new BasicStroke(line.thickness));
            drawLineAtTime(g2d, centerX, centerY, radius, line.time);

            if (line.label != null)
            {
                drawLabel(g2d, centerX, centerY, radius, line.time, line.label);
            }
        }

        // Draw the dynamic current time hand
        if (clock.getCurrentTime() != null)
        {
            g2d.setColor(Settings.TIME_HAND_COLOR);
            g2d.setStroke(new BasicStroke(1));
            drawLineAtTime(g2d, centerX, centerY, radius, clock.getCurrentTime());
        }

        // Draw the circle outline; last to place over other elements that may overlap
        drawBoundingOutline(g2d, centerX - radius, centerY - radius, diameter, diameter);
    }

    private void drawLineAtTime(Graphics2D g2d, int centerX, int centerY, int radius, LocalTime time)
    {
        double angle = calculateAngle(time);
        double endX = centerX + radius * Math.cos(angle);
        double endY = centerY - radius * Math.sin(angle);
        g2d.draw(new Line2D.Double(centerX, centerY, endX, endY));
    }

    /**
     * Draw the circle outline; drawn last to place it over other elements that may overlap.
     *
     * TODO change for different viewmodes?
     */
    private void drawBoundingOutline(Graphics2D g2d, int upperLeftX, int upperLeftY, int width, int height)
    {
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(new Ellipse2D.Double(upperLeftX, upperLeftY, width, height));
        g2d.setStroke(new BasicStroke(1.0f)); // reset stroke
    }

    private void drawLabel(Graphics2D g2d, int centerX, int centerY, int radius, LocalTime time, String label)
    {
        double angle = calculateAngle(time);

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        int textHeight = fm.getAscent();

        int padding = 10;
        int labelRadius = radius + padding + textHeight;

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        int labelX = (int) (centerX + labelRadius * cos);
        int labelY = (int) (centerY - labelRadius * sin);

        // Horizontal alignment
        if (Math.abs(cos) < 0.2)
            labelX -= textWidth / 2;
        else if (cos < 0)
            labelX -= textWidth;

        // Vertical alignment
        if (Math.abs(sin) < 0.2)
            labelY += textHeight / 2;
        else if (sin > 0)
            labelY += textHeight;

        g2d.setColor(Color.BLACK);
        g2d.drawString(label, labelX, labelY);
    }

    private double calculateAngle(LocalTime time)
    {
        double angularSpan; // the span used for the tekufah in the calculation
        int targetedTekufah; // what tekufah the targeted time is during
        boolean daytime;

        // Test if within the first tekufah; t0 <= time < t1
        if (!time.isBefore(clock.getTerminatorTimes().getTerminator(0)) && // time is at or after first terminator
                time.isBefore(clock.getTerminatorTimes().getTerminator(1))) // time is strictly before second
        {
            targetedTekufah = 0; // use first and second terminators
            if (clock.getTerminatorTimes().getStartingTerminator().equals(Terminator.SUNRISE))
            {
                angularSpan = angularSpanDay; // use day span for percent; use day calculations
                daytime = true;
            }
            else
            {
                angularSpan = angularSpanNight; // use night span for percent; use night calculations
                daytime = false;
            }
        }
        else // during second span
        {
            targetedTekufah = 1; // use second and third terminators
            if (clock.getTerminatorTimes().getStartingTerminator().equals(Terminator.SUNRISE))
            {
                angularSpan = angularSpanNight;
                daytime = false;
            }
            else
            {
                angularSpan = angularSpanDay;
                daytime = true;
            }
        }

        long totalSeconds = clock.getTerminatorTimes().getTekufahSpan(targetedTekufah);
        long currentSeconds = TimeUtil.calculateMillisBetween(clock.getTerminatorTimes().getTerminator(targetedTekufah),
                time);

        // get percent of angular span
        double spanToAdd = angularSpan * ((double) currentSeconds / totalSeconds);
        if (daytime)
            return offsetSunrise - spanToAdd;
        else
            return offsetSunset - spanToAdd; // POTENTIAL - change to +
    }

    // TODO update to accommodate ViewModes
    /**
     * Sets proper values for offsets of sunrise and sunset.
     */
    public void calculateEqualDayNightView()  // ---------------------- For clock only
    {
        // TODO should only need to be triggered once per tekufah, or whenever equalDayNight is changed

        if (Settings.viewMode.equalDayNightView) // TODO how best to change this
        {
            offsetSunrise = Circle.LEFT.radians; // where 9 o'clock would be on a normal clock
            offsetSunset = Circle.RIGHT.radians; // where 3 o'clock would be on a normal clock; use instead of 0 for further calculations
        }
        else
        {
            /*
            By setting to this, the day and night arcs must now be modified. They can no longer be 50/50, but must
            calculate the percentage of the 24-hour period is contained by each.  Day should be centered with chatzos
            at the top, and night corresponding.  It will need to recalculate and repaint at terminator change.  All
            calculations must happen as an offset of these times.
             */

            // The current tekufah will have its percentage rendered correctly (the other may be slightly too
            // large/small); however, the day portion will be centered to the top of the 24-hour circle.
            long periodTime = clock.getTerminatorTimes().getTekufahSpan(0);

            double topPeriodTime = periodTime; // initialize to daytime
            if (clock.getTerminatorTimes().getStartingTerminator().equals(Terminator.SUNSET))
                topPeriodTime = Constants.MILLIS_PER_DAY - periodTime; // if first period is night, get 24-hour remainder and set top

            double percentOfCircle = (topPeriodTime / Constants.MILLIS_PER_DAY) * 100;
            double halfDaySegment = percentOfCircle / 2;
            double radianOnePercent = (2 * Math.PI) / 100;
            double sunriseAngle = Circle.TOP.radians + (halfDaySegment * radianOnePercent);
            double sunsetAngle = ((Circle.TOP.radians - (halfDaySegment * radianOnePercent)) +
                    Circle.RIGHT.radians) % Circle.RIGHT.radians; // force it to be a positive number

            offsetSunrise = sunriseAngle;
            offsetSunset = sunsetAngle;
        }

        // calculate the distance clockwise from sunrise angle to sunset angle (e.g. day)
        angularSpanDay = (offsetSunrise - offsetSunset + (2 * Math.PI)) % (2 * Math.PI);
        angularSpanNight = (2 * Math.PI) - angularSpanDay;

        if (USE_BUFFERED_IMAGE)
            createStaticImage();
    }

    /**
     * Calculate the window sizes for the clock; only recalculated on resize.
     */
    private void calculateLayoutDimensionPositions()
    {
        int width = getWidth();
        int height = getHeight();

        diameter = Math.min(width, height) - 50; // padding of 50 around circle
        radius = diameter / 2;
        centerX = width / 2;
        centerY = height / 2;
    }

    /**
     * Display sunrise and sunset lines.
     */
    public void addTerminatorLines()
    {
        this.addStaticLine(clock.getTerminatorTimes().getTerminator(0),
                clock.getTerminatorTimes().getStartingTerminator().toString(), 3, Color.GREEN);
        Terminator other = clock.getTerminatorTimes().getStartingTerminator().equals(Terminator.SUNRISE) ?
                Terminator.SUNSET : Terminator.SUNRISE;
        this.addStaticLine(clock.getTerminatorTimes().getTerminator(1), other.toString(), 3, Color.GREEN);
    }

    /**
     * Display hour tick marks between sunrise and sunset: 1 - 11 in day; 13 - 23 in night
     */
    public void addHourTickMarks()
    {
        // calculate shaah for the time period
        // start of tekufah; offset of time due to day or night
        int[][] tekufahSettings = new int[][] {
                new int[] { 0, clock.getTerminatorTimes().getStartingTerminator().equals(Terminator.SUNRISE) ? 0 : 12},
                new int[] { 1, clock.getTerminatorTimes().getStartingTerminator().equals(Terminator.SUNSET) ? 0 : 12}
        };

        for (int[] tekufah : tekufahSettings)
        {
            LocalTime tekufahStart = clock.getTerminatorTimes().getTerminator(tekufah[0]);
            long tekufahShaah = clock.getTerminatorTimes().getTekufahShaah(tekufah[0]);

            for (int i = 0; i < 12; i++) // 0 to include terminator, 1 to exclude
            {
                LocalTime tickMark = tekufahStart.plusSeconds(
                        (tekufahShaah / 1000) * i); // millis to seconds, then multiply by hours
                this.addStaticLine(tickMark, tickMark.format(formatter), 1, Color.LIGHT_GRAY);
            }
        }
    }

    /**
     * Add a custom static line to the analog clock.
     * @param time <code>LocalTime</code> for the position of the line.
     * @param label Text for the label.
     * @param thickness Thickness of the line.
     * @param color Color of the line.
     */
    public void addStaticLine(LocalTime time, String label, int thickness, Color color)
    {
        staticLines.add(new StaticLine(time, label, thickness, color));
        if (USE_BUFFERED_IMAGE) createStaticImage();
        repaint();
    }

    /**
     * Remove all lines from static lines array.
     */
    public void clearStaticLines()
    {
        staticLines.clear();
        if (USE_BUFFERED_IMAGE) createStaticImage();
        repaint();
    }

    @Override
    public void updateTime(LocalTime time)
    {
        // TODO find where the current time is added/calculated
        //if (USE_BUFFERED_IMAGE) createStaticImage(); // TODO should be removed when properly draw static image
        repaint(); // will also trigger drawCurrentTime()
    }

    @Override
    public void updateTerminatorCalculations()
    {
        calculateEqualDayNightView();
        repaint();
    }

    @Override
    public void updateEqualView()
    {
        this.calculateEqualDayNightView();
    }

    /**
     * For testing analog clock.
     * @param args params; blank
     */
    public static void main(String[] args)
    {
        // Create JFrame (main window of application)
        JFrame frame = new JFrame("Analog Clock Testing");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        AnalogClockPanel clockPanel = new AnalogClockPanel();
        clockPanel.addHourTickMarks();

        GridRegionPanel grp = new GridRegionPanel(10, 15);
        grp.addRegion(2, 1, 11, 8, clockPanel);
        //frame.add(clockPanel, BorderLayout.CENTER);
        grp.setFillEmptyRegions(true);
        grp.setDebugBorders(true);
        grp.construct();
        frame.add(grp, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
