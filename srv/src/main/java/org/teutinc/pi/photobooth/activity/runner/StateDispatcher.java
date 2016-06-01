package org.teutinc.pi.photobooth.activity.runner;

/**
 * @author apeyrard
 */
@FunctionalInterface
public interface StateDispatcher {
    State dispatch(State state);
}
