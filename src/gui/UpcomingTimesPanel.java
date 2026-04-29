package gui;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import events.ClockEvent;
import events.ClockEventManager;
import main.ClockBrain;

import javax.swing.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

public class UpcomingTimesPanel
{
    private ClockBrain clock;
    private ComplexZmanimCalendar czc;
    private JPanel panel;

    private ClockEventManager clockEventManager;
    private ClockEvent nextUpcomingVisibleEvent;

    /**
     * Constructor.
     * @param panel
     */
    public UpcomingTimesPanel(JPanel panel)
    {
        this.clock = ClockBrain.getInstance();
        czc = clock.getComplexZmanimCalendar();
        this.panel = panel;

        clockEventManager = clock.getEventManager();
        nextUpcomingVisibleEvent = clockEventManager.getUpcomingFirstVisibleEvent();

        // construct panels
        // need method to condense ClockEvent into printable text
    }

    public static void main(String[] args)
    {
        ClockBrain clock = ClockBrain.getInstance();
        ComplexZmanimCalendar czc = clock.getComplexZmanimCalendar();
        ClockEventManager eventManager = clock.getEventManager();

        // matching from myzmanim.com; times appear to be ~10 second earlier due to location
        List<ClockEvent> events = eventManager.getUpcomingEvents();
        Date date;
        for (ClockEvent event  : events)
        {
            date = Date.from(event.getTime().toInstant());

            System.out.printf("%s\n%tF %tT\n%n", event.getTitle(), date, date);
        }

        System.out.println("\n----------\n");
        ClockEvent e = events.getFirst();
        System.out.println(e.getTitle());
        System.out.println(e.getTime().toLocalTime());

        Duration d = Duration.between(clock.getCurrentDateTime(), e.getTime());
        System.out.println(d);

        long seconds = d.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0)
            System.out.printf("%d:%02d:%02d%n", hours, minutes, secs);
        else
            System.out.printf("%02d:%02d%n", minutes, secs);

    }
}
