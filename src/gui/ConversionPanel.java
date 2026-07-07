package gui;

import interfaces.QuarterDayObserver;
import main.ClockBrain;
import util.DurationTimeFormatter;
import util.SolarTimes;
import util.TimeConverter;
import util.enums.QuarterDayMark;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.time.ZonedDateTime;

public class ConversionPanel implements QuarterDayObserver
{
    private final JPanel panel;

    private TimeConverter timeConverter;
    private final JLabel[][] table = new JLabel[6][2];

    public ConversionPanel(JPanel panel)
    {
        this.panel = panel;

        refreshTimeConverter();

        panel.setLayout(new GridLayout(6, 2)); // but layout already set?

        Border border = BorderFactory.createLineBorder(Color.GRAY);

        for (int row = 0; row < table.length; row++)
        {
            for (int col = 0; col < table[row].length; col++)
            {
                table[row][col] = new JLabel("", SwingConstants.CENTER);
                table[row][col].setBorder(border);
                panel.add(table[row][col]);
            }
        }

        setHalachicTimeOnLeft();

        // register with ClockBrain as an observer
        ClockBrain clock = ClockBrain.getInstance();
        clock.registerQuarterDayObserver(this);
    }

    /**
     * Set the standard-clock times on the left, so that the halachic durations vary to match the standard times.
     */
    public void setStandardTimeOnLeft()
    {
        clearLabels();

        table[0][0].setText("Standard");
        table[0][1].setText("Halachic");

        table[1][0].setText(DurationTimeFormatter.format(timeConverter.getDuration(), "H:mm:ss.3"));
        table[1][1].setText("Tekufah");

        table[2][0].setText("1 hour");
        table[2][1].setText(DurationTimeFormatter.format(timeConverter.getStandardHourContainsLength(), "H:mm:ss.3"));

        table[3][0].setText("1 minute");
        table[3][1].setText(DurationTimeFormatter.format(timeConverter.getStandardMinuteContainsLength(), "m:ss.3"));

        table[4][0].setText("1 second");
        table[4][1].setText(DurationTimeFormatter.format(timeConverter.getStandardSecondContainsLength(), "s.3"));

        table[5][0].setText("");
        table[5][1].setText("");
    }

    /**
     * Set the halachic-clock times on the left, so that the standard durations vary to match the halachic times.
     */
    public void setHalachicTimeOnLeft()
    {
        clearLabels();

        table[0][0].setText("Halachic");
        table[0][1].setText("Standard");

        table[1][0].setText("Tekufah");
        table[1][1].setText(DurationTimeFormatter.format(timeConverter.getDuration(), "H:mm:ss.3"));

        table[2][0].setText("1 hour");
        table[2][1].setText(DurationTimeFormatter.format(timeConverter.getHalachicHourLength(), "H:mm:ss.3"));

        table[3][0].setText("1 minute");
        table[3][1].setText(DurationTimeFormatter.format(timeConverter.getHalachicMinuteLength(), "m:ss.3"));

        table[4][0].setText("1 second");
        table[4][1].setText(DurationTimeFormatter.format(timeConverter.getHalachicSecondLength(), "s.3"));

        table[5][0].setText("1 cheilek");
        table[5][1].setText(DurationTimeFormatter.format(timeConverter.getHalachicCheilekLength(), "s.3"));
    }

    /**
     * Clear the text in all {@link JLabel}s of the table.
     */
    private void clearLabels()
    {
        for (JLabel[] jLabels : table)
            for (JLabel jLabel : jLabels)
                jLabel.setText("");
    }

    /**
     * Refresh the {@code timeConverter} to use the current span.
     */
    private void refreshTimeConverter()
    {
        ClockBrain clock = ClockBrain.getInstance();
        ZonedDateTime now = clock.getCurrentDateTime();
        SolarTimes solarTimes = clock.getSolarTimes();
        // as of now, all tekufos are the 12-hour variety; will need to allow change
        timeConverter = new TimeConverter(solarTimes.getTekufahStart(now), solarTimes.getTekufahEnd(now), 12);
    }

    /**
     * Called when a quarter-day point (midnight, sunrise, midday, or sunset) has just occurred.
     *
     * @param mark the period that has just occurred
     */
    @Override
    public void updateQuarterDay(QuarterDayMark mark)
    {
        refreshTimeConverter();
        // how to repaint?
    }

    public static void main(String[] args)
    {
        // Create JFrame (main window of application)
        JFrame frame = new JFrame("ConversionPanel Test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(300, 600);
        frame.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new BorderLayout());
        new ConversionPanel(panel);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
