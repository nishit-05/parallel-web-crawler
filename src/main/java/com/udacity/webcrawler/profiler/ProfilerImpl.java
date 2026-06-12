package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);
    Objects.requireNonNull(delegate);

    // If the class has no @Profiled methods, there is nothing to time, so fail early.
    if (!hasProfiledMethod(klass)) {
      throw new IllegalArgumentException(
          klass.getName() + " does not contain any methods annotated with @Profiled");
    }

    // Wrap the real object in a proxy so every method call goes through our interceptor first.
    @SuppressWarnings("unchecked")
    T proxy =
        (T)
            Proxy.newProxyInstance(
                klass.getClassLoader(),
                new Class<?>[] {klass},
                new ProfilingMethodInterceptor(clock, delegate, state));
    return proxy;
  }

  private static boolean hasProfiledMethod(Class<?> klass) {
    return Arrays.stream(klass.getDeclaredMethods())
        .anyMatch(method -> method.isAnnotationPresent(Profiled.class));
  }

  @Override
  public void writeData(Path path) {
    Objects.requireNonNull(path);
    // CREATE makes the file if it is not there, and APPEND adds to it instead of erasing it.
    try (Writer writer =
        Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
      writeData(writer);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
