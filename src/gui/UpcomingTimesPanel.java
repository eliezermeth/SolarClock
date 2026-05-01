package gui;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import events.ClockEvent;
import events.ClockEventManager;
import interfaces.TimeObserver;
import main.ClockBrain;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class UpcomingTimesPanel implements TimeObserver
{
    private ClockBrain clock;
    private ComplexZmanimCalendar czc;
    private JPanel panel;

    private ClockEventManager clockEventManager;
    private ClockEvent nextUpcomingVisibleEvent;

    private JPanel imminentEvent;
    private JLabel imminentTitle, imminentTime, imminentCountdown;

    private List<JPanel> upcomingList;

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
        this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.Y_AXIS));
        this.panel.setOpaque(false);
        constructImminentPanel();

        updateImminentPanelEvent(clockEventManager.getUpcomingFirstVisibleEvent());
        updateImminentPanelCountdown(clock.getCurrentDateTime());

        panel.add(Box.createVerticalGlue()); // stick elements to top
        // need method to condense ClockEvent into printable text

        // register as observer for relevant
        clock.registerTimeObserver(this);
    }

    private void constructImminentPanel()
    {
        imminentEvent = new JPanel(new GridBagLayout());
        imminentEvent.setOpaque(false);
        imminentEvent.setMaximumSize(new Dimension(Integer.MAX_VALUE, imminentEvent.getPreferredSize().height));
        imminentEvent.setAlignmentX(Component.CENTER_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // padding

        // Label
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        imminentTitle = new JLabel("Imminent zman");
        imminentTitle.setHorizontalAlignment(JLabel.CENTER);
        imminentEvent.add(imminentTitle, gbc);

        // Time
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.gridwidth = 1;
        imminentTime = new JLabel("--:--:--");
        imminentEvent.add(imminentTime, gbc);

        // Countdown
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.gridwidth = 1;
        imminentCountdown = new JLabel("--:--");
        imminentEvent.add(imminentCountdown, gbc);

        panel.add(imminentEvent);
    }

    private void updateImminentPanelEvent(ClockEvent event)
    {
        // if
        nextUpcomingVisibleEvent = event;

        imminentTitle.setText(nextUpcomingVisibleEvent.getTitle());
        // remove trailing nanoseconds
        imminentTime.setText(nextUpcomingVisibleEvent.getTime().toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString());

        // cause countdown to recalculate
        updateImminentPanelCountdown(clock.getCurrentDateTime());
    }

    private void updateImminentPanelCountdown(ZonedDateTime now)
    {
        Duration countdown = Duration.between(now, nextUpcomingVisibleEvent.getTime());
        long seconds = countdown.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0)
            imminentCountdown.setText(String.format("%d:%02d:%02d%n", hours, minutes, secs));
        else
            imminentCountdown.setText(String.format("%02d:%02d%n", minutes, secs));
    }

    @Override
    public void updateTime(ZonedDateTime time)
    {
        updateImminentPanelCountdown(time);
    }

    // special panel for imminent
    // regular queue for others

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

        // Create JFrame (main window of application)
        JFrame frame = new JFrame("Test Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(200, 600);
        frame.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        new UpcomingTimesPanel(panel);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);

    }
}
