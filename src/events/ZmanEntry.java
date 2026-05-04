package events;

import util.enums.Zman;

import java.lang.reflect.Method;

/**
 * The details, method call, and status (if to be called or not) of a single type of zman.
 *
 * @param zman {@code enum} containing static {@code Zman} data
 * @param method {@codejava.lang.reflect.Method} reflection of method to be called.
 * @param enabled If zman should be displayed.
 */
public record ZmanEntry
(
    Zman zman,
    Method method,
    boolean enabled
)
{}
