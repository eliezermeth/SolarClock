package util.enums;

/**
 * Significant points on a circle, in radians.<br>
 * {@link #RIGHT} - Where 3 o'clock would be on a clock.  Also equals 0.  The starting point of a drawn circle.<br>
 * {@link #TOP} - Where 12 o'clock would be on a clock.<br>
 * {@link #LEFT} - Where 9 o'clock would be on a clock.<br>
 * {@link #BOTTOM} - Where 6 o'clock would be on a clock.
 */
public enum Circle
{
    /**
     * Where 3 o'clock would be on a clock.  Also equals 0.  The starting point of a drawn circle.
     */
    RIGHT (2 * Math.PI),
    /**
     * Where 12 o'clock would be on a clock.
     */
    TOP (Math.PI / 2),
    /**
     * Where 9 o'clock would be on a clock.
     */
    LEFT (Math.PI),
    /**
     * Where 6 o'clock would be on a clock.
     */
    BOTTOM (3 * Math.PI / 2);

    public final double radians;

    Circle(double radians)
    {
        this.radians = radians;
    }
}
