package events;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import main.ClockBrain;
import util.Settings;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class ClockEventManager
{
    private final ClockBrain clock;

    private final List<ClockEvent> allEvents = new ArrayList<>();
    private final NavigableSet<ClockEvent> upcoming = new TreeSet<>();

    public ClockEventManager(ClockBrain clock)
    {
        this.clock = clock;
    }

    // INIT -------------------------------------------------------------------
    public void initialize(List<ZmanEntry> entries)
    {
        LocalDate today = clock.getCurrentDateTime().toLocalDate();

        // generate 2-day window
        for (int dayOffset = 0; dayOffset < 2; dayOffset++)
        {
            LocalDate targetDate = today.plusDays(dayOffset);

            for (ZmanEntry entry : entries)
            {
                if (!entry.enabled()) continue;

                ClockEvent event = buildEvent(entry, targetDate);

                if (event != null) addEvent(event);
            }
        }
    }

    // EVENT CREATION ----------------------------------------------------------------------
    private ClockEvent buildEvent(ZmanEntry entry, LocalDate date)
    {
        ZonedDateTime time = resolve(entry, date);

        if (time == null) return null;

        return new ClockEvent(entry.methodName(), entry.title(), entry.description(), time, entry.methodName());
    }

    // TIME RESOLUTION -------------------------------------------------------------------
    private ZonedDateTime resolve(ZmanEntry entry, LocalDate date)
    {
        ComplexZmanimCalendar czc = (ComplexZmanimCalendar) clock.getComplexZmanimCalendar().clone();
        ZoneId zone = czc.getCalendar().getTimeZone().toZoneId();

        // force calendar to requested date
        Calendar cal = czc.getCalendar();
        cal.set(Calendar.YEAR, date.getYear());
        cal.set(Calendar.MONTH, date.getMonthValue() - 1);
        cal.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
        czc.setCalendar(cal);

        try {
            Method method = ComplexZmanimCalendar.class.getMethod(entry.methodName());
            Date d = (Date) method.invoke(czc);
            if (d == null) return null;
            return ZonedDateTime.ofInstant(d.toInstant(), zone);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + entry.methodName(), e);
        }
    }

    // STORAGE -------------------------------------------------------------------------------
    private void addEvent(ClockEvent event)
    {
        allEvents.add(event);
        upcoming.add(event);
    }

    // MAIN UPDATE LOOP ------------------------------------------------------------------------
    // (called from Swing Timer or ClockBrain tick)
    public void onTimeUpdate(ZonedDateTime now)
    {
        while (!upcoming.isEmpty())
        {
            ClockEvent next = upcoming.first();

            if (next.getTime().isAfter(now)) break;

            upcoming.pollFirst();
            next.markTriggered();
        }

        //pruneBefore(now.toLocalDate().minusDays(Settings.EVENT_RETENTION_DAYS));
        pruneBefore(now.minusDays(Settings.EVENT_RETENTION_DAYS));
    }

    // REGENERATION HOOK (for tekufah/day change) --------------------------------------------
    public void regenerateForNewDay(List<ZmanEntry> entries)
    {
        allEvents.clear();
        upcoming.clear();
        initialize(entries);
    }

    // ACCESS ---------------------------------------------------------------------------------
    public List<ClockEvent> getAllEvents()
    {
        return Collections.unmodifiableList(allEvents);
    }

    public NavigableSet<ClockEvent> getUpcomingEvents()
    {
        return Collections.unmodifiableNavigableSet(upcoming);
    }

    // OTHER -------------------------------------------------------------------------------------
    public void pruneBefore(ZonedDateTime cutoff)
    {
        allEvents.removeIf(event -> event.getTime().isBefore(cutoff));
        upcoming.removeIf(event -> event.getTime().isBefore(cutoff));
    }
}
