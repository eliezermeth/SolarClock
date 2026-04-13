package gui;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import com.kosherjava.zmanim.util.GeoLocation;
import interfaces.EqualViewOption;
import util.*;
import util.enums.Terminator;
import util.enums.ViewMode;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * General window.  Holds information used by all.
 *
 * TODO move information from main.Main
 */
public class ZmanGUI
{
    protected ComplexZmanimCalendar czc;

    protected ZoneId zoneId;
    protected LocalTime currentTime;
    protected boolean equalDayNightView = false;
    protected ViewMode viewMode; // not needed - visual clock type?

    protected TerminatorTimes terminatorTimes = new TerminatorTimes(); // to hold (assuming middle of tekufah): before, after, next

    protected final long MILLIS_PER_DAY = 86400000L;

    protected ArrayList<EqualViewOption> updatable = new ArrayList<EqualViewOption>();

    protected boolean timeProgression = false; // if time is to move
    protected Timer timeAdvancement;

    /**
     * Initialize a graphic interface for a Zmanim Clock.
     * @param czc <code>ComplexZmanimCalendar</code> instance; a clone will be created for use
     */
    public ZmanGUI(ComplexZmanimCalendar czc)
    {
        this.czc = (ComplexZmanimCalendar) czc.clone();
        calculateSolarTerminators();

        // get and initialize time
        zoneId = this.czc.getGeoLocation().getTimeZone().toZoneId();
        currentTime = LocalTime.now(zoneId);

//        // TODO move - GUI?
//        setPreferredSize(new Dimension(700, 700));
//        setBackground(Color.WHITE);
    }

//    @Override
//    protected void paintComponent(Graphics g)
//    {
//        super.paintComponent(g);
//        Graphics2D g2d = (Graphics2D) g;
//
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//        // TODO further code
//    }

    /**
     * Method to be called upon startup of the program, at solar terminator, and when parameters change.  This method
     * gets the proper times for the terminators and which zmanim should be used for between them.
     *
     * <p>The current time is placed into a tekufah.  If the time corresponds to the beginning of a tekufah, then all
     * calculations start from there.  However, if the time corresponds to the middle of a tekufah, then the terminators
     * bracketing the current time will be the first terminators used.  This will necessitate a date change of the
     * <code>ComplexZmanimCalendar</code> calendar.</p>
     */
    protected void calculateSolarTerminators()
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

        updateCalculateEqualDayNighView();
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
        updateCalculateEqualDayNighView();
    }

    // TODO make sure it must be set from the beginning
    /**
     * Set the view mode of the analog clock.
     * @param vm enum ViewMode
     */
    public void setViewMode(ViewMode vm) // TODO examine
    {
        this.viewMode = vm;
        equalDayNightView = vm.equals(ViewMode.SUNDIAL); // TODO make better
        updateCalculateEqualDayNighView();
//        repaint();
    }

    /**
     * Get the current view mode of the analog clock.
     * @return current enum ViewMode
     */
    public ViewMode getViewMode()
    {
        return this.viewMode;
    } // TODO examine

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

    // TODO is this correct, or should it use an internal timer?
    public void setCurrentTime(LocalTime time)
    {
        this.currentTime = time;
    }

    public LocalTime getCurrentTime()
    {
        return currentTime; // since LocalTime is immutable
    }

    public TerminatorTimes getTerminatorTimes()
    {
        return terminatorTimes;
    }

    public boolean getTimeProgression()
    {
        return timeProgression;
    }

    /**
     * Toggle time progression of clock.
     */
    public void toggleTimeProgression()
    {

    }

    public void simulateTimeProgression()
    {
        // set clock time
        setCurrentTime(LocalTime.now(zoneId)); // to current, proper time (in time zone)
        if (DebugTimeModifications.TIME_OFFSET.enabled) // if should change for debugging?
        {
            LocalTime now = getCurrentTime();
            now = now.plusHours(DebugTimeModifications.TIME_OFFSET.HOURS);
            now = now.plusMinutes(DebugTimeModifications.TIME_OFFSET.MINS);
            now = now.plusSeconds(DebugTimeModifications.TIME_OFFSET.SECS);
            setCurrentTime(now);
        }

        // timer
        int second = 1000; // milliseconds
        int timerIterationSpeed = (int) (.1 * second); // how often timer should activate
        new Timer(timerIterationSpeed, e ->
        {
            // set current time
            if (!DebugTimeModifications.DEBUG) // production
                setCurrentTime(LocalTime.now(zoneId)); // set time to now
            else // debug
            {
                if (DebugTimeModifications.TIME_ACCELERATION.enabled) // speed up clock
                    // add the specified number of seconds
                    setCurrentTime(getCurrentTime().plusSeconds(DebugTimeModifications.TIME_ACCELERATION.SECONDS));
                else // regular speed
                {
                    // recalculate time to avoid potential problems
                    LocalTime now = LocalTime.now(zoneId);
                    if (DebugTimeModifications.TIME_OFFSET.enabled)
                    {
                        now = now.plusHours(DebugTimeModifications.TIME_OFFSET.HOURS);
                        now = now.plusMinutes(DebugTimeModifications.TIME_OFFSET.MINS);
                        now = now.plusSeconds(DebugTimeModifications.TIME_OFFSET.SECS);
                    }
                    setCurrentTime(now);
                }
            }

            // TODO everything that needs to be done goes here

        }).start();


    }

    // TODO delete method
    public void updateCalculateEqualDayNighView()
    {
        for (EqualViewOption o : updatable)
            o.updateEqualView();
    }

    public void addUpdatable(EqualViewOption o)
    {
        updatable.add(o);
    }

    public static void main(String[] args)
    {
        // set up clock
        GeoData location = Regions.getLocation("Pikesville");
        ZmanGUI clock = new ZmanGUI(new ComplexZmanimCalendar(
                new GeoLocation(
                        location.getName(), location.getLatitude(), location.getLongitude(),
                        TimeZone.getTimeZone(location.getRegion()))));
        //clock.setCurrentTime(LocalTime.now());
        clock.setViewMode(ViewMode.FULL_DAY);

        // Begin construction of GUI
        JFrame masterFrame = new JFrame("Zmanim Clock Layered GUI");
        masterFrame.setSize(new Dimension(800, 450));
        masterFrame.getContentPane().setBackground(Color.CYAN);


        // To manage layering of components
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setSize(new Dimension(800, 450));
        layeredPane.setBackground(Color.yellow);
        masterFrame.add(layeredPane);

//        JPanel back = new JPanel();
//        back.setBackground(Color.RED);
//        back.setSize(layeredPane.getWidth(), layeredPane.getHeight());
//        layeredPane.add(back, 0);

        // Begin basic analog GUI
        // clock.setCurrentTime(LocalTime.now());

        // Create analog clock
        AnalogClockPanel analogClockPanel = new AnalogClockPanel();
        System.out.println(masterFrame.getHeight());
        Dimension clockSize = new Dimension(masterFrame.getHeight() - 100, masterFrame.getHeight() - 100); // TODO change
        analogClockPanel.setBounds(
                (int) ((masterFrame.getWidth() / 2) - (clockSize.getWidth() / 2)),
                (int) ((masterFrame.getHeight() / 2) - (clockSize.getHeight() / 2)),
                (int) clockSize.getWidth(), (int) clockSize.getHeight());
        layeredPane.add(analogClockPanel, 0);


        masterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //masterFrame.pack();
        masterFrame.setLocationRelativeTo(null);
        masterFrame.setVisible(true);

        System.out.println(masterFrame.getSize());
    }
}
