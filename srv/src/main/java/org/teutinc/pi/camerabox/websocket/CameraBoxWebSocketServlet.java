package org.teutinc.pi.camerabox.websocket;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import restx.factory.Factory;
import restx.factory.Name;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Supplier;

/**
 * @author apeyrard
 */
public class CameraBoxWebSocketServlet extends WebSocketServlet {
    private final Supplier<WebSocket> webSocketSupplier;

    @SuppressWarnings("unchecked")
    public CameraBoxWebSocketServlet() {
        webSocketSupplier = Factory.getFactory("camerabox")
                                   .transform(factory -> factory.getComponent(Name.of(Supplier.class, "Supplier<CameraBoxWebSocket>")))
                                   .or(() -> {
                                       throw new IllegalStateException("unable to get the main restx factory");
                                   });
    }

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        return webSocketSupplier.get();
    }
}
