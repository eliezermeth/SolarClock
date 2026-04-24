package events;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import interfaces.TimeObserver;
import main.ClockBrain;
import util.ZmanOptionsConfigManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class NewClockEventManager implements TimeObserver
{
    private final ClockBrain clock = ClockBrain.getInstance();

    private final IndexedSet<ClockEvent> allEvents = new IndexedSet<>();
    private final IndexedSet<ClockEvent> upcoming = new IndexedSet<>();

    public NewClockEventManager()
    {
        // initialize
        initialize();

        // advance upcoming to proper event
        pruneBefore(upcoming, clock.getCurrentDateTime());

        // register with ClockBrain as an observer
        clock.registerTimeObserver(this);
    }

    public void initialize() // fix - split into better logic?
    {
        List<ZmanEntry> entries = ZmanOptionsConfigManager.getInstance().getEntries(); // should this be saved?
        // worry about them changing during run?

        LocalDate today = clock.getCurrentDateTime().toLocalDate();

        ZoneId zone = clock.getComplexZmanimCalendar().getCalendar().getTimeZone().toZoneId();

        // generate a 2-day window
        for (int dayOffset = 0; dayOffset < 2; dayOffset++) // for each day
        {
            // determine the day to have zmanim calculated
            LocalDate targetDate = today.plusDays(dayOffset);

            // obtain clone of ComplexZmanimCalendar to modify
            ComplexZmanimCalendar czc = (ComplexZmanimCalendar) clock.getComplexZmanimCalendar().clone();
            // force calendar to requested date
            Calendar cal = czc.getCalendar();
            cal.set(Calendar.YEAR, targetDate.getYear());
            cal.set(Calendar.MONTH, targetDate.getMonthValue() - 1);
            cal.set(Calendar.DAY_OF_MONTH, targetDate.getDayOfMonth());
            czc.setCalendar(cal);

            for (ZmanEntry entry : entries)
            {
                if (!entry.enabled()) continue; // skip disabled options

                try {
                    Date d = (Date) entry.method().invoke(czc);
                    if (d == null) continue;// if zman does not occur (that day), skip
                    ZonedDateTime time = ZonedDateTime.ofInstant(d.toInstant(), zone);

                    // add event to the schedule
                    addEvent(new ClockEvent(entry.methodName(), entry.title(), entry.description(),
                            time, entry.methodName()));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke method: " + entry.methodName(), e);
                }
            }
        }
    }

    /**
     * Add a zman event to the schedule.  Added to both all events and upcoming events.
     * @param event <code>ClockEvent</code> of time of zman
     */
    private void addEvent(ClockEvent event)
    {
        allEvents.add(event);
        upcoming.add(event);
    }

    // smaller method for going over a period to add events

    @Override
    public void updateTime(ZonedDateTime time)
    {
        // upon update, query first element in upcoming
        // if equal or greater than time, shift and send out update to all
        // past x, search for new events
        // SEE onTimeUpdate
    }

    // Regeneration hook (for when need to get new events

    /**
     * Remove all events that occurred before a specific time.
     * @param list {@code IndexedSet<ClockEvent>} to operate on
     * @param cutoff {@code ZonedDateTime} of earliest permitted time in event lists.
     */
    public void pruneBefore(IndexedSet<ClockEvent> list, ZonedDateTime cutoff)
    {
        while (true) // works since IndexedSet<ClockEvent>s are in chronological order
        {
            try {
                if (list.peek().getTime().isBefore(cutoff)) // if first element is before cutoff time, remove
                    list.poll();
                else
                    break; // once first element is after, no further elements need be checked
            } catch (NoSuchElementException e) {
                break; // do nothing; list is empty
            }
        }
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

    public static void main(String[] args)
    {
        NewClockEventManager manager = new NewClockEventManager();

        List<ClockEvent> upcoming = manager.getUpcomingEvents();

        Date date;
        for (ClockEvent event  : upcoming)
        {
            date = Date.from(event.getTime().toInstant());

            System.out.println(String.format("%s\n%tF %tT\n", event.getTitle(), date, date));
        }
    }
}
