package org.teutinc.pi.photobooth.websocket;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import restx.factory.Factory;
import restx.factory.Name;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Supplier;

/**
 * @author apeyrard
 */
public class PhotoBoothWebSocketServlet extends WebSocketServlet {
    private final Supplier<WebSocket> webSocketSupplier;

    @SuppressWarnings("unchecked")
    public PhotoBoothWebSocketServlet() {
        webSocketSupplier = Factory.getFactory("photobooth")
                                   .transform(factory -> factory.getComponent(Name.of(Supplier.class, "Supplier<PhotoBoothWebSocket>")))
                                   .or(() -> {
                                       throw new IllegalStateException("unable to get the main restx factory");
                                   });
    }

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        return webSocketSupplier.get();
    }
}
