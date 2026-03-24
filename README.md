gui.ZmanClockGUI:
- Class Javadoc:
  - update terms used
  - update description
- Make labels no longer overlap other elements
- make current time hand (and all others) no longer overlap other elements (i.e. bounding circle)
- currently uses getSunrise/Sunset for delineations; change?
  - higher-order functions?
  - other time for delineation?
- when the colored sections (and zmanim) rotate
- all: when repaint triggers; for one or all
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

## Terms:
Given to the potential for confusion for the same term used to refer to multiple items, a standardized set of terms is 
intended to be used throughout the program.
  - <b>standard:</b> The normal, everyday clock/time period.  Composed of 24 hours of the same length, each composed of 
60 minutes of the same length, etc.
  - <b>hour:</b> A standard 60-minute period.
  - <b>solar terminator:</b> The line between day and night.  In this program, the terminators are designated as sunrise 
and sunset.
  - <b>tekufah (<i>Heb., period</i>):</b> A period beginning at sunrise/sunset and ending at sunset/sunrise.  1/2 of the standard solar cycle.
  - <b>sha'ah (zman) (pl. sha'os (zmanios) (<i>Heb., hour (of time), pl. hours (of time)</i>)):</b> 1 halachic hour; 
1/12 of the day or night period.  The halachic counterpart to the hour, this is not bound to the 60-minute 
period.  Depending on the length of the tekufah, it may be longer or shorter.