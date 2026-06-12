package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A static utility class that loads a JSON configuration file.
 */
public final class ConfigurationLoader {

  private final Path path;

  /**
   * Create a {@link ConfigurationLoader} that loads configuration from the given {@link Path}.
   */
  public ConfigurationLoader(Path path) {
    this.path = Objects.requireNonNull(path);
  }

  /**
   * Loads configuration from this {@link ConfigurationLoader}'s path
   *
   * @return the loaded {@link CrawlerConfiguration}.
   */
  public CrawlerConfiguration load() {
    // try-with-resources makes sure the file gets closed when we are done with it
    try (Reader reader = Files.newBufferedReader(path)) {
      return read(reader);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Loads crawler configuration from the given reader.
   *
   * @param reader a Reader pointing to a JSON string that contains crawler configuration.
   * @return a crawler configuration
   */
  public static CrawlerConfiguration read(Reader reader) {
    Objects.requireNonNull(reader);
    ObjectMapper objectMapper = new ObjectMapper();
    // load() already closes the reader, so tell Jackson not to close it again
    objectMapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
    try {
      return objectMapper.readValue(reader, CrawlerConfiguration.Builder.class).build();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
