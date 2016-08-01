package org.teutinc.pi.camerabox.util.lang;

/**
 * @author apeyrard
 */
public class OSUtils {
    public static boolean isArmArch() {
        return System.getProperty("os.arch").equals("arm");
    }
}
