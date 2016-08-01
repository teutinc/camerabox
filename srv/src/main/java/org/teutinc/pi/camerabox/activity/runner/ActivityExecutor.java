package org.teutinc.pi.camerabox.activity.runner;

import org.teutinc.pi.camerabox.activity.Activity;
import org.teutinc.pi.camerabox.event.ActivityEvent;

/**
 * @author apeyrard
 */
public interface ActivityExecutor<A extends Activity> {

    <O extends Activity> boolean mayExecute(O activity);

    void run(A activity, StateDispatcher dispatcher);

    State state();

    void stop();

    default void handleEvent(ActivityEvent event) {}

    abstract class ForType<A extends Activity> implements ActivityExecutor<A> {
        private final Class<A> activityType;

        protected ForType(Class<A> activityType) {
            this.activityType = activityType;
        }

        @Override
        public <O extends Activity> boolean mayExecute(O activity) {
            return activityType.isInstance(activity);
        }
    }
}