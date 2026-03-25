package main;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import com.kosherjava.zmanim.util.GeoLocation;
import interfaces.EqualViewOption;
import util.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Central hub.
 *
 * TODO: THIS IS A TEST; IT IS NOT END CODE
 */
public class Main
{
    protected ComplexZmanimCalendar czc;

    protected ZoneId zoneId;
    protected LocalTime currentTime;
    protected boolean equalDayNightView = false;
    protected ViewMode viewMode; // not needed - visual clock type

    protected TerminatorTimes terminatorTimes = new TerminatorTimes();

    protected final long MILLIS_PER_DAY = 86400000L;

    protected ArrayList<EqualViewOption> updatable = new ArrayList<>();

    protected boolean timeProgression = false; // if time is to move
    protected Timer timeAdvancement;

    /**
     * Initialize the program
     */
    public Main(ComplexZmanimCalendar czc)
    {
        this.czc = (ComplexZmanimCalendar) czc;
        calculateSolarTerminators();

        // get and initialize time
        zoneId = this.czc.getGeoLocation().getTimeZone().toZoneId();
        currentTime = LocalTime.now(zoneId); // TODO is this correct?
    }

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

        if (LocalTime.now(zoneId).isBefore(tempSunrise)) // before sunrise; during previous night
        {
            // start from previous sunset
            ComplexZmanimCalendar yesterday = changeDay(this.czc, -1);
            terminatorTimes.setTerminator(0, TimeUtil.dateToLocalTime(yesterday.getSunset(), yesterday));
            terminatorTimes.setStartingTerminator(Terminator.SUNSET);

            terminatorTimes.setTerminator(1, tempSunrise);
            terminatorTimes.setTerminator(2, tempSunset);
        }
        else if (LocalTime.now(zoneId).equals(tempSunrise) ||     // at sunrise
                LocalTime.now(zoneId).isBefore(tempSunset))   // after sunrise, but before sunset
        {
            terminatorTimes.setTerminator(0, tempSunrise);
            terminatorTimes.setStartingTerminator(Terminator.SUNRISE);
            terminatorTimes.setTerminator(1, tempSunset);

            // third terminator is next sunrise
            ComplexZmanimCalendar tomorrow = changeDay(this.czc, 1);
            terminatorTimes.setTerminator(2, TimeUtil.dateToLocalTime(tomorrow.getSunrise(), tomorrow));
        }
        else // LocalTime.now(zoneId).equals(tempSunset) || LocalTime.now(zoneId).isAfter(tempSunset); sunset or after
        {
            terminatorTimes.setTerminator(0, tempSunset);
            terminatorTimes.setStartingTerminator(Terminator.SUNSET);

            // second and third terminator times are tomorrow
            ComplexZmanimCalendar tomorrow = changeDay(this.czc, 1);
            terminatorTimes.setTerminator(1, TimeUtil.dateToLocalTime(tomorrow.getSunrise(), tomorrow));
            terminatorTimes.setTerminator(2, TimeUtil.dateToLocalTime(tomorrow.getSunset(), tomorrow));
        }

