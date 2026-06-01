package gui;

import interfaces.QuarterDayObserver;
import interfaces.TimeObserver;
import main.ClockBrain;
import util.Constants;
import util.SolarTimes;
import util.enums.QuarterDayMark;
import util.TimeUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class HalachicClockPanel implements TimeObserver, QuarterDayObserver
{
    private final ClockBrain clock;
    private final JPanel panel;

    // Moving parts
    private final JLabel[] fracClockComponents = new JLabel[2]; // HH Chalakim
    private final JLabel[] regularClockComponents = new JLabel[3]; // HH MM SS

    // Values for calculations
    private LocalTime tekufahStart = null;
    private long shaahMillis; // length of hour
    private double cheilekFracPerMilli = -1;
    double adjustedMinuteLength = -1, adjustedSecondLength = -1; // milliseconds in halachic minute/second for tekufah

    public HalachicClockPanel(JPanel child)
    {
        this.clock = ClockBrain.getInstance();
        this.panel = child;

        createHalachicClock();

        // register with ClockBrain as an observer
        clock.registerTimeObserver(this);
        clock.registerQuarterDayObserver(this);
    }

    private void createHalachicClock()
    {
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // padding

        // Label
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel text = new JLabel("Halachic Time:");
        panel.add(text, gbc);

        // Initialize changing components
        for (int i = 0; i < fracClockComponents.length; i++) // fractional clock
            fracClockComponents[i] = new JLabel("--");
        for (int i = 0; i < 3; i++)
            regularClockComponents[i] = new JLabel("--"); // standard clock

        updateQuarterDay(QuarterDayMark.SUNRISE); // force-trigger
        updateHalachicClock();

        // Lay out clocks
        // Fractional clock
        JPanel fracPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        fracPanel.add(fracClockComponents[0]); // hours
        fracPanel.add(new JLabel("hours"));
        fracPanel.add(fracClockComponents[1]); // chalakim elapsed
        fracPanel.add(createTransparentLabel("/"));
        fracPanel.add(createTransparentLabel(String.valueOf(Constants.CHALAKIM_PER_SHAAH)));
        fracPanel.add(new JLabel("chalakim"));
        // add to clock
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(fracPanel, gbc);

        // Standard-style clock
        // Lay out clock
        JPanel standPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 0));
        standPanel.add(regularClockComponents[0]); // hours
        standPanel.add(new JLabel(":"));
        standPanel.add(regularClockComponents[1]); // minutes
        standPanel.add(new JLabel(":"));
        standPanel.add(regularClockComponents[2]); // seconds
        // add to clock
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(standPanel, gbc);
    }

    /**
     * Create a transparent label.
     * @param text Text for label.
     * @return <code>JLabel</code>
     */
    private JLabel createTransparentLabel(String text)
    {
        JLabel temp = new JLabel(text);
        temp.setOpaque(false);
        return temp;
    }

    /**
     * Update the halachic clock.
     */
    public void updateHalachicClock()
    {
        updateHalachicClock(clock.getCurrentTime());
    }

    /**
     * Update the halachic clock to the current time.
     * @param currentTime the current {@code LocalTime}
     */
    public void updateHalachicClock(LocalTime currentTime)
    {
        // get length from beginning of tekufah until now
        long millisElapsed = TimeUtil.calculateMillisBetween(tekufahStart, currentTime);
        long hHours = millisElapsed / shaahMillis; // num of elapsed hours
        long hRemainder = millisElapsed % shaahMillis; // remaining time; hours deducted

        // calculate chalakim for fractional clock
        int hChalakim = (int) (hRemainder * cheilekFracPerMilli);

        // calculate minutes : seconds
        long hcMinutes = (long) (hRemainder / adjustedMinuteLength);
        long hcSeconds = (long) ((hRemainder % adjustedMinuteLength) / adjustedSecondLength);

        // set texts of individual sections
        fracClockComponents[0].setText(String.format("%02d", hHours)); // hours
        fracClockComponents[1].setText(String.valueOf(hChalakim)); // chalakim

        regularClockComponents[0].setText(String.format("%02d", hHours)); // hours
        regularClockComponents[1].setText(String.format("%02d", hcMinutes)); // minutes
        regularClockComponents[2].setText(String.format("%02d", hcSeconds)); // seconds
    }

    @Override
    public void updateTime(ZonedDateTime time)
    {
        updateHalachicClock(time.toLocalTime());
    }

    @Override
    public void updateQuarterDay(QuarterDayMark mark)
    {
        // if sunrise or sunset did not just occur, no need to recalculate
        if (mark != QuarterDayMark.SUNRISE && mark != QuarterDayMark.SUNSET) return;

        // recalculate the time spans for the current tekufah
        SolarTimes solarTimes = clock.getSolarTimes();
        ZonedDateTime current = clock.getCurrentDateTime();
        // reset current tekufah
        tekufahStart = solarTimes.getTekufahStart(current).toLocalTime();
        shaahMillis = solarTimes.getTekufahShaah(current);

        // recalculate shaah-hour length
        cheilekFracPerMilli = (double) Constants.CHALAKIM_PER_SHAAH / shaahMillis;

        // Standard-to-Halachic conversion ratio:
        // (number of millis in a standard hour) divided by (number of millis in halachic hour) and then flipped
        // over 1 to change it to a multiplication factor (1.1 -> 0.9; 0.9 -> 1.1; etc.)
        double clockStyleConversionRatio = 2.0 - ((60000 * 60) / (double) shaahMillis);
        // results in the number of milliseconds in a halachic-style millisecond for the tekufah
        adjustedMinuteLength = 60000 * clockStyleConversionRatio; // 60,000 milliseconds in 1 minute
        adjustedSecondLength = 1000 * clockStyleConversionRatio; // 1,000 milliesconds in 1 second
    }

    public static void main(String[] args)
    {
        // Create JFrame
        JFrame frame = new JFrame("Halachic Clock Testing");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(600, 200);
        frame.setLayout(new BorderLayout());

        JPanel childPanel = new JPanel();

        // construct clock panel
        new HalachicClockPanel(childPanel);
        frame.add(childPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
