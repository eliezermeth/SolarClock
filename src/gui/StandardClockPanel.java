package gui;

import interfaces.TimeObserver;
import main.ClockBrain;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class StandardClockPanel implements TimeObserver
{
    private final ClockBrain clock;
    private final JPanel panel;

    // Moving parts
    private final JLabel[] components = new JLabel[3]; // HH MM SS
    private final JLabel dayDate = new JLabel();

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, MMM d");
    // EEE = abbreviated day; EEEE = full day of week
    // MMM = abbreviated month; MMMM = full month
    // d = day of month (no leading zero); dd = day of month (2 digits)

    public StandardClockPanel(JPanel child)
    {
        this.clock = ClockBrain.getInstance();
        this.panel = child;

        createStandardClock();

        // register with ClockBrain as an observer
        clock.registerTimeObserver(this);
    }

    private void createStandardClock()
    {
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // padding

        // Label
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel text = new JLabel("Standard Time:");
        text.setMaximumSize(text.getPreferredSize());
        panel.add(text, gbc);

        // Initialize changing components
        for (int i = 0; i < 3; i++)
        {
            components[i] = new JLabel("--");
        }

        updateStandardClock();

        // Lay out clock
        JPanel clockPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 0));
        clockPanel.add(components[0]); // hours
        clockPanel.add(new JLabel(":"));
        clockPanel.add(components[1]); // minutes
        clockPanel.add(new JLabel(":"));
        clockPanel.add(components[2]); // seconds

        // add to panel
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(clockPanel, gbc);

        // add day / date
        gbc.gridx = 0; gbc.gridy = 2;
        dayDate.setText("dayOfWeek, Month dayOfMonth");
        panel.add(dayDate, gbc);
    }

    public void updateStandardClock()
    {
        updateStandardClock(clock.getCurrentDateTime());
    }

    public void updateStandardClock(ZonedDateTime time)
    {
        components[0].setText(String.format("%02d", time.getHour()));
        components[1].setText(String.format("%02d", time.getMinute()));
        components[2].setText(String.format("%02d", time.getSecond()));

        dayDate.setText(time.format(formatter)); // TODO make more efficient; don't need to refresh each time
    }

    @Override
    public void updateTime(ZonedDateTime time)
    {
        updateStandardClock(time);
    }
}
