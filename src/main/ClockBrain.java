package main;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import com.kosherjava.zmanim.util.GeoLocation;
import interfaces.TerminatorObserver;
import interfaces.TimeObserver;
import interfaces.EqualViewOption;
import util.*;
import util.enums.Terminator;

import javax.swing.*;
import javax.swing.Timer;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Singleton class for holding the logic of the clock - the ComplexZmanimCalendar, current time, and time methods that
 * are not specialized to a single use.  Allows observers using the <code>ClockObserver</code> interface.
 */
public final class ClockBrain
{
    private static ClockBrain INSTANCE;

    private ComplexZmanimCalendar czc;

    protected ZoneId zoneId;
    private VirtualClock virtualClock;

    protected TerminatorTimes terminatorTimes = new TerminatorTimes();
    /**
     * Lock to prevent Timer from causing updates to sections while terminator times need to be updated.
     */
    private final ReentrantLock lock = new ReentrantLock();

    protected ArrayList<EqualViewOption> updatable = new ArrayList<>();

    protected Timer timer;

    private final List<TimeObserver> timeObservers = new ArrayList<>();
    private final List<TerminatorObserver> terminatorObservers = new ArrayList<>();

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
        virtualClock = new VirtualClock(zoneId);

        calculateSolarTerminators();
        initializeTimeProgression();
        createTekufahScheduler();
        setTimeProgression(true); // TODO is this proper?
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

    /**
     * Get a copy of the <code>ComplexZmanimCalendar</code> used by the clock.
     * @return clone of current <code>ComplexZmanimCalendar</code>
     */
    public ComplexZmanimCalendar getComplexZmanimCalendar()
    {
        return (ComplexZmanimCalendar) czc.clone();
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

        LocalTime now = virtualClock.getLocalTime();

        if (now.isBefore(tempSunrise)) // before sunrise; during previous night
        {
            // start from previous sunset
            ComplexZmanimCalendar yesterday = changeDay(this.czc, -1);
            terminatorTimes.setTerminator(0, TimeUtil.dateToLocalTime(yesterday.getSunset(), yesterday));
            terminatorTimes.setStartingTerminator(Terminator.SUNSET);

            terminatorTimes.setTerminator(1, tempSunrise);
            terminatorTimes.setTerminator(2, tempSunset);
        }
        else if (now.equals(tempSunrise) ||     // at sunrise
                now.isBefore(tempSunset))   // after sunrise, but before sunset
        {
            terminatorTimes.setTerminator(0, tempSunrise);
            terminatorTimes.setStartingTerminator(Terminator.SUNRISE);
            terminatorTimes.setTerminator(1, tempSunset);

            // third terminator is next sunrise
            ComplexZmanimCalendar tomorrow = changeDay(this.czc, 1);
            terminatorTimes.setTerminator(2, TimeUtil.dateToLocalTime(tomorrow.getSunrise(), tomorrow));
        }
        else // now.equals(tempSunset) || now.isAfter(tempSunset); sunset or after
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
        // prevent timer from causing time updates
        lock.lock(); // must occur; demand the lock

        try {
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
        } finally {
            lock.unlock(); // permit timer and other elements to work on clock
        }
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

    /**
     * Method to create the thread that will update the tekufos at the proper time.
     */
    private void createTekufahScheduler()
    {
        TimeScheduler tekufahScheduler = new TimeScheduler();
        tekufahScheduler.scheduleRepeat(
                terminatorTimes.getTerminator(1),       // first time to occur
                () -> { // tasks to run at that time
                    updateTerminatorTimes();
                    notifyTerminatorObservers();
                },
                () -> terminatorTimes.getTerminator(1) // next time supplier for when to run task
        );
    }


    // Time methods --------------------------------------------------------------------------

    /**
     * Get the current <code>LocalTime</code> time of the clock.
     * @return
     */
    public LocalTime getCurrentTime()
    {
        return virtualClock.getLocalTime(); // since LocalTime is immutable
    }

    public ZonedDateTime getCurrentDateTime()
    {
        return virtualClock.now();
    }

    // Timer methods ---------------------------------------------------------------------------

    /**
     * If clock time should move; if not, clock will remain on one time.
     * @param progress
     */
    public void setTimeProgression(boolean progress)
    {
        if (progress)
        {
            virtualClock.resume();
            timer.start();
        }
        else
        {
            virtualClock.pause();
            timer.stop();
        }
    }

    /**
     * If clock time should move; if not, clock will remain on one time.
     * @return
     */
    public boolean getTimeProgression()
    {
        return !virtualClock.isPaused();
    }

    /**
     * Set starting time for clock and initialize timer.
     */
    private void initializeTimeProgression()
    {
        // set clock time
        if (DebugTimeModifications.TIME_OFFSET.enabled) // should this change for debugging?
        {
            virtualClock.offset(DebugTimeModifications.TIME_OFFSET.duration);
        }

        // timer
        int second = 1000; // milliseconds
        int timerIterationSpeed = (int) (.1 * second); // how often timer should activate
        timer = new Timer(timerIterationSpeed, e ->
        {
            // update current time
            // virtualClock automatically updates itself every time it is polled

            syncCalendarDate(); // is this necessary each tick, or every so often?

            // everything that needs to be done, should happen here
            // Attempt to acquire the lock to allow updates for the observers.  Will fail if the terminatorTimes are
            // being updated.
            if (lock.tryLock()) // acquire the lock if possible; if not, skip this notification
                try {
                    notifyTimeObservers();
                } finally {
                    lock.unlock(); // always unlock after done
                }
            // TODO how to properly repaint?

        });
    }

    private void syncCalendarDate()
    {
        Calendar cal = GregorianCalendar.from(virtualClock.now());
        czc.setCalendar(cal);
    }

    // Observer methods ----------------------------------------------------------------------

    /**
     * Add a time observer to the ClockBrain.
     * @param observer
     */
    public void registerTimeObserver(TimeObserver observer)
    {
        timeObservers.add(observer);
    }

    /**
     * Remove a time observer from the ClockBrain.
     * @param observer
     */
    public void unregisterTimeObserver(TimeObserver observer)
    {
        timeObservers.remove(observer);
    }

    /**
     * Notify time observers.
     */
    private void notifyTimeObservers()
    {
        LocalTime currentTime = getCurrentTime(); // so not requesting it multiple times
        for (TimeObserver observer : timeObservers)
            observer.updateTime(currentTime);
    }

    /**
     * Add a terminator observer to the ClockBrain.
     * @param observer
     */
    public void registerTerminatorObserver(TerminatorObserver observer)
    {
        terminatorObservers.add(observer);
    }

    /**
     * Remove a terminator observer from the ClockBrain.
     * @param observer
     */
    public void unregisterTerminatorObserver(TerminatorObserver observer)
    {
        terminatorObservers.remove(observer);
    }

    /**
     * Notify terminator observers.
     */
    private void notifyTerminatorObservers()
    {
        for (TerminatorObserver observer : terminatorObservers)
            observer.updateTerminatorCalculations();
    }

    // ---------------------------------------------------------------------------------------

    public static void main(String[] args)
    {
        ClockBrain clock = ClockBrain.getInstance();

        System.out.println(clock.getCurrentTime());
    }
}
