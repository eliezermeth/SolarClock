package gui;

import interfaces.TimeObserver;
import main.ClockBrain;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class StandardClockPanel implements TimeObserver
{
    private ClockBrain clock;
    private DigitalClockPanel parent;
    private JPanel panel;

    // Moving parts
    private JLabel[] components = new JLabel[3]; // HH MM SS

    public StandardClockPanel(DigitalClockPanel parent, JPanel child)
    {
        this.clock = ClockBrain.getInstance();
        this.parent = parent;
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
    }

    public void updateStandardClock()
    {
        updateStandardClock(clock.getCurrentTime());
    }

    public void updateStandardClock(LocalTime time)
    {
        components[0].setText(String.format("%02d", time.getHour()));
        components[1].setText(String.format("%02d", time.getMinute()));
        components[2].setText(String.format("%02d", time.getSecond()));
    }

    @Override
    public void updateTime(ZonedDateTime time)
    {
        updateStandardClock(time.toLocalTime());
    }
}
