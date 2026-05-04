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
 * Breaks the <code>ZmanimOptions</code> file into the proper segments for operation.
 */
public class ZmanOptionsConfigManager
{
    private static ZmanOptionsConfigManager INSTANCE = new ZmanOptionsConfigManager();

    private final List<ZmanEntry> entries = new ArrayList<>();

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
        try {
            List<String> lines = Files.readAllLines(Path.of(filename));
            entries.clear();

            Set<Zman> seen = new HashSet<>();

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

                Method method;
                try
                {
                    method = ComplexZmanimCalendar.class.getMethod(zman.getMethodName());
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Failed attempting call of method " + zman.getMethodName() + " for " +
                            zman, e);
                }

                entries.add(new ZmanEntry(zman, method, enabled));
                seen.add(zman);
            }

            // Append any missing enum values (to keep system stable if enum grows)
            for (Zman z : Zman.values())
                if (!seen.contains(z))
                    entries.add(new ZmanEntry(z, null, false)); // add as disabled
        } catch (IOException e) {
            throw new RuntimeException("Error reading config file", e);
        }
    }

    /**
     * Toggle (and save) the setting for a method between true and false.
     * @param zman the {@code Zman} for the setting to be toggled.
     * @throws IOException If file was unable to be successfully written.
     */
    public void toggle(Zman zman) throws IOException
    {
        for (int i = 0; i < entries.size(); i++)
        {
            ZmanEntry e = entries.get(i);

            if (e.zman() == zman)
            {
                entries.set(i, new ZmanEntry(zman, e.method(), !e.enabled()));

                rewriteFile();
                return; // break out of loop
            }
        }

        // if method not found in list
        throw new IllegalArgumentException("Zman not found: " + zman);
    }

    /**
     * Rewrite the file for storage.
     * @throws IOException If file was not written successfully.
     */
    private void rewriteFile() throws IOException
    {
        StringBuilder sb = new StringBuilder();

        for (ZmanEntry e : entries)
        {
            Zman z = e.zman();

            sb.append("# ").append(z.getTitle()).append("\n"); // sets comment as title; change?
            sb.append(z.getId()).append(",").append(e.enabled()).append("\n\n");
        }

        Files.writeString(Path.of(filename), sb.toString());
    }

    /**
     * Get the list of all possible <code>ZmanEntry</code> and their status.
     * @return (Ordered) <code>List</code> of <code>ZmanEntry</code>
     */
    public List<ZmanEntry> getEntries()
    {
        return Collections.unmodifiableList(entries);
    }
}