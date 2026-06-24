package gui;

import main.ClockBrain;
import util.SolarTimes;
import util.TimeConverter;

import javax.swing.*;
import java.awt.*;
import java.time.ZonedDateTime;

public class ConversionPanel
{
    private final JPanel panel;

    private TimeConverter timeConverter;

    public ConversionPanel(JPanel panel)
    {
        this.panel = panel;

        ClockBrain clock = ClockBrain.getInstance();
        ZonedDateTime now = clock.getCurrentDateTime();
        SolarTimes solarTimes = clock.getSolarTimes();
        // as of now, all tekufos are the 12-hour variety; will need to allow change
        timeConverter = new TimeConverter(solarTimes.getTekufahStart(now), solarTimes.getTekufahEnd(now), 12);
    }

    public static void main(String[] args)
    {
        // Create JFrame (main window of application)
        JFrame frame = new JFrame("ConversionPanel Test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(300, 600);
        frame.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        new ConversionPanel(panel);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
