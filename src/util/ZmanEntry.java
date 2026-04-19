package util;

/**
 * The details, method call, and status (if to be called or not) of a single type of zman.
 *
 * @param title Name / basic description of zman.
 * @param description Detailed description of calculation / opinion of zman.
 * @param methodName Method in <code>ComplexZmanimCalendar</code> that is called to get the zman.
 * @param enabled If zman should be displayed.
 */
public record ZmanEntry
(
    String title,
    String description,
    String methodName,
    boolean enabled
)
{}
