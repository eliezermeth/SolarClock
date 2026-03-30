package gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Creates a panel divided into a grid.  Cells of the grid may be combined into a single panel.  Used for positioning
 * items.
 */
public class GridRegionPanel extends JPanel
{
    private final int rows;
    private final int cols;

    private final ArrayList<GridRegion> regions = new ArrayList<>();
    private boolean[][] occupied; // tracks which cells are already taken

    private boolean fillEmptyRegions = false;
    private boolean debugBorders = false;

    private boolean constructed = false;

    /**
     * Create the panel of a grid of the specified size.
     * @param rows initial rows on the grid
     * @param columns initial columns on the grid
     */
    public GridRegionPanel(int rows, int columns)
    {
        this.rows = rows;
        this.cols = columns;

        // initialize occupied array
        occupied = new boolean[this.cols][this.rows];
    }

    /**
     * Layout setup.
     */
    private void buildLayout()
    {
        GridBagLayout gbl = new GridBagLayout();

        gbl.rowWeights = new double[rows];
        gbl.columnWeights = new double[cols];
        gbl.rowHeights = new int[rows];
        gbl.columnWidths = new int[cols];

        Arrays.fill(gbl.rowWeights, 1.0);
        Arrays.fill(gbl.columnWeights, 1.0);

        this.setLayout(gbl);
    }

    /**
     * Attempt to add a panel over a region.  If the region overlaps with an existing region or exits grid bounds, the
     * add will fail.  If the region is added, it will be wrapped in a "zeroing" wrapper panel to prevent it from
     * expanding past its needed space.
     * @param x x-position in grid of top left of large cell
     * @param y y-position in grid of top left of large cell
     * @param width width of large cell
     * @param height height of large cell
     * @param panel <code>JPanel</code> to be used for large cell
     * @return If panel was successfully added.
     */
    public boolean addRegion(int x, int y, int width, int height, JPanel panel)
    {
        if (constructed)
            throw new IllegalStateException("Cannot add regions after grid has already been constructed.");

        if (x < 0 || y < 0 || // starts out of bounds
                x + width > this.cols || y + height > this.rows) // expands out of bounds
            return false;

        if (width < 1 || height < 1) // region must be at least 1x1
            return false;

        // Test the spaces where the new regions is to be added
        // pass 1: check if cell is already part of existing region
        for (int col = x; col < x + width; col++)
            for (int row = y; row < y + height; row++)
                if (occupied[col][row])
                    return false;
        // pass 2: apply
        for (int col = x; col < x + width; col++)
            for (int row = y; row < y + height; row++)
                occupied[col][row] = true;

        regions.add(new GridRegion(x, y, width, height, buildZeroingWrapper(panel)));
        return true;
    }

    /**
     * Build a wrapper panel for another panel.  The wrapper will not influence <code>GridBagLayout</code>'s expanding
     * feature to increase the size the panel takes, but will remain within its preset bounds.
     * @param panel <code>JPanel</code> to be placed inside wrapper
     * @return wrapper <code>JPanel</code>
     */
    private JPanel buildZeroingWrapper(JPanel panel)
    {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.CENTER);

        // set minimum/preferred size to 0, 0 to prevent it from expanding the GridBagLayout
        wrapper.setPreferredSize(new Dimension(0, 0));
        wrapper.setMinimumSize(new Dimension(0, 0));

        return wrapper;
    }

    /**
     * For debugging; adds regions to the unused sections of the grid.
     */
    private void fillEmptyRegions()
    {
        for (int x = 0; x < cols; x++)
            for (int y = 0; y < rows; y++)
                if (!occupied[x][y]) // if cell is not occupied, create 1x1 region
                {
                    GridRegion filler = new GridRegion(x, y, 1, 1, new JPanel());
                    regions.add(filler);
                    occupied[x][y] = true; // mark slot as currently occupied
                }
    }

    /**
     * Add regions to panel.
     */
    private void applyRegions()
    {
        for (GridRegion r : regions)
        {
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = r.x; gbc.gridy = r.y;
            gbc.gridwidth = r.width; gbc.gridheight = r.height;

            gbc.weightx = 1.0; gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;

            r.panel.setOpaque(false); // set unused areas to transparent

            this.add(r.panel, gbc);
        }
    }

    /**
     * For debugging; adds a border around each region.
     */
    private void debugBorders()
    {
        for (GridRegion r : regions)
            r.panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    /**
     * Build the grid with internal panels.
     * Settings for empty regions and borders will be applied here. If empty regions are to be filled, no more regions
     * can be added in the future.
     */
    public void construct()
    {
        buildLayout();
        if (fillEmptyRegions) fillEmptyRegions();
        applyRegions();
        if (debugBorders) debugBorders();

        constructed = true;
    }

    // Getters / Setters ----------------------------------------------------
    public void setFillEmptyRegions(boolean fill)
    {
        fillEmptyRegions = fill;
    }
    public boolean getFillEmptyRegions()
    {
        return fillEmptyRegions;
    }

    public void setDebugBorders(boolean borders)
    {
        debugBorders = borders;
    }
    public boolean getDebugBorders()
    {
        return debugBorders;
    }

    public static void main(String[] args)
    {
        // Create JFrame (main window of application)
        JFrame frame = new JFrame("GridRegionPanel Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        GridRegionPanel grp = new GridRegionPanel(10, 15);
        grp.addRegion(1, 1, 2, 2, new JPanel());
        grp.setFillEmptyRegions(true);
        grp.setDebugBorders(true);
        grp.construct();
        frame.add(grp, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
