package gui;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import com.kosherjava.zmanim.util.GeoLocation;
import util.*;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Create a Zmanim Clock.
 *
 * TODO terms used
 * solar terminator - The line between day and night.  In this program, the terminators are designated as sunrise and
 * sunset.
 * tekufah - A period beginning at sunrise/sunset and ending at sunset/sunrise.  1/2 of the standard solar cycle.
 * sha'ah (zman) - 1 halachic hour; 1/12 of the day or night period.
 * TODO description
 *
 * <p>The clock sections operate as follows:  The current tekufah and the next tekufah are displayed.  However, due to
 * sunrise/sunset shifting on a daily basis, sha'ah zmanis tick marks will not be displayed precisely for the next
 * tekufah.  Rather, the sunrise/sunset times bounding the current tekufah will be displayed.  The tick marks for the
 * current tekufah will be displayed in the proper position, but the ones for the next tekufah will be displayed based
 * on the sha'ah zmanim for that tekufah.  While there may not be a visible difference, they will either overlap or
 * not meet the markers for what would be the tekufah following.  This alters each tekufah change; tick marks will be
 * repainted so the times for the current tekufah are accurate.</p>
 */
public class ZmanClockGUI extends JPanel
{
    private ComplexZmanimCalendar czc;

    private LocalTime currentTime;

    private TerminatorTimes terminatorTimes = new TerminatorTimes(); // to hold (assuming in middle of tekufah): before, after, next

    private boolean equalDayNightView = false;
    // for use to set lines at proper position in circle
    private double offsetSunrise;
    private double offsetSunset;
    // range of day and night of circle
    private double angularSpanDay;
    private double angularSpanNight;

    private final long MILLIS_PER_DAY = 86400000L;

    private final List<StaticLine> staticLines = new ArrayList<>();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Initialize a graphic interface for a Zmanim Clock.
     * @param czc <code>ComplexZmanimCalendar</code> instance; a clone will be created for use
     */
    public ZmanClockGUI(ComplexZmanimCalendar czc)
    {
        this.czc = (ComplexZmanimCalendar) czc.clone();
        calculateSolarTerminators();

        setPreferredSize(new Dimension(500, 500));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int diameter = Math.min(width, height) - 50;
        int radius = diameter / 2;
        int centerX = width / 2;
        int centerY = height / 2;

        calculateEqualDayNightView(); // calculate proper angles for sunrise and sunset

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
            drawLineForTime(g2d, centerX, centerY, radius, line.time);

            if (line.label != null)
            {
                drawLabel(g2d, centerX, centerY, radius, line.time, line.label);
            }
        }

