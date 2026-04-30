package gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Panel to contain the digital clock and related information.  To be used as a layer of the greater program.
 * Individual cells will be used by other classes to create the smaller digital clock parts.
 */
public class DigitalClockPanel extends GridRegionPanel
{
    private boolean
            standardClockEnabled = false,
            halachicClockEnabled = false,
            conversionTableEnabled = false,
            upcomingTimesEnabled = false;
    private final JPanel
            standardClockPanel = new JPanel(),
            halachicClockPanel = new JPanel(),
            conversionTablePanel = new JPanel(),
            upcomingTimesPanel = new JPanel();

    /**
     *
     */
    public DigitalClockPanel()
    {
        super(10, 15);

        // add clock regions
        addRegion(1, 1, 3, 2, standardClockPanel);
        addRegion(11, 1, 3, 2, halachicClockPanel);

        // taller regions
        //addRegion(1, 5, 3, 4, new JPanel()); // conversionTablePanel
        addRegion(11, 5, 3, 4, upcomingTimesPanel); // upcomingTimesPanel
    }

    /// --------------------------------------------------------------
    // Component Creation
    // --------------------------------------------------------------

    private void createStandardClock()
    {
        new StandardClockPanel(standardClockPanel);
    }

    private void createHalachicClock()
    {
        new HalachicClockPanel( halachicClockPanel);
    }

    private void createConversionTable()
    {

    }

    private void createUpcomingTimes()
    {
        new UpcomingTimesPanel(upcomingTimesPanel);
    }

    // --------------------------------------------------------------
    // Setters / Getters
    // --------------------------------------------------------------

    public void setStandardClockEnabled(boolean enabled)
    {
        this.standardClockEnabled = enabled;

        if (standardClockEnabled && standardClockPanel.getComponentCount() == 0) // create when needed and avoid duplicates
            createStandardClock();

        standardClockPanel.setVisible(standardClockEnabled);

    }
    public boolean isStandardClockEnabled() { return standardClockEnabled; }

    public void setHalachicClockEnabled(boolean enabled)
    {
        this.halachicClockEnabled = enabled;

        if (halachicClockEnabled && halachicClockPanel.getComponentCount() == 0) // create when needed and avoid duplicates
            createHalachicClock();

        halachicClockPanel.setVisible(halachicClockEnabled);
    }
    public boolean isHalachicClockEnabled() { return halachicClockEnabled; }

    public void setConversionTableEnabled(boolean enabled)
    {
        this.conversionTableEnabled = enabled;

        if (conversionTableEnabled && conversionTablePanel.getComponentCount() == 0) // create when needed and avoid duplicates
            createConversionTable();

        conversionTablePanel.setVisible(conversionTableEnabled);
    }
    public boolean isConversionTableEnabled() { return conversionTableEnabled; }

    public void setUpcomingTimesEnabled(boolean enabled)
    {
        this.upcomingTimesEnabled = enabled;

        if (upcomingTimesEnabled && upcomingTimesPanel.getComponentCount() == 0) // create when needed and avoid duplicates
            createUpcomingTimes();

        upcomingTimesPanel.setVisible(upcomingTimesEnabled);
    }
    public boolean isUpcomingTimesEnabled() { return upcomingTimesEnabled; }


    public static void main(String[] args)
    {
        // Create JFrame (main window of application)
        JFrame frame = new JFrame("JLayeredPane Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        // Create DigitalClockPanel
        DigitalClockPanel dcp = new DigitalClockPanel();
        dcp.setFillEmptyRegions(true);
        dcp.setDebugBorders(true);
        dcp.construct();
        frame.add(dcp, BorderLayout.CENTER);

        // set components to visible
        dcp.setStandardClockEnabled(true);
        dcp.setHalachicClockEnabled(true);

        frame.setVisible(true);
    }
}
