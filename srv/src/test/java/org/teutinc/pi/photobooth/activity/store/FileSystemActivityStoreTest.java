package org.teutinc.pi.photobooth.activity.store;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.teutinc.pi.photobooth.activity.Activity;
import org.teutinc.pi.photobooth.activity.NoopActivity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static com.sun.deploy.cache.Cache.exists;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test cases for {@link FileSystemActivityStore}.
 */
public class FileSystemActivityStoreTest extends AbstractActivityStoreTest<FileSystemActivityStore> {

    private Path workingTempDir;
    private Path contentTempDir;

    @Before
    public void initTempDir() throws IOException {
        workingTempDir = Files.createTempDirectory("somewhere");
        contentTempDir = Files.createTempDirectory("temp");
    }

    @After
    public void cleanTempDir() throws IOException {
        FileUtils.deleteDirectory(workingTempDir.toFile());
        FileUtils.deleteDirectory(contentTempDir.toFile());
    }

    @Override
    protected FileSystemActivityStore newStore() {
        return FileSystemActivityStore.in(workingTempDir.toString(), contentTempDir.toString());
    }

    @Test
    public void should_persist_stored_activities() {
        // GIVEN
        final FileSystemActivityStore firstStore = newStore();
        Activity activity1 = new NoopActivity("A");
        Activity activity2 = new NoopActivity("B");
        firstStore.store(activity1, ImmutableSet.of())
                  .store(activity2, ImmutableSet.of());
        final FileSystemActivityStore secondStore = newStore();

        // WHEN
        final Stream<Activity> activities = secondStore.activities();

        // THEN
        assertThat(activities).containsOnly(activity1, activity2);
    }

    @Test
    public void should_move_contents_to_activity_path() throws IOException {
        // GIVEN
        com.google.common.io.Files.touch(contentTempDir.resolve("first.png").toFile());
        com.google.common.io.Files.touch(contentTempDir.resolve("second.jpg").toFile());

        final FileSystemActivityStore firstStore = newStore();
        Activity activity1 = new NoopActivity("A");

        // WHEN
        firstStore.store(activity1, ImmutableSet.of("first.png", "second.jpg"));

        // THEN
        assertThat(workingTempDir.resolve(activity1.getId()).resolve("first.png").toFile()).exists();
        assertThat(workingTempDir.resolve(activity1.getId()).resolve("second.jpg").toFile()).exists();
        assertThat(contentTempDir.resolve("first.png").toFile()).doesNotExist();
        assertThat(contentTempDir.resolve("second.jpg").toFile()).doesNotExist();
    }
}