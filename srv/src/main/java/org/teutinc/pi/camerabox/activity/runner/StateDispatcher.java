package org.teutinc.pi.camerabox.activity.runner;

/**
 * @author apeyrard
 */
@FunctionalInterface
public interface StateDispatcher {
    State dispatch(State state);
}
