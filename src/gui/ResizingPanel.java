package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyBoundsListener;

public class ResizingPanel extends JPanel implements HierarchyBoundsListener
{
    private int initialPanelWidth;
    private int initialPanelHeight;
    private double initialArea;
    private boolean resizeFont;

    @Override
    public void setSize(Dimension d)
    {
        super.setSize(d);

        this.initialPanelWidth = (int) d.getWidth();
        this.initialPanelHeight = (int) d.getHeight();
        this.initialArea = this.initialPanelWidth * this.initialPanelHeight;
    }

    @Override
    public void setSize(int width, int height)
    {
        super.setSize(width, height);

        this.initialPanelWidth = width;
        this.initialPanelHeight = height;
        this.initialArea = this.initialPanelWidth * this.initialPanelHeight;
    }

    @Override
    public void ancestorMoved(HierarchyEvent e)
    {
        // not used
    }

    @Override
    public void ancestorResized(HierarchyEvent e)
    {

    }
}
