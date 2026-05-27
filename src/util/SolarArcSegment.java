package util;

import java.awt.*;
import java.time.ZonedDateTime;

public record SolarArcSegment(
        ZonedDateTime start,
        ZonedDateTime end,
        Color startingFadeColor,
        Color mainColor,
        Color endingFadeColor
)
{}
