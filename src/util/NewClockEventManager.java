package util;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import interfaces.TimeObserver;
import main.ClockBrain;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class NewClockEventManager implements TimeObserver
{
    private final ClockBrain clock = ClockBrain.getInstance();

    private final List<ClockEvent> allEvents = new ArrayList<>();
    private final NavigableSet<ClockEvent> upcoming = new TreeSet<>();

    public NewClockEventManager()
    {
        // initialize
        initialize();

        // advance upcoming to proper event

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

    @Override
    public void updateTime(ZonedDateTime time)
    {

    }
}
