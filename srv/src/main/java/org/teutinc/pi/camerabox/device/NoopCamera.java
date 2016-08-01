package org.teutinc.pi.camerabox.device;

import restx.factory.Component;

import java.nio.file.Path;

/**
 * @author apeyrard
 */
@Component(priority = 10000)
public class NoopCamera implements Camera {

    @Override
    public Recording record() {
        return new Recording() {
            @Override
            public Video stop() {
                return new Video() {
                    @Override
                    public Video moveTo(Path destination) {
                        return this;
                    }
                };
            }
        };
    }
}
