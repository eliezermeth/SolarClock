package util.debug;

/**
 * Holds settings for debugging modifications.
 * @param <E> {@code type} to be used by the option
 */
public abstract class DebugOption<E>
{
    private boolean enabled;
    private E value;

    protected DebugOption(boolean enabled, E value)
    {
        this.enabled = enabled;
        this.value = value;
    }

    /**
     * Get the status of the debug option.
     * @return {@code true} if enabled
     */
    public boolean isEnabled() { return enabled; }

    /**
     * Set the status of the debug option.
     * @param enabled {@code true} if enabled
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * Get the value of the debug option.
     * @return stored value of option
     */
    public E get() { return value; }

    /**
     * Set the value of the debug option.
     * @param value value to set the value of the option
     */
    public void set(E value) { this.value = value; }
}
