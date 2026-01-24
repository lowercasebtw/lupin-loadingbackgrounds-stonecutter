package dev.foxgirl.loadingbackgrounds.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public record LoadingBackgroundsConfig(
    double secondsStay,
    double secondsFade,
    float brightness,
    @NotNull Position position
) {
    private static final Logger LOGGER = LogManager.getLogger(LoadingBackgroundsConfig.class);
    private static final Gson GSON =
        new GsonBuilder()
            .disableHtmlEscaping()
            .serializeNulls()
            .setPrettyPrinting()
            .setLenient()
            .create();

    private static final String DEFAULT_JSON = """
        // Loading Backgrounds configuration JSON file
        {
          // Amount of time that each background is displayed for
          "secondsStay": 5.0,

          // Amount of time it takes to fade between backgrounds
          "secondsFade": 0.5,

          // Background brightness, between 0.0 and 1.0
          "brightness": 1.0,

          // Level loading indicator position
          // One of "CENTER", "BOTTOM_LEFT", "BOTTOM_RIGHT", "TOP_LEFT", or "TOP_RIGHT"
          "position": "BOTTOM_RIGHT"
        }
        """;

    public static final LoadingBackgroundsConfig DEFAULT = GSON.fromJson(DEFAULT_JSON, LoadingBackgroundsConfig.class);

    public static @NotNull LoadingBackgroundsConfig read(@NotNull final Path pathConfigDirectory) {
        final Path pathFile = pathConfigDirectory.resolve("loadingbackgrounds-config.json");
        final Path pathTemp = pathConfigDirectory.resolve("loadingbackgrounds-config.json.tmp");
        try (final BufferedReader reader = Files.newBufferedReader(pathFile)) {
            return GSON.fromJson(reader, LoadingBackgroundsConfig.class);
        } catch (final NoSuchFileException exception) {
            LOGGER.warn("Failed to read config, file not found");
        } catch (final IOException exception) {
            LOGGER.error("Failed to read config, IO error", exception);
        } catch (final JsonParseException exception) {
            LOGGER.error("Failed to read config, JSON error", exception);
        } catch (final Exception exception) {
            LOGGER.error("Failed to read config", exception);
        }

        try {
            Files.writeString(pathTemp, DEFAULT_JSON);
            Files.move(pathTemp, pathFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException exception) {
            LOGGER.error("Failed to write new config, IO error", exception);
        } catch (final Exception exception) {
            LOGGER.error("Failed to write new config", exception);
        }

        return DEFAULT;
    }
}
