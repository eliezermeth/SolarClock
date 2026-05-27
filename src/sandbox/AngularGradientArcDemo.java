package sandbox;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class AngularGradientArcDemo extends JPanel {

    public AngularGradientArcDemo() {
        setPreferredSize(new Dimension(700, 700));
        setBackground(Color.DARK_GRAY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 100;
        int y = 100;
        int size = 500;

        double startAngle = 0;
        double extent = 320;
        double totalExtent = extent - startAngle;

        double radius = size / 2.0;

        Color startingFadeColor = Color.CYAN;
        Color mainColor = Color.YELLOW;
        Color endingFadeColor = Color.MAGENTA;

        // degrees for fade regions
        double startFadeRatio = 0.1;
        double endFadeRatio = 0.1;
        // prevent overlap
        startFadeRatio = Math.max(0, Math.min(startFadeRatio, 1));
        endFadeRatio   = Math.max(0, Math.min(endFadeRatio, 1));
        // prevent invalid geometry
        if (startFadeRatio + endFadeRatio > 1.0)
        {
            double scale = 1.0 / (startFadeRatio + endFadeRatio);
            startFadeRatio *= scale;
            endFadeRatio *= scale;
        }

        double startFadeDegrees = totalExtent * startFadeRatio;
        double endFadeDegrees = totalExtent * endFadeRatio;
        double middleDegrees = (extent - startAngle) - startFadeDegrees - endFadeDegrees; // remaining for solid middle

        // final slice count
        int startSlices = (int) Math.ceil(radius * Math.toRadians(startFadeDegrees));
        int endSlices = (int) Math.ceil(radius * Math.toRadians(endFadeDegrees));

        // draw start gradient
        for (int i = 0; i < startSlices; i++)
        {
            // calculate the position in the range of slices
            float t = (startSlices <= 1) ? 1f : (float) i / (startSlices - 1);

            // 0.5f * t splits the possible range in half, then +0.5f forces it to the second half
            float mappedT = 0.5f + 0.5f * t; // x -> y, second half only

            // get the color in the specified position between the two colors
            Color blended = interpolate(startingFadeColor, mainColor, mappedT);

            // compute where the slice stars around the circle; incremented each time
            double sliceStart = startAngle + (startFadeDegrees * i / startSlices);

            // calculate the angular span of the slice, then add +0.5 to hide the rendering gaps
            double sliceExtent = startFadeDegrees / startSlices + 0.5;

            // create wedge, set color, and draw
            Arc2D.Double slice = new Arc2D.Double(x, y, size, size, sliceStart, sliceExtent, Arc2D.PIE);
            g2.setColor(blended);
            g2.fill(slice);
        }

        // draw solid middle
        Arc2D.Double middleArc = new Arc2D.Double(x, y, size, size,
                startAngle + startFadeDegrees, middleDegrees, Arc2D.PIE);
        g2.setColor(mainColor);
        g2.fill(middleArc);

        // draw end gradient
        for (int i = 0; i < endSlices; i++)
        {
            float t = (endSlices <= 1) ? 1f : (float) i / (endSlices - 1);
            float mappedT = 0.5f * t; // y -> z, first half only
            Color blended = interpolate(mainColor, endingFadeColor, mappedT);
            double sliceStart = startAngle + startFadeDegrees + middleDegrees + (endFadeDegrees * i / endSlices);
            double sliceExtent = endFadeDegrees / endSlices + 0.5;
            Arc2D.Double slice = new Arc2D.Double(x, y, size, size, sliceStart, sliceExtent, Arc2D.PIE);
            g2.setColor(blended);
            g2.fill(slice);
        }

        // fix seams
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        // start - middle boundary
        g2.setColor(mainColor);
        g2.draw(new Arc2D.Double(x, y, size, size, startAngle + startFadeDegrees, 0.01, Arc2D.PIE));
        // middle - end boundary
        g2.setColor(mainColor);
        g2.draw(new Arc2D.Double(x, y, size, size, startAngle + startFadeDegrees + middleDegrees, 0.01, Arc2D.PIE));

        // Outer outline
        Arc2D.Double outline = new Arc2D.Double(
                x, y, size, size,
                startAngle, extent,
                Arc2D.PIE);
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(Color.WHITE);
        g2.draw(outline);

        // Title
        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2.drawString("Angular / Conical Gradient", 180, 640);

        g2.dispose();
    }

    private Color interpolate(Color c1, Color c2, float t)
    {
        int r = (int) (c1.getRed() * (1 - t) + c2.getRed() * t);
        int g = (int) (c1.getGreen() * (1 - t) + c2.getGreen() * t);
        int b = (int) (c1.getBlue() * (1 - t) + c2.getBlue() * t);

        return new Color(r, g, b);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Angular Gradient Arc");

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new AngularGradientArcDemo());

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}