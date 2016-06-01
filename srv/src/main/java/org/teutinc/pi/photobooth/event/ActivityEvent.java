package org.teutinc.pi.photobooth.event;

/**
 * @author apeyrard
 */
@SuppressWarnings("unused")
public class ActivityEvent {
    public static final ActivityEvent pressed = new ActivityEvent();
    public static final ActivityEvent doubleClicked = new ActivityEvent();
}
