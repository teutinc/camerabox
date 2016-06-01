package org.teutinc.pi.photobooth.activity.runner;

import org.junit.Test;
import org.teutinc.pi.photobooth.activity.AbstractActivity;
import org.teutinc.pi.photobooth.activity.Activity;
import org.teutinc.pi.photobooth.event.EventBus;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test cases for {@link ActivitiesRunner}.
 */
public class ActivitiesRunnerTest {

     @Test
    public void should_run_activity() {
        // GIVEN
        ActivitiesRunner runner = new ActivitiesRunner(mock(EventBus.class), Collections.singleton(new NoopExecutor()));
        final RunnableTestActivity test = new RunnableTestActivity("test");

        try {
            // WHEN
            runner.start(test);
            final Optional<Activity> running = runner.running();

            // THEN
            assertThat(running.isPresent()).isTrue();
            //noinspection OptionalGetWithoutIsPresent
            assertThat(running.get()).isEqualTo(test);
        } finally {
            runner.stopCurrentActivity();
        }
    }

    private static class RunnableTestActivity extends AbstractActivity {

        RunnableTestActivity(String name) {
            super(name);
        }
    }

    private static class NoopExecutor implements ActivityExecutor<Activity> {

        @Override
        public <O extends Activity> boolean mayExecute(O activity) {
            return true;
        }

        @Override
        public void run(Activity activity, StateDispatcher dispatcher) {

        }

        @Override
        public State state() {
            return State.noop();
        }

        @Override
        public void stop() {

        }

    }
}