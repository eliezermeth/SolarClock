package util;

import java.util.HashMap;
import java.util.Map;

public class Regions
{
    private static Map<String, GeoData> locations = new HashMap<>();

    // static initializer block to populate data
    static
    {
        GeoData[] places = new GeoData[] {
                new GeoData("Pikesville, MD", 39.37427, -76.72247, "America/New_York"),
                new GeoData("New York City, NY", 40.7128, -74.0060, "America/New_York"),
                new GeoData("Jerusalem, Israel", 31.7683, 35.2137, "Asia/Jerusalem"),
                new GeoData("Sydney, Australia", -33.8688, 151.2093, "Australia/Sydney"),
                new GeoData("Southfield, MI", 42.4734, -83.2219, "America/Detroit"),
                new GeoData("Chicago, Il", 41.8781, -87.6298, "America/Chicago")
        };

        for (GeoData p : places)
        {
            locations.put(p.name, p);
        }
    }

    public static GeoData getLocation(String location)
    {
        String closest = findMostSimilar(location, locations.keySet().toArray(new String[0]));
        return locations.get(closest);
    }

    // Levenshtein distance calculation
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

    // Find the most similar string
    public static String findMostSimilar(String input, String[] array)
    {
        String mostSimilar = null;
        int smallestDistance = Integer.MAX_VALUE;

        for (String s : array) {
            int distance = levenshteinDistance(input, s);
            if (distance < smallestDistance) {
                smallestDistance = distance;
                mostSimilar = s;
            }
        }
        return mostSimilar;
    }


}
