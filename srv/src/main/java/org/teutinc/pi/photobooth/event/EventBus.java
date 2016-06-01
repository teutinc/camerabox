package org.teutinc.pi.photobooth.event;

import org.slf4j.Logger;
import restx.factory.Component;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author apeyrard
 */
@Component
public class EventBus {
    private static final Logger logger = getLogger(EventBus.class);

    private final com.google.common.eventbus.EventBus delegate;

    public EventBus() {
        delegate = new com.google.common.eventbus.EventBus();
    }

    public void register(Object listener) {
        delegate.register(listener);
    }

    public void post(Object event) {
        logger.debug("post event: {}", event);
        delegate.post(event);
    }
}
