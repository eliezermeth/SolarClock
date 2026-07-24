
Analog

The term "midday" (or "midnight") by itself is not, in the context of this program, precise.  There are, in fact, two 
middays: astronomical noon, when the sun is at its zenith, and median noon, when midpoint between sunrise and sunset.  
As such, whenever "noon" or "midday" is used, it can be traced back to its origin point, which should be defined as 
the astronomical point or the median point.

Thus, each period - day and night - can itself be considered to be composed of two separate periods, separated by the 
midpoint.  For day, the segments may be called "dawn day" to make it clear that it is the section of day that borders on 
dawn, and "dusk day", bordering on dusk.  The same applies to night.

The analog clock has its main graphical update execute at midnight.  This is not the 12:00 (0:00 Zulu time) midnight, 
but rather a true midnight, which will either be defined as astronomical or median midnight.  Because of this, the clock 
is not locked to a normal 24 hours - rather, time periods may be compressed or expanded depending on the season.  At 
update, the analog circle (if a full-day circle) displays the dawn-night, both halves of the day, and the 
<i>following</i> dusk-night.

## Terms:
Given the potential for confusion for the same term used to refer to multiple items, a standardized set of terms is
intended to be used throughout the program.  Terms are occasionally used interchangeably, but the meaning should be
understandable.  The terms are not listed alphabetically, but rather in a way that allows later terms to build on former 
terms.
<ul>
  <li>
    <b>standard</b> - The normal, everyday clock/time period.  A day is composed of 24 hours of the same length, each 
    composed of 60 minutes of the same length, etc.  Standardized periods do not change pased on the period, but are 
    always a known and exact amount.
  </li>
  <li>
    <b>halachic</b> - When given in terms of time, this refers to the variable length over a specific period.  Generally 
    paired with a time unit (hour, minute, etc.), this is meant to distinguish it from the standard unit.  See 
    <b>sha'ah</b> for an example.
  </li>
  <li>
    <b>hour</b> - A standard 60-minute period, a standardized time unit used around the world.
  </li>
  <li>
    <b>solar terminator</b> - The line/moment between day and night, often called sunrise or sunset.  When used in the 
    program, if a specific one is required, it will be named.
  </li>
  <li>
    <b>tekufah (<i>Heb., period</i>)</b> - A period beginning at sunrise/sunset and ending at sunset/sunrise.  One-half 
    of the standard solar cycle, two of which compose a day of approximately 24 standard hours.
  </li>
  <li>
    <b>sha'ah (zman) (pl. sha'os (zmanios) (<i>Heb., hour (of time), pl. hours (of time)</i>))</b> - 1 halachic hour, or 
    1/12 of the given day or night period.  The halachic counterpart of the standard hour, this is not bound to the 
    60 standard-minute period.  Depending on the length of the tekufah  (which changes daily, in line with the seasons), 
    it may be longer or shourter than the standard hour.  In the summer, a daytime sha'ah will be longer than 60 
    standard minutes, and a nighttime sha'ah will be shorter than 60 standard minutes.  Conversely, in the winter, a 
    daytime sha'ah will be shorter than a standard hour, and a nighttime sha'ah longer than a standard hour.
  </li>
    <li>
        <b>cheilek (<i>Heb, portion</i>)</b> - 1/1080th of a halachic hour.l
    </li>
</ul>


## Modes:
The analog view has a few different modes:
- ### Sundial
  <p>The circle of the clock is divided into two equal halves.  The top half is day, and the bottom is night.  The 
  horizontal line is composed of the sunrise-sunset horizon.  The view functions like a sundial; the line 
  representing the current time show where the shadow cast by the sundial would fall (and the sun's y-axis-reflected 
  position in the sky).  Despite the sun casting no shadow at night, the current-time line shows the sun's position 
  below the horizon.  The current-time hand will move at different speeds in the two sections, as the length of the 
  current period is compressed or expanded to fit the standard 12-hour section.</p>
- ### Proportional
  <p>The circle of the clock represents (approximately) a 24-hour period.  The circle shows the percentage of that 
  period that is day (centered on the top) and night (bottom).  The current time hand will move at a constant speed 
  through  both sections, as the clock mimics a standard 24-hour clock.</p>

## Modes:
  - ### Sundial view:
    <p>The circle of the clock is divided into two equal halves.  The top half is day, and the bottom is night.  The 
    horizontal line is composed of the sunrise-sunset horizon.  This view functions like a sundial; the line 
    representing the current time shows where the shadow cast by the sundial would fall (and the sun's position in the 
    sky).  Despite the sun casting no shadow at night, the current-time line shows the sun's position below the horizon.
    The current-time hand will move at different speeds in the two sections, as the length of the current period is 
    compressed or expanded to fit the standard 12-hour section.</p>
  - ### Full day view:
    <p>The circle of the clock represents (approximately) a 24-hour period.  The circle shows the percentage of that 
    period that is day (centered on the top) and night (bottom).  The current time hand will move at a constant speed 
    through both sections, as the clock mimics a standard 24-hour clock.</p>
  ### PLANNED
  - ### Half-sundail view:
    <p>Similar to the Sundial view, but only shows the current day or night period.  Day will always be the top half of 
    a circle, and night the bottom.<br>Suitable for landscape screens.</p>
  - ### Dial view:
    <p>An arbitrary time can be selected, from 00:00 to 23:59.  That time will be placed at the topmost position on the 
    circle (12 o'clock on a standard clock).  Times will be displayed from there.  The colored circles will be drawn at 
    the proper points.<br>Example: If 15:00 (3:00 PM) is selected, the leftmost side will be 9:00 (9:00 AM; top - 6),
    the rightmost 21:00 (9:00 PM; top + 6), and the bottom 3:00 (3:00 AM).</p>
  - ### Standard clock view:
    <p>Displays as a standard 12-hour clock.  The colored sections will only be drawn for the currently visible 12-hour 
    period.</p>

