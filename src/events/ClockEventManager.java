package events;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import interfaces.TimeObserver;
import interfaces.ZmanEventObserver;
import main.ClockBrain;
import util.ZmanOptionsConfigManager;
import util.enums.Zman;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClockEventManager implements TimeObserver
{
    private final ClockBrain clock ;

    private final IndexedSet<ClockEvent> allEvents = new IndexedSet<>();
    private final IndexedSet<ClockEvent> upcoming = new IndexedSet<>();

    private final List<ZmanEventObserver> zmanEventObservers = new ArrayList<>();

    private final ComplexZmanimCalendar calculationCalendar; // holds day of calendar from which zman events are calculated
    private final ZoneId zone;

    private final List<ZmanEntry> entries = ZmanOptionsConfigManager.getInstance().getEntries(); // worry about changing?
    private final Map<Zman, ZmanEntry> entryMap = entries.stream().collect(Collectors.toMap(ZmanEntry:: zman, Function.identity()));

    public ClockEventManager(ClockBrain clock)
    {
        this.clock = clock;
        calculationCalendar = this.clock.getComplexZmanimCalendar(); // sets with clone
        zone = calculationCalendar.getCalendar().getTimeZone().toZoneId(); // worry about them changing during run?

        // initialize
        initialize();

        // register with ClockBrain as an observer
        clock.registerTimeObserver(this);
    }

    /**
     * Calculate proper past/future events and add to lists.
     */
    private void initialize()
    {
        List<ZmanEntry> entries = ZmanOptionsConfigManager.getInstance().getEntries(); // should this be saved?

        // Previous chatzos halailah
        mutateCalendarDate(-1); // send to yesterday
        ZonedDateTime now = clock.getCurrentDateTime();
        ZonedDateTime chatzosHalailah = calculationCalendar.getSolarMidnight().toInstant().atZone(now.getZone());

        if (now.isBefore(chatzosHalailah)) // need to go back another day
        {
            mutateCalendarDate(-1);
            chatzosHalailah = calculationCalendar.getSolarMidnight().toInstant().atZone(now.getZone());
        }
        // add chatzos halailah to event list
        ZmanEntry solarMidnight = null;
        for (int i = entries.size() - 1; solarMidnight == null && i >= 0; i--) // short circuit when found
            if (entries.get(i).zman() == Zman.SOLAR_MIDNIGHT)
                solarMidnight = entries.get(i);
        addEvent(new ClockEvent(solarMidnight.zman(),  chatzosHalailah, solarMidnight.enabled()));

        // Get zmanim for next day
        mutateCalendarDate(1); // go to next day
        ClockEvent temp;
        // iterate over zmanim
        for (ZmanEntry entry : entries)
        {
            temp = constructEvent(entry);
            if (temp != null) addEvent(temp); // if zman does not occur (that day), skip
        }

        // Determine and add (for day after) all zmanim passed this day to prime rolling events
        mutateCalendarDate(1); // go to next day
        for (int i = 1; i < allEvents.size(); i++) // start after the first chatzos halailah (?)
        {
            if (allEvents.get(i).getZman() == upcoming.peek().getZman()) // if zman is the same type as the upcoming
                break; // stop the rolling update; this will occur at some point

            // if event has already passed
            temp = constructEvent(entryMap.get(allEvents.get(i).getZman()));
            if (temp != null) addEvent(temp); // if zman does not occur (that day), skip

        }
    }

    /**
     * Mutates the {@code calculationCalendar} variable by a specified number of days.
     * @param daysChange the number of days the calendar should change; positive for future, negative for past
     */
    private void mutateCalendarDate(int daysChange)
    {
        Calendar cal = calculationCalendar.getCalendar();
        LocalDate today = cal.toInstant().atZone(cal.getTimeZone().toZoneId()).toLocalDate();
        LocalDate targetDate = today.plusDays(daysChange);

        // force calendar to requested date
        cal.set(Calendar.YEAR, targetDate.getYear());
        cal.set(Calendar.MONTH, targetDate.getMonthValue() - 1);
        cal.set(Calendar.DAY_OF_MONTH, targetDate.getDayOfMonth());
        calculationCalendar.setCalendar(cal);
    }

    /**
     * Construct a {@code ClockEvent} for the specified {@code ZmanEntry} on the day stored as
     * {@code ComplexZmanimCalendar calculationCalendar}.
     * @param entry {@code ZmanEntry} of data to gather/construct
     * @return a constructed {@code ClockEvent}; {@code null} if zman does not occur (that day)
     */
    private ClockEvent constructEvent(ZmanEntry entry)
    {
        try {
            Date d = (Date) entry.zman().getMethod().invoke(calculationCalendar);
            if (d == null) return null; // if zman does not occur (that day)
            ZonedDateTime time = ZonedDateTime.ofInstant(d.toInstant(), zone);
            return new ClockEvent(entry.zman(),  time, entry.enabled());
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + entry.zman().getMethodName(), e);
        }
    }

    /**
     * Add a zman event to the schedule.  Will only add to upcoming events if it is not before the current time.
     * @param event {@code ClockEvent} of time of zman
     */
    private void addEvent(ClockEvent event)
    {
        allEvents.add(event);

        if (!event.getTime().isBefore(clock.getCurrentDateTime())) // if event does not occur in the past
            upcoming.add(event);
    }

    @Override
    public void updateTime(ZonedDateTime time)
    {
        if (upcoming.peek().getTime().isAfter(time))
            return; // no event yet; do nothing

        // equal to or after event time
        ClockEvent next = upcoming.peek();
        next.markTriggered();

        // add newly-calculated event to end of queue
        if (next.getZman() == Zman.SOLAR_MIDNIGHT)
            mutateCalendarDate(1); // proceed to next day
        ClockEvent temp = constructEvent(entryMap.get(next.getZman())); // prep event
        if (temp != null) addEvent(temp); // if zman does not occur that day, skip
        upcoming.poll(); // remove just-passed event

        if (next.getZman() == Zman.SOLAR_MIDNIGHT) // end of day; clean allEvents list
        {
            // force-remove the first event; test elements after
            // remove until next solar midnight
            do {
                allEvents.poll();
            } while (allEvents.peek().getZman() != Zman.SOLAR_MIDNIGHT);
        }

        // should never need to happen, but just in case
        if (upcoming.isEmpty())
            initialize();

        if (next.isEnabled()) // if this one was visible, alert observers to next visible event
            notifyZmanEventObservers();
    }

    // ---------------------------------------------------------------------------------------
    // Access methods
    // ---------------------------------------------------------------------------------------

    /**
     * Return a list of all stored events (past and future).
     * @return <code>List</code> of <code>ClockEvent</code>s
     */
    public List<ClockEvent> getAllEvents()
    {
        return allEvents.asUnmodifiableList();
    }

    /**
     * Get a list of all upcoming events (future).
     * @return <code>NavigableSet</code> of <code>ClockEvent</code>s
     */
    public List<ClockEvent> getUpcomingEvents()
    {
        return upcoming.asUnmodifiableList();
    }

    /**
     * Returns a list of all of a specific type of event within the provided list.
     * @param list {@code List<ClockEvent>} to be searched
     * @param zman {@code Zman} to find
     * @return {@code List<ClockEvent>} of all {@code Zman}s in {@code list}; {@code null} if none
     */
    public List<ClockEvent> getListOfEvent(List<ClockEvent> list, Zman zman)
    {
        List<ClockEvent> temp = new LinkedList<>();
        for (ClockEvent e : list)
            if (e.getZman() == zman)
                temp.add(e);
        return !temp.isEmpty() ? temp : null;
    }

    /**
     * Returns the first of a specific type of {@code ClockEvent} within the provided list.
     * @param list {@code List<ClockEvent>} to be searched
     * @param zman {@code Zman} to find
     * @return first {@code methodName} of {@code ClockEvent} in {@code list}; {@code null} if none
     */
    public ClockEvent getFirst(List<ClockEvent> list, Zman zman)
    {
        List<ClockEvent> temp = getListOfEvent(list, zman);
        return (!temp.isEmpty()) ? temp.getFirst() : null;
    }

    /**
     * Returns the last of a specific type of {@code ClockEvent} within the provided list.
     * @param list {@code List<ClockEvent>} to be searched
     * @param zman {@code Zman} to find
     * @return last {@code methodName} of {@code ClockEvent} in {@code list}; {@code null} if none
     */
    public ClockEvent getLast(List<ClockEvent> list, Zman zman)
    {
        List<ClockEvent> temp = getListOfEvent(list, zman);
        return (!temp.isEmpty()) ? temp.getLast() : null;
    }

    /**
     * Returns the relative position of a specific type of {@code ClockEvent} within provided list.
     * @param list {@code List<ClockEvent>} to be searched
     * @param occurrenceNumber nth occurrence of {@code methodName} within {@code list}; {@code 0, 1,...} from
     *                         beginning, {@code -1, -2,...} from end (will snap to nearest event if number is out of
     *                         bounds)
     * @param zman {@code Zman} to find
     * @return nth occurrence of {@code methodName} in {@code list}; {@code null} if no events
     */
    public ClockEvent get(List<ClockEvent> list, int occurrenceNumber, Zman zman)
    {
        ArrayList<ClockEvent> temp = (ArrayList<ClockEvent>) getListOfEvent(list, zman);

        if (temp == null) return null;

        int size = temp.size();
        // leave positive; make negative to relative position
        int resolvedIndex = (occurrenceNumber >= 0) ? occurrenceNumber : size + occurrenceNumber;
        if (resolvedIndex >= temp.size()) // if after last, set to last
            resolvedIndex = temp.size() - 1;
        else if (resolvedIndex < 0) // if before first, set to first
            resolvedIndex = 0;
        return temp.get(resolvedIndex);
    }

    /**
     * Returns the first visible (enabled) {@code ClockEvent} within a list.
     * @param list the list to search for an enabled {@code ClockEvent}
     * @return the first active {@code ClockEvent}; {@code null} if none active
     */
    public ClockEvent getFirstVisibleEvent(List<ClockEvent> list)
    {
        for (ClockEvent event : list)
            if (event.isEnabled())
                return event; // short circuit on first enabled event
        return null;
    }

    /**
     * Returns the first visible (enabled) upcoming {@code ClockEvent} within a list.
     * @return the first active upcoming {@code ClockEvent}; {@code null} if none active
     */
    public ClockEvent getUpcomingFirstVisibleEvent()
    {
        return getFirstVisibleEvent(upcoming.asUnmodifiableList());
    }

    /**
     * Returns all visible (enabled) events within a list.
     * @param list list to search for visible events
     * @return list of visible events
     */
    public List<ClockEvent> getVisibleEvents(List<ClockEvent> list)
    {
        List<ClockEvent> visibleList = new LinkedList<>();

        for (ClockEvent event : list)
            if (event.isEnabled())
                visibleList.add(event);

        return visibleList;
    }

    /**
     * Returns all visible events {@code ClockEvent}s saved for current time period.
     * @return all enabled {@code ClockEvent}s
     */
    public List<ClockEvent> getVisibleAllEvents()
    {
        return getVisibleEvents(allEvents.asUnmodifiableList());
    }

    /**
     * Returns upcoming visible events {@code ClockEvent}s saved for current time period.
     * @return upcoming enabled {@code ClockEvent}s
     */
    public List<ClockEvent> getVisibleUpcomingEvents()
    {
        return getVisibleEvents(upcoming.asUnmodifiableList());
    }

    // ---------------------------------------------------------------------------------------
    // Observer methods
    // ---------------------------------------------------------------------------------------

    /**
     * Add an event observer to {@code ClockEventManager}.
     * @param observer observer class
     */
    public void registerZmanEventObserver(ZmanEventObserver observer)
    {
        zmanEventObservers.add(observer);
    }

    /**
     * Remove an event observer from {@code ClockEventManager}.
     * @param observer observer class
     */
    public void unregisterZmanEventObserver(ZmanEventObserver observer)
    {
        zmanEventObservers.remove(observer);
    }

    /**
     * Notify zman event observers that upcoming {@code ClockEvent} has changed.
     */
    public void notifyZmanEventObservers()
    {
        ClockEvent alert = getUpcomingFirstVisibleEvent();
        for (ZmanEventObserver observer : zmanEventObservers)
            observer.updateZmanEvent(alert);
    }

    public static void main(String[] args)
    {
        ClockBrain clock = ClockBrain.getInstance();
        ClockEventManager manager = clock.getEventManager();
        List<ClockEvent> events = manager.getAllEvents();

        Date date;
        for (ClockEvent event  : events)
        {
            date = Date.from(event.getTime().toInstant());

            System.out.printf("%s\n%tF %tT\n%n", event.getTitle(), date, date);
        }
    }
}
// to do
// modify so that all clock events are calculated; only some are shown

// getEventsForSolarDay()