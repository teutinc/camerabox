package org.teutinc.pi.camerabox;

import com.google.common.base.Optional;
import restx.server.JettyWebServer;
import restx.server.WebServer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class can be used to run the app.
 *
 * Alternatively, you can deploy the app as a war in a regular container like tomcat or jetty.
 *
 * Reading the port from system env PORT makes it compatible with heroku.
 */
public class AppServer {
    private static final String WEB_INF_LOCATION = "src/main/webapp/WEB-INF/web.xml";
    private static final String WEB_APP_LOCATION = "src/main/webapp";

    public static void main(String[] args) throws Exception {
        final Path userDir = Paths.get(System.getProperty("user.dir"));

        System.setProperty("restx.mode", System.getProperty("restx.mode", "prod"));

        int port = Integer.valueOf(Optional.fromNullable(System.getenv("PORT")).or("8080"));
        WebServer server = new JettyWebServer(
                userDir.resolve(WEB_INF_LOCATION).toAbsolutePath().toString(),
                userDir.resolve(WEB_APP_LOCATION).toAbsolutePath().toString(),
                port,
                "0.0.0.0"
        ).setServerId("camerabox");

        server.startAndAwait();
    }
}
