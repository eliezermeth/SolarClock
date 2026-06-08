package gui;

import events.ClockEventManager;
import interfaces.QuarterDayObserver;
import interfaces.TimeObserver;
import interfaces.EqualViewOption;
import main.ClockBrain;
import util.*;
import util.enums.Circle;
import util.enums.QuarterDayMark;
import util.enums.SHAAH_TICK_MARK_STYLE;
import util.enums.Zman;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AnalogClockPanel extends JPanel implements TimeObserver, EqualViewOption, QuarterDayObserver
{
    private ClockBrain clock; // singleton; provider

    private ClockEventManager eventManager;
    private SolarTimes solarTimes;

    // for use to set lines at proper position in circle
    /** Radians for the position of sunrise on the clock. */
    private double offsetSunrise;
    /** Radians for the position of sunset on the clock. */
    private double offsetSunset;
    /** Radians for the position of true midday on the clock. */
    private double offsetMidday;
    /** Radians for the position of (starting) true midnight on the clock. */
    private double offsetMidnight;

    /** The distance clockwise from the true midnight angle to the sunrise angle (which contains dawn). */
    private double angularSpanDawnNight;
    /** The distance clockwise from the sunrise angle to the true midday angle. */
    private double angularSpanMorning;
    /** The distance clockwise from the true midday angle to the sunset angle. */
    private double angularSpanAfternoon;
    /** The distance clockwise from the sunset angle to the true midnight angle (which contains dusk). */
    private double angularSpanDuskNight;

    private List<SolarArcSegment> cachedSolarSegments;

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
        eventManager = clock.getEventManager();
        // TODO register to clockEventManager as listener

        solarTimes = new SolarTimes(clock.getComplexZmanimCalendar());
        solarTimes.setDate(eventManager.getFirst(eventManager.getAllEvents(), Zman.SUNRISE));
        buildSolarArcSections();
        // TODO when it needs to update

        // register with ClockBrain as an observer
        clock.registerTimeObserver(this);
        clock.registerQuarterDayObserver(this);

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
            drawLineAtTime(g2d, centerX, centerY, radius, clock.getCurrentDateTime());
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
            // draw line if thickness is greater than 0
            if (line.thickness > 0)
            {
                Graphics2D tempG2D = (Graphics2D) g2d.create(); // instance to be modified for line
                if (line.isDotted)
                    tempG2D.setStroke(line.stroke); // pull full stroke style from line
                else
                    tempG2D.setStroke(new BasicStroke(line.thickness)); // only change line thickness

                tempG2D.setColor(line.color);
                drawLineAtTime(tempG2D, centerX, centerY, radius, line.time);

                tempG2D.dispose();
            }

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
        drawSolarArcSections(g2d);

        // draw line on midnight to close gap of arcs
        g2d.setColor(Settings.NIGHT_COLOR);
        g2d.setStroke(new BasicStroke(1));
        drawLineAtTime(g2d, centerX, centerY, radius, solarTimes.getMidnight(Settings.ANALOG_MIDPOINT_MODE));
        // draw line on midday to close gap of arcs
        g2d.setColor(Settings.DAY_COLOR);
        g2d.setStroke(new BasicStroke(1));
        drawLineAtTime(g2d, centerX, centerY, radius, solarTimes.getMidday(Settings.ANALOG_MIDPOINT_MODE));

        // Draw static lines with optional labels
        for (StaticLine line : staticLines)
        {
            // draw line if thickness is greater than 0
            if (line.thickness > 0)
            {
                Graphics2D tempG2D = (Graphics2D) g2d.create(); // instance to be modified for line
                if (line.isDotted)
                    tempG2D.setStroke(line.stroke); // pull full stroke style from line
                else
                    tempG2D.setStroke(new BasicStroke(line.thickness)); // only change line thickness

                tempG2D.setColor(line.color);
                drawLineAtTime(tempG2D, centerX, centerY, radius, line.time);

                tempG2D.dispose();
            }

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
            drawLineAtTime(g2d, centerX, centerY, radius, clock.getCurrentDateTime());
        }

        // Draw the circle outline; last to place over other elements that may overlap
        drawBoundingOutline(g2d, centerX - radius, centerY - radius, diameter, diameter);
    }

    /**
     * Craft the two daylight, two nighttime, and six twilight periods.
     */
    private void buildSolarArcSections()
    {
        // TODO rebuild when SolarTimes updates
        List<SolarArcSegment> list = new ArrayList<>();

        // input time elements within segments in ccw-order, but colors in correct order
        list.add(new SolarArcSegment(
                solarTimes.getTwilight(SolarTimes.Period.DAWN, SolarTimes.Twilight.ASTRONOMICAL),
                solarTimes.getMidnight(Settings.ANALOG_MIDPOINT_MODE),
                Settings.NIGHT_COLOR, Settings.NIGHT_COLOR));
        list.add(new SolarArcSegment(
                solarTimes.getTwilight(SolarTimes.Period.DAWN, SolarTimes.Twilight.NAUTICAL),
                solarTimes.getTwilight(SolarTimes.Period.DAWN, SolarTimes.Twilight.ASTRONOMICAL),
                Settings.NIGHT_COLOR, Settings.ASTRONOMICAL_TWILIGHT_COLOR));
        list.add(new SolarArcSegment(
                solarTimes.getTwilight(SolarTimes.Period.DAWN, SolarTimes.Twilight.CIVIL),
                solarTimes.getTwilight(SolarTimes.Period.DAWN, SolarTimes.Twilight.NAUTICAL),
                Settings.ASTRONOMICAL_TWILIGHT_COLOR, Settings.NAUTICAL_TWILIGHT_COLOR));
        list.add(new SolarArcSegment(
                solarTimes.getSunrise(),
                solarTimes.getTwilight(SolarTimes.Period.DAWN, SolarTimes.Twilight.CIVIL),
                Settings.NAUTICAL_TWILIGHT_COLOR, Settings.DAY_COLOR));
        list.add(new SolarArcSegment(
                solarTimes.getMidday(Settings.ANALOG_MIDPOINT_MODE),
                solarTimes.getSunrise(),
                Settings.DAY_COLOR, Settings.DAY_COLOR));
        list.add(new SolarArcSegment(
                solarTimes.getSunset(),
                solarTimes.getMidday(Settings.ANALOG_MIDPOINT_MODE),
                Settings.DAY_COLOR, Settings.DAY_COLOR));
        list.add(new SolarArcSegment(
                solarTimes.getTwilight(SolarTimes.Period.DUSK, SolarTimes.Twilight.CIVIL),
                solarTimes.getSunset(),
                Settings.DAY_COLOR, Settings.NAUTICAL_TWILIGHT_COLOR));
        list.add(new SolarArcSegment(
                solarTimes.getTwilight(SolarTimes.Period.DUSK, SolarTimes.Twilight.NAUTICAL),
                solarTimes.getTwilight(SolarTimes.Period.DUSK, SolarTimes.Twilight.CIVIL),
                Settings.NAUTICAL_TWILIGHT_COLOR, Settings.ASTRONOMICAL_TWILIGHT_COLOR));
        list.add(new SolarArcSegment(
                solarTimes.getTwilight(SolarTimes.Period.DUSK, SolarTimes.Twilight.ASTRONOMICAL),
                solarTimes.getTwilight(SolarTimes.Period.DUSK, SolarTimes.Twilight.NAUTICAL),
                Settings.ASTRONOMICAL_TWILIGHT_COLOR, Settings.NIGHT_COLOR));
        list.add(new SolarArcSegment(
                solarTimes.getNextMidnight(Settings.ANALOG_MIDPOINT_MODE),
                solarTimes.getTwilight(SolarTimes.Period.DUSK, SolarTimes.Twilight.ASTRONOMICAL),
                Settings.NIGHT_COLOR, Settings.NIGHT_COLOR));

        cachedSolarSegments = list;
    }


    private void drawSolarArcSections(Graphics2D g2d)
    {
        ConicalGradientArc gradientArc = new ConicalGradientArc();
        gradientArc.setArc2DDoubleDimensions(centerX - radius, centerY - radius, diameter, diameter);
        gradientArc.setClockwiseDraw(true);

        for (SolarArcSegment s : cachedSolarSegments)
        {
            // calculate proper distance between start and end
            double start = Math.toDegrees(calculateAngle(s.start()));
            double end = Math.toDegrees(calculateAngle(s.end()));
            int diff = (int) ((360 + end - start) % 360); // may wrap around circle

            // draw arc
            gradientArc.drawGradientArc(g2d, s.startingColor(), s.endingColor(), start, diff, -1);
            // draw line between arcs to cover up gaps
            //g2d.setColor(s.startingColor());
            //drawLineAtTime(g2d, centerX, centerY, diameter / 2, s.end());
            // TODO paint the small gaps in between the different arcs
        }
    }

    private void drawLineAtTime(Graphics2D g2d, int centerX, int centerY, int radius, ZonedDateTime time)
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

    private void drawLabel(Graphics2D g2d, int centerX, int centerY, int radius, ZonedDateTime time, String label)
    {
        // short-circuit if no text
        if (label.isEmpty()) return;

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

    /**
     * Calculates the angular position (in radians) on the clock face for a given {@link ZonedDateTime}.
     * <p>
     * The clock is divided into four time-based quadrants:
     * <ul>
     *     <li>Midnight -> Sunrise (dawn/night)</li>
     *     <li>Sunrise -> Midday (morning)</li>
     *     <li>Midday -> Sunset (afternoon)</li>
     *     <li>Sunset -> Next Midnight (dusk/night)</li>
     * </ul>
     *
     * Each quadrant is mapped proportionally to a corresponding angular span on the circular clock face.  The returned
     * angle is expressed in radians and is measured clockwise from the configured offset for that quadrant.
     * <p>
     * Internally, the method:
     * <ol>
     *     <li>Determines which quadrant the supplied time belongs to.</li>
     *     <li>Calculates the elapsed time within that quadrant.</li>
     *     <li>Maps the elapsed percentage of the time span to the corresponding angular span.</li>
     *     <li>Returns the final clockwise angle in radians.</li>
     * </ol>
     *
     * The valid range for {@code time} is <pre>[midnight, nextMidnight]</pre> where {@code midnight} is the starting
     * midnight for the current solar cycle.
     *
     * @param time the {@link ZonedDateTime} to convert into a clock-face angle
     * @return the angle in radians representing the position of the given time on the analog clock
     *
     * @throws IllegalArgumentException if the supplied time occurs before the current cycle's midnight or after the
     * next midnight
     */
    private double calculateAngle(ZonedDateTime time)
    {
        double offset; // the starting offset for the quadrant span
        double angularSpan; // span used for the quadrant in the calculation
        ZonedDateTime startTimeDemarcation, endTimeDemarcation;

        if (time.isBefore(solarTimes.getMidnight(Settings.ANALOG_MIDPOINT_MODE)))
            throw new IllegalArgumentException("Element time is too early:\n" +
                    time + " is before " + solarTimes.getMidnight(Settings.ANALOG_MIDPOINT_MODE));
        else if (time.isBefore(solarTimes.getSunrise())) // quadrant 3; earliest section
        {
            offset = offsetMidnight;
            angularSpan = angularSpanDawnNight;
            startTimeDemarcation = solarTimes.getMidnight(Settings.ANALOG_MIDPOINT_MODE);
            endTimeDemarcation = solarTimes.getSunrise();
        }
        else if (time.isBefore(solarTimes.getMidday(Settings.ANALOG_MIDPOINT_MODE))) // quadrant 2; first day section
        {
            offset = offsetSunrise;
            angularSpan = angularSpanMorning;
            startTimeDemarcation = solarTimes.getSunrise();
            endTimeDemarcation = solarTimes.getMidday(Settings.ANALOG_MIDPOINT_MODE);
        }
        else if (time.isBefore(solarTimes.getSunset())) // quadrant 1; second day section
        {
            offset = offsetMidday;
            angularSpan = angularSpanAfternoon;
            startTimeDemarcation = solarTimes.getMidday(Settings.ANALOG_MIDPOINT_MODE);
            endTimeDemarcation = solarTimes.getSunset();
        }
        else if (time.isBefore(solarTimes.getNextMidnight(Settings.ANALOG_MIDPOINT_MODE)) ||
                time.isEqual(solarTimes.getNextMidnight(Settings.ANALOG_MIDPOINT_MODE))) // quadrant 4; latest section
        {
            offset = offsetSunset;
            angularSpan = angularSpanDuskNight;
            startTimeDemarcation = solarTimes.getSunset();
            endTimeDemarcation = solarTimes.getNextMidnight(Settings.ANALOG_MIDPOINT_MODE);
        }
        else // time is past permissible
            throw new IllegalArgumentException("Element time is too late:\n" +
                    time + " is after " + solarTimes.getNextMidnight(Settings.ANALOG_MIDPOINT_MODE));

        long totalMillis = Duration.between(startTimeDemarcation, endTimeDemarcation).toMillis();
        long elapsedSeconds = Duration.between(startTimeDemarcation, time).toMillis();

        // get percent of angular span
        double spanToAdd = angularSpan * ((double) elapsedSeconds / totalMillis);
        return offset - spanToAdd; // subtract to have clockwise motion
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
            at the top, and night corresponding.  It will recalculate at true midnight, and all calculations must happen
            as an offset of these times.  It will repaint after it has recalculated positions.
             */
            long dayPeriodTime = Duration.between(solarTimes.getSunrise(), solarTimes.getSunset()).toMillis();
            double percentOfCircle = ((double) dayPeriodTime / Constant.MILLIS_PER_DAY) * 100;
            double newHalfDaySegment = percentOfCircle / 2;
            double newRadianOnePercent = (2 * Math.PI) / 100;
            double sunriseAngle = Circle.TOP.radians + (newHalfDaySegment * newRadianOnePercent);
            double sunsetAngle = ((Circle.TOP.radians - (newHalfDaySegment * newRadianOnePercent)) +
                    Circle.RIGHT.radians) % (2 * Math.PI); // force it to be a positive number

            offsetSunrise = sunriseAngle;
            offsetSunset = sunsetAngle;
        }

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // This will need to be changed when astronomical vs. time-bound features are implemented.
        // At this point, it is set to astronomical.
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        offsetMidday = Circle.TOP.radians;
        offsetMidnight = Circle.BOTTOM.radians;

        // calculate the distance clockwise from for each of the four sections
        angularSpanDawnNight = (offsetSunrise - offsetMidnight + (2 * Math.PI)) % (2 * Math.PI);
        angularSpanMorning = (offsetMidday - offsetSunrise + (2 * Math.PI)) % (2 * Math.PI);
        angularSpanAfternoon = (offsetSunset - offsetMidday + (2 * Math.PI)) % (2 * Math.PI);
        angularSpanDuskNight = (offsetMidnight - offsetSunset + (2 * Math.PI)) % (2 * Math.PI);

        angularSpanDawnNight = smallestAngularSpan(offsetMidnight, offsetSunrise);
        angularSpanMorning = smallestAngularSpan(offsetSunrise, offsetMidday);
        angularSpanAfternoon = smallestAngularSpan(offsetMidday, offsetSunset);
        angularSpanDuskNight = smallestAngularSpan(offsetSunset, offsetMidnight);

        if (USE_BUFFERED_IMAGE)
            createStaticImage();
    }

    private double smallestAngularSpan(double a, double b)
    {
        double diff = Math.abs(a - b) % (2 * Math.PI);
        return Math.min(diff, (2 * Math.PI) - diff);
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
        this.addStaticLine(solarTimes.getMidnight(Settings.ANALOG_MIDPOINT_MODE), "Midnight", 2, Color.GREEN);
        this.addStaticLine(solarTimes.getSunrise(), "Sunrise", 2, Color.GREEN);
        this.addStaticLine(solarTimes.getMidday(Settings.ANALOG_MIDPOINT_MODE), "Midday", 2, Color.GREEN);
        this.addStaticLine(solarTimes.getSunset(), "Sunset", 2, Color.GREEN);
    }

    /**
     * Display hour tick marks between sunrise and sunset.
     */
    public void addHourTickMarks()
    {
        // if tick marks should not be shown, don't do calculations
        if (!Settings.ANALOG_SHAAH_TICK_MARKS_ENABLED)
            return;

        if (Settings.ANALOG_SHAAH_TICK_MARK_STYLE == SHAAH_TICK_MARK_STYLE.ONE_TWELFTH_OF_TEKUFAH)
        {
            // calculate sha'ah for time period; will be based on what has been determined as default for midday/night
            int [] hoursOffset = new int[] { 6, 0, 6, 0}; // start from midnight
            ZonedDateTime[] demarcations = new ZonedDateTime[]
                    { solarTimes.getMidnight(Settings.ANALOG_MIDPOINT_MODE), solarTimes.getSunrise(),
                            solarTimes.getMidday(Settings.ANALOG_MIDPOINT_MODE), solarTimes.getSunset(),
                            solarTimes.getNextMidnight(Settings.ANALOG_MIDPOINT_MODE) }; // include next midnight to give spans

            for (int period = 0; period < hoursOffset.length; period++)
            {
                ZonedDateTime startTime = demarcations[period];
                long spanMillis = Duration.between(demarcations[period], demarcations[period + 1]).toMillis();
                long shaahLength = spanMillis / 6; // six sha'os per quadrant

                for (int hour = 0; hour < 6; hour++) // six sha'os per quadrant
                {
                    // 1. Time
                    ZonedDateTime tickMark = startTime.plusSeconds(
                            (shaahLength * hour) / Constant.MILLIS_PER_SECOND); // multiply by hours, then millis to seconds
                    // 2. Text
                    String text = (Settings.ANALOG_SHAAH_TIME_MARKINGS) ? tickMark.format(formatter) : "";
                    // Hour number can be gotten via hoursOffset[period] + hour

                    // add to static line array
                    StaticLine sl = this.addStaticLine(tickMark, text,
                            Settings.ANALOG_SHAAH_TICK_MARK_WIDTH,
                            Settings.ANALOG_SHAAH_TICK_MARKS_COLOR);
                    sl.setDotted(10, 10);
                }
            } // above code has not been tested for accuracy
        }
        // TODO other methods of calculations

        /*
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
        */
    }

    /**
     * Add a custom static line to the analog clock.
     * @param time <code>ZonedDateTime</code> for the position of the line.
     * @param label Text for the label; if text is blank, the text portion should not be displayed.
     * @param thickness Thickness of the line; if width is <code>0</code>, the line portion should not be displayed.
     * @param color Color of the line.
     *
     * @return The <code>StaticLine</code> that was added to the <code>ArrayList</code>.
     */
    public StaticLine addStaticLine(ZonedDateTime time, String label, int thickness, Color color)
    {
        StaticLine sl = new StaticLine(time, label, thickness, color);
        staticLines.add(sl);
        if (USE_BUFFERED_IMAGE) createStaticImage();
        repaint(); // TODO is this the proper place to repaint?  Line may be made dotted after, using return.
        return sl;
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
    public void updateTime(ZonedDateTime time)
    {
        // TODO find where the current time is added/calculated
        //if (USE_BUFFERED_IMAGE) createStaticImage(); // TODO should be removed when properly draw static image
        repaint(); // will also trigger drawCurrentTime()
    }

    @Override
    public void updateQuarterDay(QuarterDayMark mark)
    {
        if (mark != QuarterDayMark.MIDNIGHT) return; // no need to repaint if not midnight

        solarTimes = clock.getSolarTimes();
        buildSolarArcSections(); // is this proper?

        clearStaticLines();
        // addHourTickMarks();
        // TODO reset static lines, etc
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
        // clockPanel.addTerminatorLines();

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
