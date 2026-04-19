package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Breaks the <code>ZmanimOptions</code> file into the proper segments for operation.
 */
public class ZmanOptionsConfigManager
{
    private static ZmanOptionsConfigManager INSTANCE = new ZmanOptionsConfigManager();

    private final List<ZmanEntry> entries = new ArrayList<>();

    private static final String filename = "ZmanimOptions.txt";

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
        String text = null;
        try {
            text = Files.readString(Path.of(filename));
        } catch (IOException e) {
            throw new RuntimeException("Error with file " + Path.of(filename) + "\n" +e);
        }
        entries.clear();
        entries.addAll(parse(text));
    }

    /**
     * Parse the text into the proper <code>ZmanEntry</code> pieces.
     * @param text file text
     * @return List of <code>ZmanEntry</code> (in order)
     */
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
            boolean enabled = Boolean.parseBoolean(lines.getLast());

            // optional description
            String description = "";
            if (lines.size() > 3)
                description = String.join("\n", lines.subList(1, lines.size() - 2));

            list.add(new ZmanEntry(title, description, methodName, enabled));
        }

        return list;
    }

    /**
     * Toggle (and save) the setting for a method between true and false.
     * @param methodName Plaintext method name for setting to be toggled.
     * @throws IOException If file was unable to be successfully written.
     */
    public void toggle(String methodName) throws IOException
    {
        for (int i = 0; i < entries.size(); i++)
        {
            ZmanEntry old = entries.get(i);

            if (old.methodName().equals(methodName))
            {
                ZmanEntry updated = new ZmanEntry(
                        old.title(), old.description(),
                        old.methodName(), !old.enabled()
                );
                entries.set(i, updated);

                rewriteFile();
                return; // break out of loop
            }
        }

        // if method not found in list
        throw new IllegalArgumentException("Method not found: " + methodName);
    }

    /**
     * Rewrite the file for storage.
     * @throws IOException If file was not written successfully.
     */
    private void rewriteFile() throws IOException
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < entries.size(); i++)
        {
            ZmanEntry e = entries.get(i);

            sb.append(e.title()).append("\n");
            if (!e.description().isBlank())
                sb.append(e.description()).append("\n");
            sb.append(e.methodName()).append("\n");
            sb.append(e.enabled()).append("\n");

            if (i < entries.size() - 1)
                sb.append("***\n");
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
