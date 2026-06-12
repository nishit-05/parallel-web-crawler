package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object delegate;
  private final ProfilingState state;

  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState state) {
    this.clock = Objects.requireNonNull(clock);
    this.delegate = Objects.requireNonNull(delegate);
    this.state = Objects.requireNonNull(state);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // Don't profile the equals() method, otherwise the proxy could break normal equality checks.
    if (method.getDeclaringClass().equals(Object.class) && method.getName().equals("equals")) {
      return method.invoke(delegate, args);
    }

    // Only methods marked with @Profiled get timed.
    boolean profiled = method.isAnnotationPresent(Profiled.class);
    Instant start = profiled ? clock.instant() : null;
    try {
      // Call the real method and give back whatever it returns.
      return method.invoke(delegate, args);
    } catch (InvocationTargetException e) {
      // The real method threw something. Throw the original exception, not the reflection wrapper.
      throw e.getTargetException();
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } finally {
      // Record the time even if the method threw an exception.
      if (profiled) {
        Duration elapsed = Duration.between(start, clock.instant());
        state.record(delegate.getClass(), method, elapsed);
      }
    }
  }
}
