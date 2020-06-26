package org.robolectric.junit.rules;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.robolectric.android.util.concurrent.BackgroundExecutor;

/**
 * Let tests to run on background thread, if it has annotation {@link BackgroundTest}.
 *
 * <p>This is useful for testing logic that explicitly forbids being called on the main thread.
 */
public final class BackgroundTestRule implements TestRule {

  /** Annotation for tests that need to be executed in separate thread. */
  @Retention(RUNTIME)
  @Target({METHOD})
  public @interface BackgroundTest {}

  @Override
  public Statement apply(Statement base, Description description) {
    if (description.getAnnotation(BackgroundTest.class) == null) {
      return base;
    }
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        BackgroundExecutor.runInBackground(
            new Runnable() {
              @Override
              public void run() {
                try {
                  base.evaluate();
                } catch (Throwable t) {
                  throwable.set(t);
                }
              }
            });
        if (throwable.get() != null) {
          throw throwable.get();
        }
      }
    };
  }
}
