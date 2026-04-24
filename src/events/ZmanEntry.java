package events;

import java.lang.reflect.Method;

/**
 * The details, method call, and status (if to be called or not) of a single type of zman.
 *
 * @param title Name / basic description of zman.
 * @param description Detailed description of calculation / opinion of zman.
 * @param methodName Method in <code>ComplexZmanimCalendar</code> that is called to get the zman.
 * @param method <code>java.lang.reflect.Method</code> reflection of method to be called.
 * @param enabled If zman should be displayed.
 */
public record ZmanEntry
(
    String title,
    String description,
    String methodName,
    Method method,
    boolean enabled
)
{}
