package org.teutinc.pi.photobooth.util.lang;

import java.util.Optional;
import java.util.stream.Stream;


/**
 * @author apeyrard
 */
public class MoreStreams {

    public static <T> Stream<T> reduceOptionals(Optional<T> optional) {
        return optional.map(Stream::of).orElseGet(Stream::empty);
    }
}
