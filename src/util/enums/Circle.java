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

/*
                                        π/2
                                      ~1.570
                                     •••••••••
                               ••••••    |    ••••••
                          •••        12 o'clock       •••
               3π/4    •••               |               •••    π/4
             ~2.356  ••                  |                  ••  ~0.785
                   ••   \                |                /   ••
                •••         \            |            /         •••
            ••                   \       |       /                   ••
           ••                          \ | /                          ••
         π •-- 9 o'clock ----------------+---------------- 3 o'clock --• 0, 2π
    ~3.141 ••                          / | \                          •• 0, ~6.283
            ••                   /       |       \                   ••
                •••         /             |            \         •••
                   ••   /                |                \   ••
               5π/4  ••                  |                  ••  7π/4
             ~3.926    •••               |               •••    ~5.497
                          •••         6 o'clock       •••
                               ••••••    |    ••••••
                                     •••••••••
                                         3π/2
                                        ~4.712
 */