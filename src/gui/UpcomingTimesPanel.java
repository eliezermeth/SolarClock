package gui;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import events.ClockEvent;
import events.ClockEventManager;
import interfaces.TimeObserver;
import main.ClockBrain;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UpcomingTimesPanel implements TimeObserver
{
    private ClockBrain clock;
    private ComplexZmanimCalendar czc;
    private JPanel panel;

    private ClockEventManager clockEventManager;
    private List<EventPanel> displayedList = new ArrayList<>();

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

        // construct panels
        this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.Y_AXIS));
        this.panel.setOpaque(false);

        refreshDisplayedList();
        updateDisplayedListCountdown(clock.getCurrentDateTime());

        panel.add(Box.createVerticalGlue()); // stick elements to top
        // need method to condense ClockEvent into printable text

        // register as observer for relevant
        clock.registerTimeObserver(this);

        // TODO elements do not yet disappear after time has passed
    }

    private void refreshDisplayedList()
    {
        // clear displayed list
        displayedList.clear();

        List<ClockEvent> upcoming = clockEventManager.getVisibleUpcomingEvents();

        // get max of 4
        for (int i = 0; i < upcoming.size() && i < 4; i++)
        {
            EventPanel event = new EventPanel(upcoming.get(i));
            displayedList.add(event); // add to displayed list
            panel.add(event.getPanel()); // add to panel
        }
    }

    private void updateDisplayedListCountdown(ZonedDateTime now)
    {
        for (EventPanel panel : displayedList)
            panel.updateCountdown(now);
    }

    @Override
    public void updateTime(ZonedDateTime time)
    {
        updateDisplayedListCountdown(time);
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

class EventPanel
{
    private final JPanel panel;

    private final ClockEvent event;

    private final JTextPane title;
    private final JLabel time;
    private final JLabel countdown;

    /**
     * Create a new panel for an event.  Displays the event type, time, and the countdown until the time is reached.
     * @param event {@code ClockEvent} to display
     */
    public EventPanel(ClockEvent event)
    {
        this.event = event;

        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        title = new JTextPane();
        title.setEditable(false);
        title.setFocusable(false);
        title.setOpaque(false);
        title.setBorder(null);
        StyledDocument doc = title.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        Font labelFont = UIManager.getFont("Label.font");
        StyleConstants.setFontFamily(attrs, labelFont.getFamily());
        StyleConstants.setFontSize(attrs, labelFont.getSize());
        StyleConstants.setBold(attrs, labelFont.isBold());
        StyleConstants.setForeground(attrs, UIManager.getColor("Label.foreground"));
        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), attrs, false);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(title, gbc);

        // Time
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START; // left-align within cell
        gbc.insets = new Insets(5, 25, 5, 5); // push in from left
        time = new JLabel("--:--:--");
        panel.add(time, gbc);

        // Countdown
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END; // right-align within cell
        gbc.insets = new Insets(5, 5, 5, 25); // push in from left
        countdown = new JLabel("--:--");
        panel.add(countdown, gbc);

        // set event-specific details
        setEventDetails(event);
    }

    /**
     * At startup, set the specific details for the event.  Only the title and time for the event are set here; the
     * countdown requires an update from the clock to say the current time.
     * @param event {@code ClockEvent} to be displayed
     */
    private void setEventDetails(ClockEvent event)
    {
        title.setText(event.getTitle());
        time.setText(event.getTime().toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString());
        // countdown cannot be set without an update from the clock
    }

    /**
     * Update the countdown timer for this event.
     * @param now the current time on the clock
     */
    public void updateCountdown(ZonedDateTime now)
    {
        Duration diff = Duration.between(now, event.getTime());
        long seconds = diff.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0)
            countdown.setText(String.format("%d:%02d:%02d%n", hours, minutes, secs));
        else
            countdown.setText(String.format("%1d:%02d%n", minutes, secs)); // even if less than minute; 0 shows up
    }

    /**
     * Get the {@code JPanel} used by the event.
     * @return {@code JPanel} with visible details
     */
    public JPanel getPanel()
    {
        return panel;
    }
}
