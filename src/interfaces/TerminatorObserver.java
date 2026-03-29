package interfaces;

/**
 * Interface for when the terminators of the clock change; used when new data must be calculated.
 */
public interface TerminatorObserver
{
    /**
     * Called when the terminators of the clocks change; allows internal data to be recalculated.
     */
    void updateTerminatorCalculations();
}
