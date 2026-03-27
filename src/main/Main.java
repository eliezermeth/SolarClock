package main;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import com.kosherjava.zmanim.util.GeoLocation;
import interfaces.EqualViewOption;
import util.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Central hub.
 *
 * TODO: THIS IS A TEST; IT IS NOT END CODE
 */
public class Main
{
    protected ComplexZmanimCalendar czc;

    protected boolean equalDayNightView = false;
    protected ViewMode viewMode; // not needed - visual clock type


    protected final long MILLIS_PER_DAY = 86400000L;

    /**
     * Initialize the program
     */
    public Main(ComplexZmanimCalendar czc)
    {
        this.czc = (ComplexZmanimCalendar) czc;
    }

    public static void main(String[] args) throws InterruptedException, InvocationTargetException
    {
        // set up clock
        GeoData location = Regions.getLocation("Pikesville");
        Main clock = new Main(new ComplexZmanimCalendar(
                new GeoLocation(
                        location.getName(), location.getLatitude(), location.getLongitude(),
                        TimeZone.getTimeZone(location.getRegion())
                )
        ));

        // TODO for GUI?
        // have the GUI section (here, DigitalClock) be invoked via this fashion, then it can be interacted with as normal
        // allow swing to create thread-independent clock; ref through clockRef[0]
        final DigitalClock[] clockRef = new DigitalClock[1];
        SwingUtilities.invokeAndWait(() -> {
            clockRef[0] = new DigitalClock();
        });
        DigitalClock digitalClock = clockRef[0];
    }
}

class DigitalClock
{
    JLabel standardTime, halachicTime, halachicTimeStandard;
    JLabel halachicHourLength;

    public DigitalClock()
    {
        // Create window
        JFrame frame = new JFrame("Text Clock");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new GridBagLayout());
        // use gridbagconstrainst for easy positioning
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // padding
        // Label 1 and text field
        gbc.gridx = 0;
        gbc.gridy = 0;
        frame.add(new JLabel("Standard Time:"), gbc);
        gbc.gridx = 1;
        standardTime = new JLabel("--:--:--.----");
        frame.add(standardTime, gbc);
        // Label 2 and text field
        gbc.gridx = 0;
        gbc.gridy = 1;
        frame.add(new JLabel("Halachic Time:"), gbc);
        gbc.gridx = 1;
        halachicTime = new JLabel("-- hours --/1080 chalakim");
        frame.add(halachicTime, gbc);
        // Label 3 and text field
        gbc.gridx = 0;
        gbc.gridy = 2;
        frame.add(new JLabel("Halachic Time Standard:"), gbc);
        gbc.gridx = 1;
        halachicTimeStandard = new JLabel("--:--:--.----");
        frame.add(halachicTimeStandard, gbc);
        // Label 4 and text field
        gbc.gridx = 0;
        gbc.gridy = 3;
        frame.add(new JLabel("Halachic Hour Length:"), gbc);
        gbc.gridx = 1;
        halachicHourLength = new JLabel("--:--:--.----");
        frame.add(halachicHourLength, gbc);
        // show window
        frame.setVisible(true);
    }

    public void setStandardTime(String s) { standardTime.setText(s); }
    public void setHalachicTime(String s) { halachicTime.setText(s); }
    public void setHalachicTimeStandard(String s) { halachicTimeStandard.setText(s); }
    public void setHalachicHourLength(String s) { halachicHourLength.setText(s); }
}
