package org.teutinc.pi.photobooth.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.Subscribe;
import org.eclipse.jetty.websocket.WebSocket;
import org.slf4j.Logger;
import org.teutinc.pi.photobooth.CameraBox;
import org.teutinc.pi.photobooth.activity.runner.State;
import org.teutinc.pi.photobooth.event.ActivityEvent;
import org.teutinc.pi.photobooth.event.EventBus;
import restx.factory.AutoPreparable;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author apeyrard
 */
@Module
public class PhotoBoothWebSocketModule {

    @Provides @Named("Supplier<PhotoBoothWebSocket>")
    public Supplier webSocketSupplier(WebSocketDispatcher dispatcher) {
        return dispatcher::newWebSocket;
    }

    @Provides
    public WebSocketDispatcher dispatcher(EventBus eventBus, ObjectMapper mapper, CameraBox cameraBox) {
        return new WebSocketDispatcher(eventBus, mapper, cameraBox);
    }

    static class WebSocketDispatcher implements AutoPreparable {
        private static final Logger logger = getLogger(WebSocketDispatcher.class);

        private final List<PhotoBoothWebSocket> sockets = new CopyOnWriteArrayList<>();
        private final EventBus eventBus;
        private final ObjectMapper mapper;
        private final CameraBox cameraBox;

        public WebSocketDispatcher(EventBus eventBus, ObjectMapper mapper, CameraBox cameraBox) {
            this.eventBus = eventBus;
            this.mapper = mapper;
            this.cameraBox = cameraBox;
        }

        @Override
        public void prepare() {
            eventBus.register(new Object() {
                @Subscribe
                public void onWatchEvent(State event) {
                    final Iterator<PhotoBoothWebSocket> iterator = sockets.iterator();
                    if (iterator.hasNext()) {
                        try {
                            String message = serialize(event);
                            iterator.forEachRemaining(socket -> socket.send(message));
                        } catch (JsonProcessingException e) {
                            logger.error("unable to serialize event: " + event, e);
                        }
                    }
                }
            });
        }

        public WebSocket.OnTextMessage newWebSocket() {
            return new PhotoBoothWebSocket();
        }

        private void register(PhotoBoothWebSocket webSocket) {
            sockets.add(webSocket);
        }

        private void unregister(PhotoBoothWebSocket webSocket) {
            sockets.remove(webSocket);
        }

        private String serialize(Object message) throws JsonProcessingException {
            return mapper.writeValueAsString(message);
        }

        private class PhotoBoothWebSocket implements WebSocket.OnTextMessage {
            private volatile WebSocket.Connection connection;

            @Override
            public void onOpen(Connection connection) {
                logger.info("[@{}] opening web socket connection: {}", hashCode(), connection);
                this.connection = connection;
                register(this);
                State state = cameraBox.state();
                try {
                    send(serialize(state));
                } catch (JsonProcessingException e) {
                    logger.error("unable to serialize state: " + state, e);
                }
            }

            @Override
            public void onMessage(String message) {
//                logger.info("[@{}] dropping incoming message: '{}'", hashCode(), message);

                // fixme
                logger.info("[@{}] receive incoming message: '{}'", hashCode(), message);
                if ("click".equals(message)) {
                    eventBus.post(ActivityEvent.pressed);
                } else if ("double-click".equals(message)) {
                    eventBus.post(ActivityEvent.doubleClicked);
                } else {
                    logger.info("[@{}] dropping incoming message: '{}'", hashCode(), message);
                }
            }

            public void send(String message) {
                final Connection connection = this.connection;
                if (connection != null) {
                    logger.info("[@{}] sending message: {}", hashCode(), message);
                    try {
                        this.connection.sendMessage(message);
                    } catch (Exception e) {
                        logger.error("[@{}] unable to send message '{}', due to unexpected exception {}", hashCode(), message, e);
                    }
                } else {
                    logger.error("[@{}] unable to send message '{}', there is no active connection", hashCode(), message);
                }
            }

            @Override
            public void onClose(int closeCode, String message) {
                logger.info("[@{}] closing {}. Code {}, message {}", hashCode(), connection, closeCode, message);
                unregister(this);
                connection = null;
            }
        }
    }


}

