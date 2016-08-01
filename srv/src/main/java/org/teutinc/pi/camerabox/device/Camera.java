package org.teutinc.pi.camerabox.device;

import java.nio.file.Path;

/**
 * @author apeyrard
 */
public interface Camera {

    /**
     * Starts a video recording.
     *
     * @return A handler on the started record.
     */
    Recording record();

    interface Recording {
        Video stop();
    }

    interface Video {
        Video moveTo(Path destination);
    }
}