        // Draw the dynamic current time hand
        if (currentTime != null)
        {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(1));
            drawLineForTime(g2d, centerX, centerY, radius, currentTime);
        }

        // Draw the circle outline
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawOval(centerX - radius, centerY - radius, diameter, diameter);
        g2d.setStroke(new BasicStroke(1.0f));
    }

    private void drawLineForTime(Graphics2D g2d, int centerX, int centerY, int radius, LocalTime time)
    {
        double angle = calculateAngle(time);
        int endX = (int) (centerX + radius * Math.cos(angle));
        int endY = (int) (centerY - radius * Math.sin(angle));
        g2d.drawLine(centerX, centerY, endX, endY);
    }

    // TODO make so label does not overlap other elements
    private void drawLabel(Graphics2D g2d, int centerX, int centerY, int radius, LocalTime time, String label)
    {
        double angle = calculateAngle(time);
        int labelX = (int) (centerX + (radius + 20) * Math.cos(angle));
        int labelY = (int) (centerY - (radius + 20) * Math.sin(angle));
        g2d.setColor(Color.BLACK);

        // split text into different lines
        String[] lines = label.split("\n");
        for (int i = 0; i < lines.length; i++)
            g2d.drawString(lines[i], labelX - 10, (labelY - 8) + (i * 15));
        //g2d.drawString(label, labelX - 10, labelY + 5); // Adjust for label alignment
    }

    private double calculateAngle(LocalTime time)
    {
        double angularSpan; // the span used for the tekufah in the calculation
        int targetedTekufah; // what tekufah the targeted time is during
        boolean daytime;

        if (time.equals(terminatorTimes.getTerminator(0)) || // during first span
                time.isAfter(terminatorTimes.getTerminator(0)) && time.isBefore(terminatorTimes.getTerminator(1)))
        {
            targetedTekufah = 0; // use first and second terminators
            if (terminatorTimes.getStartingTerminator().equals(Terminator.SUNRISE))
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
            if (terminatorTimes.getStartingTerminator().equals(Terminator.SUNRISE))
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

        long totalSeconds = TimeUtil.calculateMillisBetween(terminatorTimes.getTerminator(targetedTekufah),
                terminatorTimes.getTerminator(targetedTekufah + 1));
        long currentSeconds = TimeUtil.calculateMillisBetween(terminatorTimes.getTerminator(targetedTekufah),
                time);

        // get percent of angular span
        double spanToAdd = angularSpan * ((double) currentSeconds / totalSeconds);
        if (daytime)
            return offsetSunrise - spanToAdd;
        else
            return offsetSunset - spanToAdd;
    }

    public void setCurrentTime(LocalTime currentTime)
    {
        this.currentTime = currentTime;
        repaint();
    }

    /**
     * Display sunrise and sunset lines.
     */
    public void addTerminatorLines()
    {
        this.addStaticLine(terminatorTimes.getTerminator(0), terminatorTimes.getStartingTerminator().toString() + "\n" +
                terminatorTimes.getTerminator(0).format(formatter), 3, Color.GREEN);
        Terminator other = terminatorTimes.getStartingTerminator().equals(Terminator.SUNRISE) ? Terminator.SUNSET : Terminator.SUNRISE;
        this.addStaticLine(terminatorTimes.getTerminator(1), other.toString() + "\n" +
                terminatorTimes.getTerminator(1).format(formatter), 3, Color.GREEN);
    }

    /**
     * Display hour tick marks between sunrise and sunset: 1 - 11 in day; 13 - 23 in night
     */
    public void addHourTickMarks()
    {
        // calculate shaah for the time period
        // start of tekufah; offset of time due to day or night
        int[][] tekufahSettings = new int[][] {
                new int[] { 0, terminatorTimes.getStartingTerminator().equals(Terminator.SUNRISE) ? 0 : 12},
                new int[] { 1, terminatorTimes.getStartingTerminator().equals(Terminator.SUNSET) ? 0 : 12}
        };

        for (int[] tekufah : tekufahSettings)
        {
            LocalTime tekufahStart = terminatorTimes.getTerminator(tekufah[0]);
            long tekufahShaah = terminatorTimes.getTekufahShaah(tekufah[0]);

            for (int i = 1; i < 12; i++)
            {
                LocalTime tickMark = tekufahStart.plusSeconds(
                        (tekufahShaah / 1000) * i); // millis to seconds, then multiply by hours
                this.addStaticLine(tickMark,
                        String.valueOf(i + tekufah[1]) + "\n" + tickMark.format(formatter),
                        1,
                        Color.LIGHT_GRAY);
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

    // /\ Drawing /\
    // -----------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    // \/ Logic \/

    /**
     * Method to be called upon startup of the program, at solar terminator, and when parameters change.  This method
     * gets the proper times for the terminators and which zmanim should be used for between them.
     *
     * <p>The current time is placed into a tekufah.  If the time corresponds to the beginning of a tekufah, then all
     * calculations start from there.  However, if the time corresponds to the middle of a tekufah, then the terminators
     * bracketing the current time will be the first terminators used.  This will necessitate a date change of the
     * <code>ComplexZmanimCalendar</code> calendar.</p>
     */
    private void calculateSolarTerminators()
    {
        // currently uses getSunrise() for delineations; switch to higher-order based on options for delineations?

        LocalTime tempSunrise = TimeUtil.dateToLocalTime(czc.getSunrise(), czc);
        LocalTime tempSunset = TimeUtil.dateToLocalTime(czc.getSunset(), czc);

        if (LocalTime.now().isBefore(tempSunrise)) // before sunrise; during previous night
        {
            // start from previous sunset
            ComplexZmanimCalendar yesterday = changeDay(this.czc, -1);
            terminatorTimes.setTerminator(0, TimeUtil.dateToLocalTime(yesterday.getSunset(), yesterday));
            terminatorTimes.setStartingTerminator(Terminator.SUNSET);

            terminatorTimes.setTerminator(1, tempSunrise);
            terminatorTimes.setTerminator(2, tempSunset);
        }
        else if (LocalTime.now().equals(tempSunrise) ||     // at sunrise
                    LocalTime.now().isBefore(tempSunset))   // after sunrise, but before sunset
        {
            terminatorTimes.setTerminator(0, tempSunrise);
            terminatorTimes.setStartingTerminator(Terminator.SUNRISE);
            terminatorTimes.setTerminator(1, tempSunset);

            // third terminator is next sunrise
            ComplexZmanimCalendar tomorrow = changeDay(this.czc, 1);
            terminatorTimes.setTerminator(2, TimeUtil.dateToLocalTime(tomorrow.getSunrise(), tomorrow));
        }
        else // LocalTime.now().equals(tempSunset) || LocalTime.now().isAfter(tempSunset); sunset or after
        {
            terminatorTimes.setTerminator(0, tempSunset);
            terminatorTimes.setStartingTerminator(Terminator.SUNSET);

            // second and third terminator times are tomorrow
            ComplexZmanimCalendar tomorrow = changeDay(this.czc, 1);
            terminatorTimes.setTerminator(1, TimeUtil.dateToLocalTime(tomorrow.getSunrise(), tomorrow));
            terminatorTimes.setTerminator(2, TimeUtil.dateToLocalTime(tomorrow.getSunset(), tomorrow));
        }

        calculateEqualDayNightView();
    }

    /**
     * Called at a terminator change, this method advances the terminators to the future times and swaps starting status.
     */
    private void updateTerminatorTimes()
    {
        // if any terminator time is null, just calculate all to be safe
        for (int i = 0; i < 3; i++)
            if (terminatorTimes.getTerminator(i) == null) // if any time is null
            {
                calculateSolarTerminators();
                return; // no point in updating since all times now set
            }

        // get next terminator time to be saved
        // if current terminator head is SUNRISE, will need next SUNSET; and vice versa
        LocalTime nextTime;
        if (terminatorTimes.getStartingTerminator().equals(Terminator.SUNRISE)) // need tomorrow's sunset
        {
            ComplexZmanimCalendar future = changeDay(this.czc, 1);
            nextTime = TimeUtil.dateToLocalTime(future.getSunset(), future);
        }
        else // starting terminator is SUNSET; need aftermorrow's sunrise
        {
            ComplexZmanimCalendar future = changeDay(this.czc, 2);
            nextTime = TimeUtil.dateToLocalTime(future.getSunrise(), future);
        }

        terminatorTimes.increment(nextTime);
        calculateEqualDayNightView();  // ---------------------- For clock only
    }

    public void setEqualDayNightView(boolean equal)  // ---------------------- For clock only
    {
        this.equalDayNightView = equal;
        calculateEqualDayNightView();
        repaint();
    }

    public boolean getEqualDayNightView()  // ---------------------- For clock only
    {
        return this.equalDayNightView;
    }

    /**
     * Sets proper values for offsets of sunrise and sunset.
     */
    private void calculateEqualDayNightView()  // ---------------------- For clock only
    {
        if (equalDayNightView)
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
            long periodTime = terminatorTimes.getTekufahSpan(0);

            double topPeriodTime = periodTime; // initilize to daytime
            if (terminatorTimes.getStartingTerminator().equals(Terminator.SUNSET))
                topPeriodTime = MILLIS_PER_DAY - periodTime; // if first period is night, get 24-hour remainder and set top

            double percentOfCircle = (topPeriodTime / MILLIS_PER_DAY) * 100;
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
     * Get a new ComplexZmanimCalendar with zmanim for a different day.
     *
     * @param czc ComplexZmanimCalendar
     * @param numDays number of days to change; positive = future, negative = past
     * @return modified clone offset by the specified number of days
     */
    private ComplexZmanimCalendar changeDay(ComplexZmanimCalendar czc, int numDays)
    {
        ComplexZmanimCalendar modified = (ComplexZmanimCalendar) czc.clone(); // create clone to avoid messing up current day

        // change day
        Calendar instance = modified.getCalendar();
        instance.add(Calendar.DAY_OF_YEAR, numDays);
        modified.setCalendar(instance);

        return modified;
    }

    public void simulateTimeProgression(int delay)
    {
        new Timer(delay, e ->
        {
            if (currentTime != null)
            {
                currentTime = currentTime.plusMinutes(1);
                repaint();
            }
        }).start();
    }

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Zman Clock GUI");

        // set up clocks
        GeoData location = Regions.getLocation("Pikesville");

        ZmanClockGUI clock = new ZmanClockGUI(new ComplexZmanimCalendar(
                new GeoLocation(
                        location.getName(), location.getLatitude(), location.getLongitude(),
                        TimeZone.getTimeZone(location.getRegion()))));


        //clock.setCurrentTime(LocalTime.now());
        clock.setEqualDayNightView(false);

        clock.addHourTickMarks();
        clock.addTerminatorLines();

        //clock.simulateTimeProgression(10);

        frame.add(clock);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
