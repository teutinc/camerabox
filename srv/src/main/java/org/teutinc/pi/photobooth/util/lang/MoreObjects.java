package org.teutinc.pi.photobooth.util.lang;

import java.util.Objects;

/**
 * @author apeyrard
 */
public class MoreObjects {
    private MoreObjects() {}

    public static boolean notEquals(Object a, Object b) {
        return !Objects.equals(a, b);
    }
}
