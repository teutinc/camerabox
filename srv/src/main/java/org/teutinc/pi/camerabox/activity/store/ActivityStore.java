package org.teutinc.pi.camerabox.activity.store;

import com.google.common.collect.ImmutableSet;
import org.teutinc.pi.camerabox.activity.Activity;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author apeyrard
 */
public interface ActivityStore<S extends ActivityStore> {
    S store(Activity activity, ImmutableSet<String> contents);

    S update(Activity activity, ImmutableSet<String> contents);

    S remove(String name);

    Stream<Activity> activities();

    Optional<Activity> findById(String id);

    default Activity getById(String id) {
        return findById(id).orElseThrow(IllegalArgumentException::new);
    }

    Optional<Activity> findByName(String name);
}
