package util;

import java.util.ArrayList;
import java.util.List;

/**
 * Breaks the <code>ZmanimOptions</code> file into the proper segments for operation.
 */
public class ZmanimFileParser
{
    public static List<ZmanEntry> parse(String text)
    {
        List <ZmanEntry> list = new ArrayList<>();

        String[] sections = text.split("\\*\\*\\*"); // split text on keybreak ***

        for (String section : sections)
        {
            section = section.strip();
            if (section.isEmpty()) continue;

            List<String> lines = section.lines().map(String::strip).filter(s -> !s.isEmpty()).toList();

            String title = lines.getFirst();
            String methodName = lines.get(lines.size() - 2); // second to last line
            boolean isTrue = Boolean.parseBoolean(lines.getLast());

            // optional description
            String description = "";
            if (lines.size() > 3)
                description = String.join("\n", lines.subList(1, lines.size() - 2));

            list.add(new ZmanEntry(title, description, methodName, isTrue));

            return list;
        }
    }
}
