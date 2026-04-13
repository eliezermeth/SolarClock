package util.enums;

/**
 * Defines the options for how the tick marks on the analog clock should be defined.<br>
 * {@link #ONE_TWELFTH_OF_TEKUFAH} - Tekufah is delineated by sunrise and sunset, and divided into twelve equal parts.<br>
 * {@link #SHAAH_HALACHIC_CALCULATION} - Day starts by alos and ends ???; use <code>ComplexZmanimCalendar</code> to
 * get appropriate sha'ah length.
 */
public enum SHAAH_TICK_MARK_STYLE
{
    /** Tekufah is delineated by sunrise and sunset, and divided into twelve equal parts. */
    ONE_TWELFTH_OF_TEKUFAH,
    /** Day starts by alos and ends ???; use <code>ComplexZmanimCalendar</code> to get appropriate sha'ah length. */
    SHAAH_HALACHIC_CALCULATION
}
