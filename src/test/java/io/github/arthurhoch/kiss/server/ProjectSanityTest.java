package io.github.arthurhoch.kiss.server;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectSanityTest {
    @Test
    void parserDoesNotUseForbiddenLineParsingApis() throws IOException {
        String parser = Files.readString(Path.of("src/main/java/io/github/arthurhoch/kiss/server/protocol/http11/Http11RequestParser.java"));

        assertFalse(parser.contains("String.split"));
        assertFalse(parser.contains("Scanner"));
        assertFalse(parser.contains("BufferedReader"));
        assertFalse(parser.contains("java.util.regex"));
    }

    @Test
    void workflowsMatchProjectReleaseAndDocsConventions() throws IOException {
        String ci = Files.readString(Path.of(".github/workflows/ci.yml"));
        String pages = Files.readString(Path.of(".github/workflows/pages.yml"));
        String release = Files.readString(Path.of(".github/workflows/maven-central-release.yml"));
        String dependabot = Files.readString(Path.of(".github/dependabot.yml"));

        assertTrue(ci.contains("mvn -B clean verify"));
        assertTrue(ci.contains("'17'"));
        assertTrue(ci.contains("'21'"));
        assertTrue(pages.contains("source: docs"));
        assertTrue(release.contains("MAVEN_CENTRAL_USERNAME"));
        assertTrue(release.contains("MAVEN_CENTRAL_PASSWORD"));
        assertTrue(release.contains("GPG_PRIVATE_KEY"));
        assertTrue(release.contains("GPG_PASSPHRASE"));
        assertTrue(dependabot.contains("package-ecosystem: maven"));
        assertTrue(dependabot.contains("package-ecosystem: github-actions"));
    }

    @Test
    void nativeImageDocsDoNotClaimOfficialSupport() throws IOException {
        String nativeImage = Files.readString(Path.of("docs/native-image.md"));

        assertTrue(nativeImage.contains("Do not claim official Native Image support"));
        assertFalse(nativeImage.contains("officially supported"));
    }

    @Test
    void mainSourcesDoNotDirectlyRequireJdk21OrClasspathMagic() throws IOException {
        String mainSources;
        try (var paths = Files.walk(Path.of("src/main/java"))) {
            mainSources = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(ProjectSanityTest::readUnchecked)
                    .collect(Collectors.joining("\n"));
        }

        assertFalse(mainSources.contains("Executors.newVirtualThreadPerTaskExecutor("));
        assertFalse(mainSources.contains("ServiceLoader"));
        assertFalse(mainSources.contains("sun.misc.Unsafe"));
    }

    @Test
    void androidDocsRequireActualDeviceValidationBeforeSupportClaims() throws IOException {
        String android = Files.readString(Path.of("docs/android.md"));

        assertTrue(android.contains("Actual Android instrumented validation is required"));
    }

    @Test
    void snykCodeIgnoreIsNarrowlyScopedToLoopbackTestHelper() throws IOException {
        String dcignore = Files.readString(Path.of(".dcignore"));

        assertTrue(dcignore.contains("LoopbackHttpTestClient.java"));
        assertFalse(dcignore.contains("src/main/java"));
        assertFalse(dcignore.contains("src/test/java/**"));
        assertFalse(dcignore.contains("src/test/**"));
    }

    private static String readUnchecked(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + path, e);
        }
    }
}
