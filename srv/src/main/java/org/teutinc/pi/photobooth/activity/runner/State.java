package org.teutinc.pi.photobooth.activity.runner;

/**
 * @author apeyrard
 */
public interface State {
    String getActivityId();

    String getActivity();

    State fresh();

    static State noop() {
        return NoopState.INSTANCE;
    }

    class NoopState implements State {
        static final State INSTANCE = new NoopState();

        @Override
        public String getActivityId() {
            return "noop";
        }

        @Override
        public String getActivity() {
            return "noop";
        }

        @Override
        public State fresh() {
            return this;
        }
    }
}
