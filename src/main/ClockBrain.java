package main;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import com.kosherjava.zmanim.util.GeoLocation;
import events.ClockEvent;
import events.ClockEventManager;
import interfaces.QuarterDayObserver;
import interfaces.TimeObserver;
import interfaces.ZmanEventObserver;
import util.*;
import util.enums.QuarterDayMark;
import util.enums.Zman;

import javax.swing.Timer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Singleton class for holding the logic of the clock - the ComplexZmanimCalendar, current time, and time methods that
 * are not specialized to a single use.  Allows observers using the {@code ClockObserver} interface.
 */
public final class ClockBrain implements ZmanEventObserver
{
    private static ClockBrain INSTANCE;

    private final ComplexZmanimCalendar czc;

    public final ZoneId zoneId;
    private final VirtualClock virtualClock;
    private final ClockEventManager eventManager;

    private SolarTimes solarTimes;
    /**
     * Lock to prevent Timer from causing updates to sections while terminator times need to be updated.
     */
    private final ReentrantLock lock = new ReentrantLock();

    private Timer timer;

    private final List<TimeObserver> timeObservers = new ArrayList<>();
    private final List<QuarterDayObserver> quarterDayObservers = new ArrayList<>();

    private ClockBrain()
    {
        // set up clock
        GeoData location = Regions.getLocation(Settings.location);
        this.czc = new ComplexZmanimCalendar(new GeoLocation(
                location.getName(), location.getLatitude(), location.getLongitude(),
                TimeZone.getTimeZone(location.getRegion())
        ));

        // get and initialize time
        zoneId = this.czc.getGeoLocation().getTimeZone().toZoneId();
        virtualClock = new VirtualClock(zoneId);
        solarTimes = new SolarTimes(getComplexZmanimCalendar());
        initializeTimeProgression();

        // start event manager
        eventManager = new ClockEventManager(this);

        // craft and initialize solar times
        solarTimes = new SolarTimes(getComplexZmanimCalendar());
        solarTimes.setDate(eventManager.getFirst(eventManager.getAllEvents(), Zman.SOLAR_MIDNIGHT));

        eventManager.registerZmanEventObserver(this);

        setTimeProgression(true); // TODO is this proper?
    }

    public synchronized static ClockBrain getInstance() // synchronized for thread safety when/if implemented
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
     * Get a copy of the {@code ComplexZmanimCalendar} used by the clock.
     * @return clone of current {@code ComplexZmanimCalendar}
     */
    public ComplexZmanimCalendar getComplexZmanimCalendar()
    {
        return (ComplexZmanimCalendar) czc.clone();
    }

    /**
     * Get the event manager (containing all {@code ClocEvent}s) of the {@code ClockBrain}.
     * @return instance of {@code ClockEventManager} created by the {@code ClockBrain}
     */
    public ClockEventManager getEventManager()
    {
        return eventManager; // should this be masked?
    }

    /**
     * Get the class holding the current solar times for the day.
     * @return {@link SolarTimes}
     */
    public SolarTimes getSolarTimes()
    {
        return solarTimes;
    }


    // Time methods --------------------------------------------------------------------------

    /**
     * Get the current {@code LocalTimeLocalTime} time of the clock.
     * @return {@code LocalTime} of clock
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
     * @param progress {@code true} if clock should tick
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
     * If clock time moves; if not, clock remains on one time.
     * @return if clock time is progressing
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
        // timer
        int timerIterationSpeed = (int) (Constant.MILLIS_PER_SECOND / Settings.clockUpdatesPerSecond); // how often timer activates
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
     * @param observer instance of {@code TimeObserver}
     */
    public void registerTimeObserver(TimeObserver observer)
    {
        timeObservers.add(observer);
    }

    /**
     * Remove a time observer from the ClockBrain.
     * @param observer instance of {@code TimeObserver}
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
        for (TimeObserver observer : timeObservers)
            observer.updateTime(getCurrentDateTime());
    }

    public void registerQuarterDayObserver(QuarterDayObserver observer)
    {
        quarterDayObservers.add(observer);
    }

    public void unregisterQuarterDayObserver(QuarterDayObserver observer)
    {
        quarterDayObservers.remove(observer);
    }

    private void notifyQuarterDayObservers(Zman zman)
    {
        // only called if valid quarter day
        QuarterDayMark justOccurred = QuarterDayMark.MIDNIGHT; // necessary initialization

        switch (zman)
        {
            case SOLAR_MIDNIGHT -> justOccurred = QuarterDayMark.MIDNIGHT;
            case SUNRISE -> justOccurred = QuarterDayMark.SUNRISE;
            case MIDDAY -> justOccurred = QuarterDayMark.MIDDAY;
            case SUNSET -> justOccurred = QuarterDayMark.SUNSET;
        }

        for (QuarterDayObserver observer : quarterDayObservers)
        {
            observer.updateQuarterDay(justOccurred);
        }
    }

    // as observer -------------------------------------

    @Override
    public void updateZmanEvent(ClockEvent event)
    {
        // At a new zman, attempt to set the new solar times for the day.
        // Should this only be done at certain zmanim for efficiency?
        try {
            solarTimes.setDate(event);
        } catch (IllegalArgumentException e)
        {
            // do nothing; event was of the incorrect type to trigger a rewrite of times
        }

        // if quarter-day event, trigger
        // SHOULD THIS ALSO ENCOMPASS SOLARTIMES?
        List<ClockEvent> all = eventManager.getAllEvents();
        int next = all.indexOf(event);
        Zman prev = all.get(next - 1).getZman();
        // TODO tidy up
        if (prev == Zman.SOLAR_MIDNIGHT || prev == Zman.SUNRISE || prev == Zman.MIDDAY || prev == Zman.SUNSET)
        {
            notifyQuarterDayObservers(prev);
        }
    }

    // ---------------------------------------------------------------------------------------

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        ClockBrain clock = ClockBrain.getInstance();

        String methodName = "getSunrise";
        Method m = clock.czc.getClass().getMethod(methodName);
        System.out.println(m.invoke(clock.czc));
        System.out.println(m.invoke(clock.czc).getClass());

        System.out.println(clock.getCurrentTime());
    }
}
