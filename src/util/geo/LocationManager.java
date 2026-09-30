package util.geo;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages the collection of known geographic locations.
 *
 * <p>Locations are stored in a YAML file and represented in memory by {@link NewGeoData} objects.</p>
 */
public class LocationManager
{
    private static Map<String, NewGeoData> locations = new LinkedHashMap<>();

    private final Path file;

    /**
     * Creates a LocationManager using the specified data file.
     *
     * @param file location of data file
     * @throws IOException if the file cannot be read
     */
    public LocationManager(Path file) throws IOException
    {
        this.file = file;
        load();
    }

    /**
     * Loads all saved locations from the YAML file.
     *
     * <p>If the file does not exist, the manager starts with an empty collection.</p>
     *
     * @throws IOException if the file cannot be read
     */
    public void load() throws IOException
    {
        locations.clear();

        if (!Files.exists(file)) return; // break if file does not exist

        Yaml yaml = new Yaml();

        try (InputStream input = Files.newInputStream(file)) {
            Map<String, Object> root = yaml.load(input);

            if (root == null) return;

            Object locationObject = root.get("locations");

            if (!(locationObject instanceof List<?> locationList)) return;

            for (Object object : locationList)
            {
                if (!(object instanceof Map<?, ?> data)) continue; // invalid object format

                String name = (String) data.get("name");
                if (name == null || name.isBlank()) continue; // invalid name

                double latitude = ((Number) data.get("latitude")).doubleValue();
                double longitude = ((Number) data.get("longitude")).doubleValue();

                Object elevationObject = data.get("elevation");
                Double elevation = elevationObject == null ? null : ((Number) elevationObject).doubleValue();

                Object lockedObject = data.get("elevationLocked");
                boolean elevationLocked = lockedObject != null && (Boolean) lockedObject;

                String timezone = (String) data.get("timezone");

                NewGeoData location = new NewGeoData(name, latitude, longitude, elevation, elevationLocked, timezone);
                locations.put(name, location);
            }
        }
    }

    /**
     * Saves all locations to the YAML file.
     *
     * @throws IOException if the file cannot be written
     */
    public void save() throws IOException
    {
        Path parent = file.getParent();

        if (parent != null) Files.createDirectories(parent);

        Map<String, Object> root = new HashMap<>();
        List<Map<String, Object>> locationList = new ArrayList<>();

        for (NewGeoData location : locations.values())
        {
            Map<String, Object> data = new HashMap<>();
            data.put("name", location.getName());
            data.put("latitude", location.getLatitude());
            data.put("longitude", location.getLongitude());
            data.put("elevation", location.getElevation());
            data.put("elevationLocked", location.isElevationLocked());
            data.put("timezone", location.getTimezone());
            locationList.add(data);
        }

        root.put("locations", locationList);

        // write
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        Yaml yaml = new Yaml(options);

        try (Writer writer = Files.newBufferedWriter(file)) {
            yaml.dump(root, writer);
        }
    }

    /**
     * Returns the location whose name most closely matches the supplied name.
     *
     * <p>Matching is performed in the following order:</p>
     * <ol>
     *     <li>Exact match</li>
     *     <li>Case-insensitive exact match</li>
     *     <li>Name beginning with the supplied text</li>
     *     <li>Name containing the supplied text</li>
     *     <li>Closest Levenshtein match</li>
     * </ol>
     *
     * @param name location name
     * @return closest matching {@link NewGeoData}, or {@code null} if no locations exist
     */
    public NewGeoData getLocation(String name)
    {
        if (name == null || locations.isEmpty()) return null;

        String search = name.trim();

        if (search.isEmpty()) return null;

        // 1. Exact match
        NewGeoData location = locations.get(search);
        if (location != null) return null;

        // 2. Case-insensitive exact match
        String lowerSearch = search.toLowerCase(Locale.ROOT);
        for (NewGeoData candidate : locations.values())
        {
            if (candidate.getName().toLowerCase(Locale.ROOT).equals(lowerSearch))
                return candidate;
        }

        // 3. Name starts with search text
        for (NewGeoData candidate : locations.values())
        {
            if (candidate.getName().toLowerCase(Locale.ROOT).startsWith(lowerSearch))
                return candidate;
        }

        // 4. Name contains search text
        for (NewGeoData candidate : locations.values())
        {
            if (candidate.getName().toLowerCase(Locale.ROOT).contains(lowerSearch))
                return candidate;
        }

        // 5. Fall back to Levenshtein matching
        String closest = findMostSimilar(name, locations.keySet().toArray(new String[0]));
        return locations.get(closest);
    }

    /**
     * Adds or replaces a location.
     *
     * <p>The location is changed in memory.  Call {@link #save()} to persist the change to the YAML file.</p>
     *
     * @param location location to add
     */
    public void addLocation(NewGeoData location)
    {
        if (location == null)
            throw new IllegalArgumentException("Location cannot be null.");
        if (location.getName() == null || location.getName().isBlank())
            throw new IllegalArgumentException("Location must have a name.");
        locations.put(location.getName(), location);
    }

    /**
     * Removes a location by name.
     *
     * @param name name of the location
     * @return {@code true} if a location was removed
     */
    public boolean removeLocation(String name)
    {
        return locations.remove(name) != null;
    }

    /**
     * Returns {@code true} if a location with the exact name exists.
     *
     * @param name location name
     * @return {@code true} if the location exists
     */
    public boolean containsLocation(String name)
    {
        return locations.containsKey(name);
    }

    /**
     * Returns all known locations.
     *
     * @return a copy of the location list
     */
    public List<NewGeoData> getLocations()
    {
        return new ArrayList<>(locations.values());
    }

    /**
     * Return the number of known locations.
     *
     * @return number of locations
     */
    public int size()
    {
        return locations.size();
    }

    /**
     * Finds the {@link String} in the supplied array with the smallest Levenshtein distance from the input.
     *
     * @param input input string
     * @param array strings to compare against
     * @return most similar string, or {@code null} if the array is empty
     */
    public static String findMostSimilar(String input, String[] array)
    {
        if (input == null || array == null || array.length == 0) return null;

        String mostSimilar = null;
        int smallestDistance = Integer.MAX_VALUE;

        for (String candidate : array)
        {
            int distance = levenshteinDistance(input, candidate);

            if (distance < smallestDistance)
            {
                smallestDistance = distance;
                mostSimilar = candidate;
            }
        }

        return mostSimilar;
    }

    /**
     * Calculates the Levenshtein distance between two {@link String}s.
     *
     * @param s1 first string
     * @param s2 second string
     * @return Levenshtein distance
     */
    public static int levenshteinDistance(String s1, String s2)
    {
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            for (int j = 0; j <= len2; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    );
                }
            }
        }
        return dp[len1][len2];
    }
}
