package org.teutinc.pi.camerabox.button;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Coalescor in order to manage click and double click.
 *
 * If two clicks are close, they will be transformed into a double click.
 *
 * @author apeyrard
 */
public class ButtonEventCoalescor {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    private final EventHolder eventHolder = new EventHolder();
    private final Runnable onClick;
    private final Runnable onDoubleClick;
    private final long doubleClickDelay;
    private final TimeUnit doubleClickDelayUnit;

    public ButtonEventCoalescor(Runnable onClick, Runnable onDoubleClick, long doubleClickDelay, TimeUnit doubleClickDelayUnit) {
        this.onClick = onClick;
        this.onDoubleClick = onDoubleClick;
        this.doubleClickDelay = doubleClickDelay;
        this.doubleClickDelayUnit = doubleClickDelayUnit;
    }

    public void click() {
        synchronized (eventHolder) {
            if (eventHolder.future != null && !eventHolder.future.isDone() && eventHolder.future.cancel(true)) {
                // we manage to cancel the previous click, that was not sent, so send a double click instead
                eventHolder.future = null;
                onDoubleClick.run();
            } else {
                // no previous click was recorded
                eventHolder.future = executor.schedule(
                        () -> {
                            synchronized (eventHolder) {
                                if (eventHolder.future != null) {
                                    // click has not been canceled, so it has not been grouped, with another click
                                    onClick.run();
                                }
                            }
                        },
                        doubleClickDelay, doubleClickDelayUnit);
            }
        }
    }

    private static class EventHolder {
        private volatile ScheduledFuture<?> future;
    }
}
