package main;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import com.kosherjava.zmanim.util.GeoLocation;
import interfaces.EqualViewOption;
import util.*;

import javax.swing.*;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Singleton class for holding the logic of the clock - the ComplexZmanimCalendar, current time, and time methods that
 * are not specialized to a single use.
 */
public final class ClockBrain
{
    private static ClockBrain INSTANCE;

    private ComplexZmanimCalendar czc;

    protected ZoneId zoneId;
    protected LocalTime currentTime;

    protected TerminatorTimes terminatorTimes = new TerminatorTimes();

    protected final long MILLIS_PER_DAY = 86400000L;

    protected ArrayList<EqualViewOption> updatable = new ArrayList<>();

    protected boolean timeProgression = false; // if time is to move
    protected Timer timer;

    private ClockBrain()
    {
        // set up clock
        GeoData location = Regions.getLocation(Settings.location);
        this.czc = new ComplexZmanimCalendar(new GeoLocation(
                location.getName(), location.getLatitude(), location.getLongitude(),
                TimeZone.getTimeZone(location.getRegion())
        ));

        // get and intitialize time
        zoneId = this.czc.getGeoLocation().getTimeZone().toZoneId();
        currentTime = LocalTime.now(zoneId);

        calculateSolarTerminators();
        initializeTimeProgression();
    }

    public synchronized static ClockBrain getInstance() // synchronized for thread-safe when/if implemented
    {
        if (INSTANCE == null)
            INSTANCE = new ClockBrain();

        return INSTANCE;
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

    // Terminator methods -----------------------------------------------------------------

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

    /**
     * Get the terminator times.  Contains the past terminator and two upcoming terminators. If the current time is in
     * the middle of a day tekufah, the order will be (1) past sunrise, (2) upcoming sunset, (3) upcoming sunrise.
     * Should advance upon reaching the first upcoming tekufah, with (2) becoming (1), (3) becoming (2), and the new (3)
     * being calculated.
     * @return
     */
    public TerminatorTimes getTerminatorTimes()
    {
        return terminatorTimes;
    }


    // Time methods --------------------------------------------------------------------------

    /**
     * Set the <code>LocalTime</code> for the clock.
     * @param time
     */
    public void setCurrentTime(LocalTime time)
    {
        this.currentTime = time;
    }

    /**
     * Get the current time of the clock.
     * @return
     */
    public LocalTime getCurrentTime()
    {
        return currentTime; // since LocalTime is immutable
    }

    // Timer methods ---------------------------------------------------------------------------

    /**
     * If clock time should move; if not, clock will remain on one time.
     * @param progress
     */
    public void setTimeProgression(boolean progress)
    {
        timeProgression = progress;

        if (timeProgression)
            timer.start();
        else
            timer.stop();
    }

    /**
     * If clock time should move; if not, clock will remain on one time.
     * @return
     */
    public boolean getTimeProgression()
    {
        return timeProgression;
    }

    /**
     * Set starting time for clock and initialize timer.
     */
    private void initializeTimeProgression()
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
        timer = new Timer(timerIterationSpeed, e ->
        {
            // update current time
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

            // TODO everything that needs to be done should happen here

        });
    }
}
