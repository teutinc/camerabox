package org.teutinc.pi.photobooth.activity.store;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.teutinc.pi.photobooth.activity.Activity;
import org.teutinc.pi.photobooth.activity.NoopActivity;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test cases for implementations of {@link ActivityStore}
 */
public abstract class AbstractActivityStoreTest<S extends ActivityStore<S>> {

    protected abstract S newStore();

    @Test
    public void should_stream_no_activities_when_no_activities_is_available_in_storage() {
        // GIVEN
        final ActivityStore<?> store = newStore();

        // WHEN
        final Stream<Activity> activities = store.activities();

        // THEN
        assertThat(activities).isEmpty();
    }


    @Test
    public void should_store_new_activities() {
        // GIVEN
        final ActivityStore<?> store = newStore();
        final Activity activity1 = new NoopActivity("A");
        final Activity activity2 = new NoopActivity("B");

        // WHEN
        store.store(activity1, ImmutableSet.of())
             .store(activity2, ImmutableSet.of());

        // THEN
        assertThat(store.activities()).containsOnly(activity1, activity2);
    }

    @Test
    public void should_find_activity_by_name() {
        // GIVEN
        final ActivityStore<?> store = newStore();
        final Activity activity1 = new NoopActivity("A");
        final Activity activity2 = new NoopActivity("B");
        store.store(activity1, ImmutableSet.of())
             .store(activity2, ImmutableSet.of());

        // WHEN
        final Optional<Activity> activity = store.findByName("A");

        // THEN
        assertThat(activity.isPresent()).isTrue();
        assertThat(activity.get()).isEqualTo(activity1);
    }

    @Test
    public void should_not_allow_to_store_duplicate_names() {
        // GIVEN
        final ActivityStore<?> store = newStore();
        final Activity activity1 = new NoopActivity("A");
        final Activity activity2 = new NoopActivity("A");
        store.store(activity1, ImmutableSet.of());

        // THEN WHEN
        assertThatThrownBy(() -> store.store(activity2, ImmutableSet.of())).isInstanceOf(IllegalStateException.class)
                                                                  .hasMessageContaining("duplicate")
                                                                  .hasMessageContaining("A");
    }

    @Test
    public void should_remove_activities() {
        // GIVEN
        final ActivityStore<?> store = newStore();
        final Activity activity1 = new NoopActivity("A");
        final Activity activity2 = new NoopActivity("B");
        store.store(activity1, ImmutableSet.of())
             .store(activity2, ImmutableSet.of());

        // WHEN
        store.remove(activity1.getId());

        // THEN
        assertThat(store.activities()).containsOnly(activity2);
    }
}