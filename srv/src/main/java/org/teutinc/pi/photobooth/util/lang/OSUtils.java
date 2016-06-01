package org.teutinc.pi.photobooth.util.lang;

/**
 * @author apeyrard
 */
public class OSUtils {
    public static boolean isArmArch() {
        return System.getProperty("os.arch").equals("arm");
    }
}