        // updateCalculateEqualDayNighView(); TODO?
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
        // updateCalculateEqualDayNighView(); TODO?
    }

    public void simulateTimeProgression()
    {
        /*new Timer(delay, e ->
        {
            if (currentTime != null)
            {
                currentTime = currentTime.plusMinutes(1);
                // repaint(); TODO enable when GUI
            }
        }).start();*/

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

    public static void main(String[] args) throws InterruptedException, InvocationTargetException
    {
        // set up clock
        GeoData location = Regions.getLocation("Pikesville");
        Main clock = new Main(new ComplexZmanimCalendar(
                new GeoLocation(
                        location.getName(), location.getLatitude(), location.getLongitude(),
                        TimeZone.getTimeZone(location.getRegion())
                )
        ));

        // allow swing to create thread-independent clock; ref through clockRef[0]
        final DigitalClock[] clockRef = new DigitalClock[1];
        SwingUtilities.invokeAndWait(() -> {
            clockRef[0] = new DigitalClock();
        });
        DigitalClock digitalClock = clockRef[0];

        // Allow tekufah flips
        TimeScheduler tekufahScheduler = new TimeScheduler();
        // where to put
        tekufahScheduler.scheduleRepeat(
                clock.terminatorTimes.getTerminator(1),       // first time
                () -> clock.updateTerminatorTimes(),            // task
                () -> clock.terminatorTimes.getTerminator(1) // next time supplier
        );

        // set clock time
        clock.setCurrentTime(LocalTime.now());
        if (DebugTimeModifications.TIME_OFFSET.enabled)
        {
            LocalTime now = clock.getCurrentTime();
            now = now.plusHours(DebugTimeModifications.TIME_OFFSET.HOURS);
            now = now.plusMinutes(DebugTimeModifications.TIME_OFFSET.MINS);
            now = now.plusSeconds(DebugTimeModifications.TIME_OFFSET.SECS);
            clock.setCurrentTime(now);
        }

        // timer
        int second = 1000; // milliseconds
        int timerIterationSpeed = (int) (.1 * second);
        new Timer(timerIterationSpeed, e -> // every quarter second do
        {
            // set current time
            if (!DebugTimeModifications.DEBUG) // production
                clock.setCurrentTime(LocalTime.now()); // set time to now
            else // debug
            {
                if (DebugTimeModifications.TIME_ACCELERATION.enabled) // speed up the clock
                {
                    // add the specified number of seconds
                    clock.setCurrentTime(clock.getCurrentTime().plusSeconds(
                            DebugTimeModifications.TIME_ACCELERATION.SECONDS));
                }
                else // regular speed
                {
                    // advance the same speed as the timer
                    //clock.setCurrentTime(clock.getCurrentTime().plus(timerIterationSpeed, ChronoUnit.MILLIS));

                    // recalculate time to avoid potential problems
                    LocalTime now = LocalTime.now();
                    if (DebugTimeModifications.TIME_OFFSET.enabled)
                    {
                        now = now.plusHours(DebugTimeModifications.TIME_OFFSET.HOURS);
                        now = now.plusMinutes(DebugTimeModifications.TIME_OFFSET.MINS);
                        now = now.plusSeconds(DebugTimeModifications.TIME_OFFSET.SECS);
                    }
                    clock.setCurrentTime(now);
                }
            }

            // set standard time
            LocalTime ct = clock.getCurrentTime();
            String sb = String.format("%02d", ct.getHour()) +
                    ":" +
                    String.format("%02d", ct.getMinute()) +
                    ":" +
                    String.format("%02d", ct.getSecond());
            digitalClock.setStandardTime(sb);

            // set halachic time
            // TODO worry about if the time is after midnight
            // time between terminator and now
            long millisShaahSpan = clock.terminatorTimes.getTekufahShaah(0);
            long millisDifference = TimeUtil.calculateMillisBetween(clock.terminatorTimes.getTerminator(0), clock.getCurrentTime());
            long hHour = millisDifference / millisShaahSpan;
            long hRemainder = millisDifference % millisShaahSpan;
            double scaleFactor = (double) Constants.CHALAKIM_PER_SHAAH / millisShaahSpan;
            sb = String.format("%02d", hHour) + " hours " + (int) (hRemainder * scaleFactor) + "/1800 chalakim";
            digitalClock.setHalachicTime(sb);

            // TODO: Hits 60 minutes before hour flip; figure out how to change; leftover fractions of second?
            // TODO kick over terminators at swap
            // halachic time; clock format
            // create conversion factor
            double conversionFactor = 2.0 - ((60000 * 60) / (double) millisShaahSpan); // millis in standard hour; then flip over 1
            double adjustedMinuteLength = 60000 * conversionFactor; // 60,000 milliseconds in 1 minute
            double adjustedSecondLength = 1000 * conversionFactor; // 1,000 milliseconds in 1 second
            long hcMinute = (long) (hRemainder / adjustedMinuteLength);
            long hcSeconds = (long) ((hRemainder % adjustedMinuteLength) / adjustedSecondLength);
            sb = String.format("%02d", hHour) +
                    ":" +
                    String.format("%02d", hcMinute) +
                    ":" +
                    String.format("%02d", hcSeconds);
            digitalClock.setHalachicTimeStandard(sb);

            // Halachic Hour Length
            // can be calculated once per tekufah
            long spanMillis = clock.terminatorTimes.getTekufahShaah(0); // milliseconds of tekufah hour
            Duration temp = Duration.ofMillis(spanMillis);
            long HH = temp.toHours();
            long MM = temp.toMinutesPart();
            long SS = temp.toSecondsPart();
            long mm = temp.toMillisPart();
            digitalClock.setHalachicHourLength(String.format("%02d:%02d:%02d.%d", HH, MM, SS, mm));
        }).start();

        // simulateTimeProgression?
    }
}

class DigitalClock
{
    JLabel standardTime, halachicTime, halachicTimeStandard;
    JLabel halachicHourLength;

    public DigitalClock()
    {
        // Create window
        JFrame frame = new JFrame("Text Clock");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new GridBagLayout());
        // use gridbagconstrainst for easy positioning
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // padding
        // Label 1 and text field
        gbc.gridx = 0;
        gbc.gridy = 0;
        frame.add(new JLabel("Standard Time:"), gbc);
        gbc.gridx = 1;
        standardTime = new JLabel("--:--:--.----");
        frame.add(standardTime, gbc);
        // Label 2 and text field
        gbc.gridx = 0;
        gbc.gridy = 1;
        frame.add(new JLabel("Halachic Time:"), gbc);
        gbc.gridx = 1;
        halachicTime = new JLabel("-- hours --/1080 chalakim");
        frame.add(halachicTime, gbc);
        // Label 3 and text field
        gbc.gridx = 0;
        gbc.gridy = 2;
        frame.add(new JLabel("Halachic Time Standard:"), gbc);
        gbc.gridx = 1;
        halachicTimeStandard = new JLabel("--:--:--.----");
        frame.add(halachicTimeStandard, gbc);
        // Label 4 and text field
        gbc.gridx = 0;
        gbc.gridy = 3;
        frame.add(new JLabel("Halachic Hour Length:"), gbc);
        gbc.gridx = 1;
        halachicHourLength = new JLabel("--:--:--.----");
        frame.add(halachicHourLength, gbc);
        // show window
        frame.setVisible(true);
    }

    public void setStandardTime(String s) { standardTime.setText(s); }
    public void setHalachicTime(String s) { halachicTime.setText(s); }
    public void setHalachicTimeStandard(String s) { halachicTimeStandard.setText(s); }
    public void setHalachicHourLength(String s) { halachicHourLength.setText(s); }
}

// TODO: code to calculate halachic time