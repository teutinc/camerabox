package org.teutinc.pi.photobooth;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.teutinc.pi.photobooth.activity.Activity;
import org.teutinc.pi.photobooth.activity.runner.ActivitiesRunner;
import org.teutinc.pi.photobooth.activity.runner.State;
import org.teutinc.pi.photobooth.activity.store.ActivityStore;
import restx.factory.Component;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author apeyrard
 */
@Component
public class CameraBox {
    private final ActivityStore<?> store;
    private final ActivitiesRunner runner;

    public CameraBox(ActivityStore store, ActivitiesRunner runner) {
        this.store = store;
        this.runner = runner;
    }

    public State state() {
        return runner.state();
    }

    public Stream<RunningActivity> activities() {
        final Optional<Activity> running = runner.running();
        return store.activities()
                    .map(activity -> new RunningActivity(activity, running.filter(activity::equals).isPresent()));
    }

    public Optional<Activity> start(String id) {
        return store.findById(id)
                .flatMap(activity -> runner.start(activity).running()); // fixme ?! really, just for the 404 ?!
    }

    public Optional<Activity> stop(String id) {
        final Optional<Activity> activity = store.findById(id);
        activity.ifPresent(__ -> runner.stopCurrentActivity());
        return activity;
    }

    public Optional<Activity> activity(String id) {
        return store.findById(id);
    }

    public Activity add(Activity activity, ImmutableSet<String> contents) {
        store.store(activity, contents);
        return activity;
    }

    public Activity update(Activity activity, ImmutableSet<String> contents) {
        store.update(activity, contents);
        return activity;
    }

    public void delete(String id) {
        store.remove(id);
    }

    public static class RunningActivity {
        private final Activity activity;
        private final boolean running;

        public RunningActivity(Activity activity, boolean running) {
            this.activity = activity;
            this.running = running;
        }

        public Activity getActivity() {
            return activity;
        }

        public boolean isRunning() {
            return running;
        }
    }
}
