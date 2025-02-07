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
## Modes:
  - ### Sundial view:
    <p>The circle of the clock is divided into two equal halves.  The top half is day, and the bottom is night.  The 
    horizontal line is composed of the sunrise-sunset horizon.  This view functions like a sundial; the line 
    representing the current time shows where the shadow cast by the sundial would fall (and the sun's position in the 
    sky).  Despite the sun casting no shadow at night, the current-time line shows the sun's position below the horizon.
    The current-time hand will move at different speeds in the two sections, as the length of the current period is 
    compressed or expanded to fit the standard 12-hour section.</p>
  - ### 24-hour view:
    <p>The circle of the clock represents (approximately) a 24-hour period.  The circle shows the percentage of that 
    period that is day (centered on the top) and night (bottom).  The current time hand will move at a constant speed 
    through both sections, as the clock mimics a standard 24-hour clock.</p>
  ### UPCOMING
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