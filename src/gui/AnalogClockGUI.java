package gui;

import interfaces.TerminatorObserver;
import interfaces.TimeObserver;
import interfaces.EqualViewOption;
import main.ClockBrain;
import util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AnalogClockGUI extends JPanel implements TimeObserver, TerminatorObserver, EqualViewOption
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

    private final List<StaticLine> staticLines = new ArrayList<>();

    /**
     * The <code>Graphics2D</code> object used for painting on the panel.
     */
    private Graphics2D g2d;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public AnalogClockGUI()
    {
        clock = ClockBrain.getInstance();

        // calculate proper angles for sunrise and sunset; recalculated when terminators changed / ViewMode changed
        calculateEqualDayNightView();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawClock();
    }

    private void drawClock()
    {
        int width = getWidth();
        int height = getHeight();
        int diameter = Math.min(width, height) - 50;
        int radius = diameter / 2;
        int centerX = width / 2;
        int centerY = height / 2;

        // Draw colored sections of circle
        Color dayColor = new Color(255, 255, 200); // light yellow
        Color nightColor = new Color(100, 100, 255); // light deep blue

        // Draw the top half of the circle
        Arc2D.Double dayArc = new Arc2D.Double(centerX - radius, centerY - radius, diameter, diameter,
                Math.toDegrees(offsetSunset),
                Math.toDegrees((offsetSunrise - offsetSunset + (2 * Math.PI)) % (2 * Math.PI)),
                Arc2D.PIE); // pie slice (filled); more accurate than fillArc()
        g2d.setColor(dayColor); // Light yellow
        g2d.fill(dayArc);

        // Draw the bottom half of the circle
        Arc2D.Double nightArc = new Arc2D.Double(centerX - radius, centerY - radius, diameter, diameter,
                Math.toDegrees(offsetSunrise),
                360 - Math.toDegrees((offsetSunrise - offsetSunset + (2 * Math.PI)) % (2 * Math.PI)),
                Arc2D.PIE); // pie slice (filled); more accurate than fillArc()
        g2d.setColor(nightColor); // Light deep blue
        g2d.fill(nightArc);

        // Draw static lines with optional labels
        for (StaticLine line : staticLines)
        {
            g2d.setColor(line.color);
            g2d.setStroke(new BasicStroke(line.thickness));
            drawLineAtTime(centerX, centerY, radius, line.time);

            if (line.label != null)
            {
                drawLabel(centerX, centerY, radius, line.time, line.label);
            }
        }

//        // Draw the dynamic current time hand
//        if (clock.getCurrentTime() != null)
//        {
//            g2d.setColor(Color.RED);
//            g2d.setStroke(new BasicStroke(1));
//            drawLineAtTime(centerX, centerY, radius, clock.getCurrentTime());
//        }

        // Draw the circle outline; last to place over other elements that may overlap
        drawBoundingCircle(centerX - radius, centerY - radius, diameter, diameter);
    }

    private void drawLineAtTime(int centerX, int centerY, int radius, LocalTime time)
    {
        double angle = calculateAngle(time);
        int endX = (int) (centerX + radius * Math.cos(angle));
        int endY = (int) (centerY - radius * Math.sin(angle));
        g2d.drawLine(centerX, centerY, endX, endY);
    }

    /**
     * Draw the circle outline; drawn last to place it over other elements that may overlap.
     *
     * TODO change for different viewmodes?
     */
    private void drawBoundingCircle(int upperLeftX, int upperLeftY, int width, int height)
    {
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawOval(upperLeftX, upperLeftY, width, height);
        g2d.setStroke(new BasicStroke(1.0f)); // reset stroke
    }

    // TODO make so label does not overlap other elements
    private void drawLabel(int centerX, int centerY, int radius, LocalTime time, String label)
    {
        double angle = calculateAngle(time);
        String angleText = String.format("%.2f", angle); // DEBUG
        label = label + " (" + angleText + ")"; // DEBUG
        int labelX = (int) (centerX + (radius + 20) * Math.cos(angle));
        int labelY = (int) (centerY - (radius + 20) * Math.sin(angle));
        g2d.setColor(Color.BLACK);
        g2d.drawString(label, labelX - 10, labelY + 5); // Adjust for label alignment
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

        long totalSeconds = TimeUtil.calculateMillisBetween(clock.getTerminatorTimes().getTerminator(targetedTekufah),
                clock.getTerminatorTimes().getTerminator(targetedTekufah + 1));
        long currentSeconds = TimeUtil.calculateMillisBetween(clock.getTerminatorTimes().getTerminator(targetedTekufah),
                time);

        // get percent of angular span
        double spanToAdd = angularSpan * ((double) currentSeconds / totalSeconds);
        if (daytime)
            return offsetSunrise - spanToAdd;
        else
            return offsetSunset - spanToAdd;
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

            for (int i = 1; i < 12; i++) // 0 to include terminator, 1 to exclude
            {
                LocalTime tickMark = tekufahStart.plusSeconds(
                        (tekufahShaah / 1000) * i); // millis to seconds, then multiply by hours
                this.addStaticLine(tickMark, tickMark.format(formatter), 1, Color.LIGHT_GRAY);
            }
        }
    }

    public void addStaticLine(LocalTime time, String label, int thickness, Color color)
    {
        staticLines.add(new StaticLine(time, label, thickness, color));
        repaint();
    }

    public void clearStaticLines()
    {
        staticLines.clear();
        repaint();
    }

    @Override
    public void updateTime(LocalTime time)
    {
        // TODO find where the current time is added/calculated
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

        AnalogClockGUI clockPanel = new AnalogClockGUI();
        clockPanel.addHourTickMarks();
        frame.add(clockPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
