package main;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import com.kosherjava.zmanim.util.GeoLocation;
import gui.AnalogClockPanel;
import gui.DigitalClockPanel;
import gui.GridRegionPanel;
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

    /**
     * Initialize the program
     */
    public Main(ComplexZmanimCalendar czc)
    {
        this.czc = (ComplexZmanimCalendar) czc;
    }

    public static void main(String[] args) throws InterruptedException, InvocationTargetException
    {
        JFrame frame = new JFrame("Zmanim Clock");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        // Create JLayeredPane to manage layering of components
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        frame.add(layeredPane, BorderLayout.CENTER); // add JLayeredPane to JFrame

        // Layer 0 - analog clock
        AnalogClockPanel clockPanel = new AnalogClockPanel();
        clockPanel.addHourTickMarks();
        GridRegionPanel analogGRP = new GridRegionPanel(10, 15);
        analogGRP.addRegion(2, 1, 11, 8, clockPanel);
//        analogGRP.setFillEmptyRegions(true);
//        analogGRP.setDebugBorders(true);
        analogGRP.construct();
        analogGRP.setOpaque(false);
        layeredPane.add(analogGRP, Integer.valueOf(0));

        // Layer 1 - digital clock
        DigitalClockPanel dcp = new DigitalClockPanel();
        dcp.setOpaque(false);
        dcp.construct();
        layeredPane.add(dcp, Integer.valueOf(1));
        dcp.setStandardClockEnabled(true);
        dcp.setHalachicClockEnabled(true);
        dcp.setConversionTableEnabled(false);
        dcp.setUpcomingTimesEnabled(true);


        frame.setVisible(true);

//        // TODO for GUI?
//        // have the GUI section (here, DigitalClock) be invoked via this fashion, then it can be interacted with as normal
//        // allow swing to create thread-independent clock; ref through clockRef[0]
//        final DigitalClock[] clockRef = new DigitalClock[1];
//        SwingUtilities.invokeAndWait(() -> {
//            clockRef[0] = new DigitalClock();
//        });
//        DigitalClock digitalClock = clockRef[0];
    }
}
