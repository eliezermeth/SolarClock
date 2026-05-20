package util;

import java.awt.*;
import java.time.ZonedDateTime;

public record TwilightSegment(
        ZonedDateTime start,
        ZonedDateTime end,
        Color color
)
{}
