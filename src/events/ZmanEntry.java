package events;

import util.enums.Zman;

/**
 * The details and status (if to be called or not) of a single type of zman.
 *
 * @param zman {@code enum} containing static {@code Zman} data
 * @param enabled If zman should be displayed.
 */
public record ZmanEntry
(
    Zman zman,
    boolean enabled
)
{}
