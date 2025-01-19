package org.alex_hashtag.buildSystem;

import org.junit.jupiter.api.Test;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class RainforestTest {

    @Test
    void testValidConfiguration() {
        String path = "test_project/rainforest.toml";
        Rainforest rainforest = new Rainforest(path);

        // Validate Project
        assertNotNull(rainforest.project);
        assertEquals("test_project", rainforest.project.root());
        assertEquals(Path.of("src/org/alex_hashtag/Main.toucan"), rainforest.project.main());
        assertEquals(new GenericVersion(1, 0, 0), rainforest.project.version());

        // Validate Metadata
        assertNotNull(rainforest.metadata);
        assertEquals("Toucan Test Project", rainforest.metadata.name());
        assertEquals("A project to test the compilation of Toucan projects", rainforest.metadata.description());
        assertArrayEquals(new String[]{"Alexander"}, rainforest.metadata.authors());
        assertEquals(1, rainforest.metadata.emails().length);
        assertEquals("alex_hashtag@toucan.wiki", rainforest.metadata.emails()[0].getAddress());
        assertNotNull(rainforest.metadata.website());
        assertEquals("https://toucan.wiki", rainforest.metadata.website().toString());

        // Validate Build
        assertNotNull(rainforest.build);
        assertEquals(Optimization.O3, rainforest.build.optimization());
        assertEquals(Path.of("build/output"), rainforest.build.executable());

        // Validate Dependencies
        assertNotNull(rainforest.dependencies);
        assertEquals(2, rainforest.dependencies.size());
        assertEquals("alex_hashtag", rainforest.dependencies.get(0).publisher());
    }

    @Test
    void testMissingOptionalFields() {
        String path = "src/test/resources/optional_missing_rainforest.toml";
        Rainforest rainforest = new Rainforest(path);

        // Validate Metadata defaults
        assertEquals("Unnamed Project", rainforest.metadata.name());
        assertEquals("", rainforest.metadata.description());
        assertArrayEquals(new String[]{}, rainforest.metadata.authors());
        assertArrayEquals(new InternetAddress[]{}, rainforest.metadata.emails());

        // Validate Dependencies defaults
        assertNotNull(rainforest.dependencies);
        assertTrue(rainforest.dependencies.isEmpty());

        // Validate Build defaults
        assertEquals(Optimization.O0, rainforest.build.optimization());
    }



    @Test
    void testDependencyParsing() {
        Dependency dependency = Dependency.parse("alex_hashtag::example_dependency::1.2.3");
        assertEquals("alex_hashtag", dependency.publisher());
        assertEquals("example_dependency", dependency.project());
        assertEquals(new GenericVersion(1, 2, 3), dependency.version());
    }

    @Test
    void testInvalidDependencyParsing() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> Dependency.parse("invalid_dependency"));
        assertTrue(exception.getMessage().contains("Dependency must be in the format"));
    }
}
