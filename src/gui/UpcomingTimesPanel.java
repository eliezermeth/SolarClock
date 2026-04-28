package gui;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import events.ClockEvent;
import events.ClockEventManager;
import main.ClockBrain;

import javax.swing.*;
import java.util.Date;
import java.util.List;

public class UpcomingTimesPanel
{
    private ClockBrain clock;
    private ComplexZmanimCalendar czc;
    private JPanel panel;

    /**
     * Constructor.
     * @param panel
     */
    public UpcomingTimesPanel(JPanel panel)
    {
        this.clock = ClockBrain.getInstance();
        czc = clock.getComplexZmanimCalendar();
        this.panel = panel;
    }

    public static void main(String[] args)
    {
        ClockBrain clock = ClockBrain.getInstance();
        ComplexZmanimCalendar czc = clock.getComplexZmanimCalendar();
        ClockEventManager eventManager = clock.getEventManager();

        List<ClockEvent> events = eventManager.getAllEvents();
        Date date;
        for (ClockEvent event  : events)
        {
            date = Date.from(event.getTime().toInstant());

            System.out.printf("%s\n%tF %tT\n%n", event.getTitle(), date, date);
        }
        // matching from myzmanim.com; times appear to be ~10 second earlier due to location
    }

    public static void printTimeGroup(String name, Date time)
    {
        System.out.println(name);
        System.out.println(time);
        System.out.println();
    }
}
