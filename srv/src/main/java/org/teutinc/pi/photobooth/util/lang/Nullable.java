package org.teutinc.pi.photobooth.util.lang;

import java.util.Optional;

/**
 * Just to provides more readable API, and mark nullable things.
 * This annotation should only be used on private API, as public API should not allow null, and
 * use {@link Optional}.
 *
 * @author apeyrard
 */
public @interface Nullable {
}
