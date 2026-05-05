package util;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import events.ZmanEntry;
import util.enums.Zman;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Breaks the {@code ZmanimOptions} file into the proper segments for operation.
 */
public class ZmanOptionsConfigManager
{
    private static ZmanOptionsConfigManager INSTANCE = new ZmanOptionsConfigManager();

    private final ZmanEntry[] entries = new ZmanEntry[Zman.values().length]; // set to number of enum Zman

    private static final String filename = "src/util/ZmanimOptions";

    private ZmanOptionsConfigManager()
    {
        loadFile();
    }

    public synchronized static ZmanOptionsConfigManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Load in the file from text.
     */
    private void loadFile()
    {
        // clear entries array
        Arrays.fill(entries, null);

        try {
            List<String> lines = Files.readAllLines(Path.of(filename));

            for (String raw : lines)
            {
                String line = raw.strip();
                if (line.isEmpty() || line.startsWith("#")) continue; // skip blank and comment lines

                String[] parts = line.split(",");
                if (parts.length != 2) continue; // improperly formatted line

                String id = parts[0].trim();
                boolean enabled = Boolean.parseBoolean(parts[1].trim());

                Zman zman = Zman.fromId(id);
                if (zman == null) continue; // unknown ID; skip (possible log)

                entries[zman.ordinal()] = new ZmanEntry(zman, generateReflectedMethod(zman), enabled); // add to proper slot in order
            }

            // Append any missing enum values (to keep system stable if enum grows); TODO make sure adds in order
            for (int i = 0; i < entries.length; i++)
                if (entries[i] != null && entries[i] == null) // was not set
                {
                    Zman zman = Zman.values()[i];
                    entries[i] = new ZmanEntry(zman, generateReflectedMethod(zman), false); // add as disabled
                }
        } catch (IOException e) {
            throw new RuntimeException("Error reading config file", e);
        }
    }

    /**
     * Returns the reflected method the {@code Zman} runs through {@code ComplexZmanimCalendar}.  Throws a
     * {@code RunTimeException} if the attempted method call fails.
     * @param zman {@code Zman} containing the method to call
     * @return reflected {@code Method}
     */
    private Method generateReflectedMethod(Zman zman)
    {
        try
        {
            return ComplexZmanimCalendar.class.getMethod(zman.getMethodName());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed attempting call of method " + zman.getMethodName() + " for " +
                    zman, e);
        }
    }

    /**
     * Toggle (and save) the setting for a method between true and false.
     * @param zman the {@code Zman} for the setting to be toggled.
     * @throws IOException If file was unable to be successfully written.
     */
    public void toggle(Zman zman) throws IOException
    {
        for (int i = 0; i < entries.length; i++)
        {
            if (entries[i] != null &&entries[i].zman() == zman)
            {
                ZmanEntry temp = entries[i];
                entries[i] = new ZmanEntry(temp.zman(), temp.method(), !temp.enabled());

                rewriteFile();
                return; // break out of loop and avoid throwing error
            }
        }

        // if method not found in list
        throw new IllegalArgumentException("Zman not found: " + zman);
    }

    /**
     * Rewrite the file for storage.
     * @throws IOException If file was not read/written successfully.
     */
    private void rewriteFile() throws IOException
    {
        StringBuilder sb = new StringBuilder();

        // mutable copy, to facilitate addition of missing elements
        Map<Zman, ZmanEntry> map = new EnumMap<>(Zman.class);
        for (ZmanEntry e : entries)
            map.put(e.zman(), e);

        List<String> lines = Files.readAllLines(Path.of(filename));

        if (!lines.isEmpty()) // read successful; write with existing order and comments
        {
            for (String raw : lines)
            {
                String line = raw.stripLeading(); // acceptable loss of leading whitespace characters for rewrite
                if (line.isEmpty() || line.startsWith("#")) // empty or comment
                {
                    sb.append(line).append("\n");
                }
                else
                {
                    String[] parts = line.split(",");
                    if (parts.length != 2)
                    {
                        sb.append(line).append("\n"); // preserve improperly formatted lines as safety measure
                        continue;
                    }

                    Zman zman = Zman.fromId(parts[0].trim());
                    if (zman == null)
                    {
                        sb.append(line).append("\n"); // preserve improperly formatted lines as safety measure
                        continue;
                    }

                    ZmanEntry e = map.remove(zman);
                    if (e != null)
                        sb.append(zman.getId()).append(",").append(e.enabled()).append("\n");
                }
            }
        }

        // add missing zman options (or if file not read) to config file
        for (ZmanEntry e : map.values())
        {
            sb.append("# ").append(e.zman().getTitle()).append("\n"); // sets title as comment
            sb.append(e.zman().getId()).append(",").append(e.enabled()).append("\n");
        }

        Files.writeString(Path.of(filename), sb.toString().strip());
    }

    /**
     * Get the list of all possible {@code ZmanEntry} and their status.  Modifications to the original list should be
     * reflected in the returned object.
     * @return (Ordered) {@code List} of {@code ZmanEntry}
     */
    public List<ZmanEntry> getEntries()
    {
        return Collections.unmodifiableList(Arrays.asList(entries));
    }
}