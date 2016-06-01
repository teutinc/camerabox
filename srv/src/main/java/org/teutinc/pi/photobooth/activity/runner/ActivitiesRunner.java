package org.teutinc.pi.photobooth.activity.runner;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import org.teutinc.pi.photobooth.activity.Activity;
import org.teutinc.pi.photobooth.event.ActivityEvent;
import org.teutinc.pi.photobooth.event.EventBus;
import org.teutinc.pi.photobooth.util.lang.Nullable;
import restx.factory.AutoPreparable;
import restx.factory.Component;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * @author apeyrard
 */
@Component
public class ActivitiesRunner implements AutoPreparable {
    private final EventBus eventBus;
    private final ImmutableSet<ActivityExecutor> executors;
    private @Nullable ActivityRunner running;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @SuppressWarnings("unchecked")
    public ActivitiesRunner(EventBus eventBus, Iterable<ActivityExecutor> executors) {
        this.eventBus = eventBus;
        this.executors = ImmutableSet.copyOf(executors);
    }

    @Override
    public void prepare() {
        eventBus.register(new Object() {
            @Subscribe
            public void onActivityEvent(ActivityEvent event) {
                handleEvent(event);
            }
        });
    }

    private synchronized void handleEvent(ActivityEvent event) {
        if (running != null) {
            running.handleEvent(event);
        }
    }

    public ActivitiesRunner start(Activity activity) {
        return start(activity, RunningMode.INFINITE_LOOP);
    }

    public synchronized <A extends Activity> ActivitiesRunner start(A activity, RunningMode runningMode) {
        running().ifPresent(this::throwRunningActivity);
        executor.execute(running = runningMode.runner(activity, getExecutorFor(activity), this::dispatch));
        return this;
    }

    @SuppressWarnings("unchecked")
    private <A extends Activity> ActivityExecutor<A> getExecutorFor(A activity) {
        return (ActivityExecutor<A>) executors.stream()
                                              .filter(executor -> executor.mayExecute(activity))
                                              .findAny()
                                              .orElseThrow(() -> new IllegalStateException("unable to find any executor for: " + activity));
    }

    public synchronized State state() {
        final ActivityRunner running = this.running;
        if (running == null) {
            return State.noop();
        }
        return running.state();
    }

    public synchronized ActivitiesRunner stopCurrentActivity() {
        if (running != null) {
            final CountDownLatch stopLatch = running.stop();
            try {
                if (!stopLatch.await(2, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(format(
                            "unable to stop the activity '%s', 2s have been wait, but activity is still not finished!",
                            running.activity().getName()
                    ));
                }
            } catch (InterruptedException ignored) {}
            running = null;
        }
        dispatch(state());
        return this;
    }

    public synchronized Optional<Activity> running() {
        return Optional.ofNullable(running).map(ActivityRunner::activity);
    }

    private void throwRunningActivity(Activity running) {
        throw new IllegalStateException(
                format(
                        "activity '%s' is already running, stop it before launching a new one",
                        running.getName()
                )
        );
    }

    private State dispatch(State state) {
        eventBus.post(state);
        return state;
    }

    private interface ActivityRunner<A extends Activity> extends Runnable {
        A activity();

        State state();

        CountDownLatch stop();

        void handleEvent(ActivityEvent event);
    }

    @SuppressWarnings("unused")
    public enum RunningMode {
        ONCE {
            @Override
            <A extends Activity> ActivityRunner runner(A activity, ActivityExecutor<A> executor, StateDispatcher dispatcher) {
                return new AbstractActivityRunner<A>(activity, executor, dispatcher) {
                    @Override
                    protected void doRun() {
                        executor.run(activity, dispatcher);
                    }
                };
            }
        },
        INFINITE_LOOP {
            @Override
            <A extends Activity> ActivityRunner runner(A activity, ActivityExecutor<A> executor, StateDispatcher dispatcher) {
                return new AbstractActivityRunner<A>(activity, executor, dispatcher) {
                    @Override
                    protected void doRun() {
                        while (running) {
                            executor.run(activity, dispatcher);
                        }
                        System.out.println("finishing...");
                    }
                };
            }
        };

        abstract <A extends Activity> ActivityRunner runner(A activity, ActivityExecutor<A> executor, StateDispatcher dispatcher);
    }

    private static abstract class AbstractActivityRunner<A extends Activity> implements ActivityRunner<A> {
        final A activity;
        final ActivityExecutor<A> executor;
        final StateDispatcher dispatcher;
        volatile boolean running = false;
        final CountDownLatch stopLatch = new CountDownLatch(1);

        protected AbstractActivityRunner(A activity, ActivityExecutor<A> executor, StateDispatcher dispatcher) {
            this.activity = activity;
            this.executor = executor;
            this.dispatcher = dispatcher;
        }

        @Override
        public void run() {
            running = true;
            doRun();
            running = false;
            stopLatch.countDown();
        }

        protected abstract void doRun();

        @Override
        public CountDownLatch stop() {
            running = false;
            executor.stop();
            Thread.currentThread().interrupt();
            return stopLatch;
        }

        @Override
        public A activity() {
            return activity;
        }

        @Override
        public State state() {
            return running ? executor.state() : State.noop();
        }

        @Override
        public void handleEvent(ActivityEvent event) {
            executor.handleEvent(event);
        }
    }
}
