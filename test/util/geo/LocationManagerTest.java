package util.geo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocationManagerTest
{
    private Path testFile;
    private LocationManager manager;

    private static final GeoData HOME = new GeoData(
            "Home",
            39.37402849068494,
            -76.6885885985364,
            128.0,
            false,
            "America/New_York"
    );

    private static final GeoData JERUSALEM = new GeoData(
            "Jerusalem, Israel",
            31.7683,
            35.2137,
            754.0,
            true,
            "Asia/Jerusalem"
    );

    @BeforeEach
    void setUp() throws IOException
    {
        testFile = Files.createTempFile(
                "location-manager-test",
                ".yaml"
        );

        Files.deleteIfExists(testFile);

        manager = new LocationManager(testFile);
    }

    @AfterEach
    void tearDown() throws IOException
    {
        Files.deleteIfExists(testFile);
    }

    @Test
    void constructorWithMissingFile()
    {
        assertEquals(0, manager.size());
        assertTrue(manager.getLocations().isEmpty());
    }

    @Test
    void addLocation()
    {
        manager.addLocation(HOME);

        assertEquals(1, manager.size());
        assertTrue(manager.containsLocation("Home"));

        GeoData result = manager.getLocation("Home");

        assertNotNull(result);
        assertEquals("Home", result.getName());
        assertEquals(HOME.getLatitude(), result.getLatitude());
        assertEquals(HOME.getLongitude(), result.getLongitude());
        assertEquals(HOME.getElevation(), result.getElevation());
        assertEquals(
                HOME.isElevationLocked(),
                result.isElevationLocked()
        );
        assertEquals(HOME.getTimezone(), result.getTimezone());
    }

    @Test
    void addLocationReplacesExistingLocation()
    {
        manager.addLocation(HOME);

        GeoData replacement = new GeoData(
                "Home",
                40.0,
                -75.0,
                500.0,
                true,
                "America/New_York"
        );

        manager.addLocation(replacement);

        assertEquals(1, manager.size());

        GeoData result = manager.getLocation("Home");

        assertNotNull(result);
        assertEquals(40.0, result.getLatitude());
        assertEquals(-75.0, result.getLongitude());
        assertEquals(500.0, result.getElevation());
        assertTrue(result.isElevationLocked());
    }

    @Test
    void addNullLocation()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> manager.addLocation(null)
        );
    }

    @Test
    void addLocationWithBlankName()
    {
        GeoData location = new GeoData(
                "",
                1.0,
                2.0,
                null,
                false,
                "UTC"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.addLocation(location)
        );
    }

    @Test
    void removeLocation()
    {
        manager.addLocation(HOME);

        assertTrue(manager.containsLocation("Home"));

        boolean removed = manager.removeLocation("Home");

        assertTrue(removed);
        assertFalse(manager.containsLocation("Home"));
        assertEquals(0, manager.size());
    }

    @Test
    void removeMissingLocation()
    {
        assertFalse(manager.removeLocation("Does Not Exist"));
    }

    @Test
    void containsLocation()
    {
        manager.addLocation(HOME);

        assertTrue(manager.containsLocation("Home"));
        assertFalse(manager.containsLocation("Jerusalem"));
    }

    @Test
    void getLocations()
    {
        manager.addLocation(HOME);
        manager.addLocation(JERUSALEM);

        List<GeoData> locations = manager.getLocations();

        assertEquals(2, locations.size());
        assertTrue(locations.contains(HOME));
        assertTrue(locations.contains(JERUSALEM));
    }

    @Test
    void getLocationsReturnsCopy()
    {
        manager.addLocation(HOME);

        List<GeoData> locations = manager.getLocations();

        locations.clear();

        assertEquals(1, manager.size());
        assertTrue(manager.containsLocation("Home"));
    }

    @Test
    void getLocationExactMatch()
    {
        manager.addLocation(HOME);
        manager.addLocation(JERUSALEM);

        GeoData result = manager.getLocation("Home");

        assertSame(HOME, result);
    }

    @Test
    void getLocationPartialName()
    {
        manager.addLocation(HOME);
        manager.addLocation(JERUSALEM);

        GeoData result = manager.getLocation("Jerusalem");

        assertSame(JERUSALEM, result);
    }

    @Test
    void getLocationCaseInsensitive()
    {
        manager.addLocation(HOME);

        GeoData result = manager.getLocation("home");

        assertSame(HOME, result);
    }

    @Test
    void getLocationContainedName()
    {
        manager.addLocation(HOME);
        manager.addLocation(JERUSALEM);

        GeoData result = manager.getLocation("Israel");

        assertSame(JERUSALEM, result);
    }

    @Test
    void getLocationFuzzyMatch()
    {
        manager.addLocation(HOME);
        manager.addLocation(JERUSALEM);

        GeoData result = manager.getLocation("Jerusalem, Isreal");

        assertSame(JERUSALEM, result);
    }

    @Test
    void getLocationReturnsNullWhenEmpty()
    {
        assertNull(manager.getLocation("Home"));
    }

    @Test
    void getLocationNullName()
    {
        manager.addLocation(HOME);

        assertNull(manager.getLocation(null));
    }

    @Test
    void getLocationBlankName()
    {
        manager.addLocation(HOME);

        assertNull(manager.getLocation(""));
        assertNull(manager.getLocation("   "));
    }

    @Test
    void saveAndLoad() throws IOException
    {
        manager.addLocation(HOME);
        manager.addLocation(JERUSALEM);

        manager.save();

        LocationManager loadedManager =
                new LocationManager(testFile);

        assertEquals(2, loadedManager.size());

        GeoData home = loadedManager.getLocation("Home");
        GeoData jerusalem =
                loadedManager.getLocation("Jerusalem, Israel");

        assertNotNull(home);
        assertNotNull(jerusalem);

        assertGeoDataEquals(HOME, home);
        assertGeoDataEquals(JERUSALEM, jerusalem);
    }

    @Test
    void saveCreatesParentDirectories() throws IOException
    {
        Path nestedDirectory = testFile
                .getParent()
                .resolve("location-manager-test");

        Path nestedFile = nestedDirectory.resolve("locations.yaml");

        try
        {
            LocationManager nestedManager =
                    new LocationManager(nestedFile);

            nestedManager.addLocation(HOME);
            nestedManager.save();

            assertTrue(Files.exists(nestedFile));
        }
        finally
        {
            Files.deleteIfExists(nestedFile);
            Files.deleteIfExists(nestedDirectory);
        }
    }

    @Test
    void nullElevationIsPreserved() throws IOException
    {
        GeoData location = new GeoData(
                "Unknown Elevation",
                39.0,
                -76.0,
                null,
                false,
                "America/New_York"
        );

        manager.addLocation(location);
        manager.save();

        LocationManager loadedManager =
                new LocationManager(testFile);

        GeoData loaded =
                loadedManager.getLocation("Unknown Elevation");

        assertNotNull(loaded);
        assertNull(loaded.getElevation());
        assertFalse(loaded.isElevationLocked());
    }

    @Test
    void elevationLockedIsPreserved() throws IOException
    {
        manager.addLocation(JERUSALEM);
        manager.save();

        LocationManager loadedManager =
                new LocationManager(testFile);

        GeoData loaded =
                loadedManager.getLocation("Jerusalem, Israel");

        assertNotNull(loaded);
        assertEquals(754.0, loaded.getElevation());
        assertTrue(loaded.isElevationLocked());
    }

    @Test
    void saveEmptyManager() throws IOException
    {
        manager.save();

        assertTrue(Files.exists(testFile));

        LocationManager loadedManager =
                new LocationManager(testFile);

        assertEquals(0, loadedManager.size());
    }

    @Test
    void levenshteinDistance()
    {
        assertEquals(
                0,
                LocationManager.levenshteinDistance(
                        "Home",
                        "Home"
                )
        );

        assertEquals(
                1,
                LocationManager.levenshteinDistance(
                        "Home",
                        "Hom"
                )
        );

        assertEquals(
                1,
                LocationManager.levenshteinDistance(
                        "Home",
                        "home"
                )
        );

        assertEquals(
                3,
                LocationManager.levenshteinDistance(
                        "kitten",
                        "sitting"
                )
        );
    }

    @Test
    void findMostSimilar()
    {
        String[] locations = {
                "Home",
                "Pikesville, MD",
                "Jerusalem, Israel",
                "Sydney, Australia"
        };

        assertEquals(
                "Home",
                LocationManager.findMostSimilar(
                        "Hom",
                        locations
                )
        );

        assertEquals(
                "Jerusalem, Israel",
                LocationManager.findMostSimilar(
                        "Jerusalem, Isreal",
                        locations
                )
        );
    }

    /**
     * Compares all fields of two NewGeoData objects.
     */
    private void assertGeoDataEquals(
            GeoData expected,
            GeoData actual)
    {
        assertEquals(
                expected.getName(),
                actual.getName()
        );

        assertEquals(
                expected.getLatitude(),
                actual.getLatitude()
        );

        assertEquals(
                expected.getLongitude(),
                actual.getLongitude()
        );

        assertEquals(
                expected.getElevation(),
                actual.getElevation()
        );

        assertEquals(
                expected.isElevationLocked(),
                actual.isElevationLocked()
        );

        assertEquals(
                expected.getTimezone(),
                actual.getTimezone()
        );
    }
}
