package gui;

import interfaces.TerminatorObserver;
import interfaces.TimeObserver;
import main.ClockBrain;
import util.Constants;
import util.Terminator;
import util.TimeUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;

public class HalachicClockPanel implements TimeObserver, TerminatorObserver
{
    private ClockBrain clock;
    private DigitalClockPanel parent;
    private JPanel panel;

    // Moving parts
    private JLabel[] fracClockComponents = new JLabel[2]; // HH Chalakim
    private JLabel[] regularClockComponents = new JLabel[3]; // HH MM SS

    // Values for calculations
    private int hour = -1, min = -1; // hour
    private Terminator currentTekufah = null;
    private LocalTime tekufahStart = null;
    private long shaahMillis; // length of hour
    private double cheilekFracPerMilli = -1;
    double adjustedMinuteLength = -1, adjustedSecondLength = -1; // milliseconds in halachic minute/second for tekufah

    public HalachicClockPanel(DigitalClockPanel parent, JPanel child)
    {
        this.clock = ClockBrain.getInstance();
        this.parent = parent;
        this.panel = child;

        createHalachicClock();

        // register with ClockBrain as an observer
        clock.registerTimeObserver(this);
        clock.registerTerminatorObserver(this);
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

        updateTerminatorCalculations();
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
     * @param currentTime
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
    public void updateTime(LocalTime time)
    {
        updateHalachicClock(time);
    }

    @Override
    public void updateTerminatorCalculations()
    {
        // reset current tekufah
        currentTekufah = clock.getTerminatorTimes().getStartingTerminator();
        tekufahStart = clock.getTerminatorTimes().getTerminator(0);

        // recalculate shaah-hour length
        shaahMillis = clock.getTerminatorTimes().getTekufahShaah(0);
        cheilekFracPerMilli = (double) Constants.CHALAKIM_PER_SHAAH / shaahMillis;

        // Standard-to-Halachic conversion ratio:
        // (number of millis in a standard hour) divided by (number of millis in halachic hour) and then flipped
        // over 1 to change it to a multiplication factor (1.1 -> 0.9; 0.9 -> 1.1; etc)
        double clockStyleConversionRatio = 2.0 - ((60000 * 60) / (double) shaahMillis);
        // results in the number of milliseconds in a halachic-style millisecond for the tekufah
        adjustedMinuteLength = 60000 * clockStyleConversionRatio; // 60,000 milliseconds in 1 minute
        adjustedSecondLength = 1000 * clockStyleConversionRatio; // 1,000 milliesconds in 1 second
    }
}
