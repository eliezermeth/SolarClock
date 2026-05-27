package util;

import java.awt.*;
import java.time.ZonedDateTime;

public record SolarArcSegment(
        ZonedDateTime start,
        ZonedDateTime end,
        Color startingColor,
        Color endingColor
)
{}
