package org.teutinc.pi.camerabox.activity.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.teutinc.pi.camerabox.activity.Activity;
import org.teutinc.pi.camerabox.util.lang.MoreStreams;
import org.teutinc.pi.camerabox.util.lang.Nullable;
import restx.factory.Component;

import javax.inject.Named;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.teutinc.pi.camerabox.util.lang.MoreObjects.notEquals;

/**
 * @author apeyrard
 */
@Component
public class FileSystemActivityStore implements ActivityStore<FileSystemActivityStore> {
    private static final Logger logger = getLogger(FileSystemActivityStore.class);

    public static FileSystemActivityStore in(String root, String tmpContentPath) {
        return in(root, tmpContentPath, null);
    }

    public static FileSystemActivityStore in(String root, String tmpContentPath, ObjectMapper mapper) {
        return new FileSystemActivityStore(root, tmpContentPath, mapper);
    }

    private final Path root;
    private final Path tmpContentPath;
    private final ObjectMapper mapper;


    private volatile ImmutableMap<String, Activity> activities;
    private ImmutableMap<String, Activity> getActivities() { return activities; }
    private void setActivities(ImmutableMap<String, Activity> activities) { this.activities = activities; }
    private final ReentrantLock lock = new ReentrantLock();

    FileSystemActivityStore(@Named("camerabox.activity.storePath") String root,
                            @Named("camerabox.upload.tempPath") String tmpContentPath,
                            @Nullable ObjectMapper mapper) {
        this.root = Paths.get(requireNonNull(root));
        this.tmpContentPath = Paths.get(requireNonNull(tmpContentPath));
        this.mapper = mapper != null ? mapper : new ObjectMapper();
        setActivities(readPersisted(this.root));
    }

    private ImmutableMap<String, Activity> readPersisted(Path root) {

        // fixme temp for tests...
//        final QuestionBoxActivity activity = new QuestionBoxActivity(
//                UUID.fromString("8bd0db33-0cc2-4fd3-bb71-ae24512891eb").toString().substring(0, 8),
//                "some questions",
//                new QuestionBoxActivity.Welcome(new QuestionBoxActivity.SingleTextTemplate("Click when ready...")),
//                ImmutableList.of(
//                        new QuestionBoxActivity.Question(5000, new QuestionBoxActivity.SingleTextTemplate("Describe yourself...")),
//                        new QuestionBoxActivity.Question(20000, new QuestionBoxActivity.SingleTextTemplate("what is your favorite player?")),
//                        new QuestionBoxActivity.Question(30000, new QuestionBoxActivity.TextAndImageTemplate("what do you think about him?", "pogba.jpg"))
//                ),
//                Optional.of(new QuestionBoxActivity.Bye(10000, new QuestionBoxActivity.SingleTextTemplate("that's it. thanks. see you")))
//        );
//
//        final QuestionBoxActivity activity2 = new QuestionBoxActivity(
//                UUID.fromString("73d12c18-b495-4e18-af38-1bdfe460ca21").toString().substring(0, 8),
//                "quelques questions...",
//                new QuestionBoxActivity.Welcome(new QuestionBoxActivity.SingleTextTemplate("Appuyez sur le bouton pour commencer...")),
//                ImmutableList.of(
//                        new QuestionBoxActivity.Question(5000, new QuestionBoxActivity.SingleTextTemplate("Quel est votre prénom ?")),
//                        new QuestionBoxActivity.Question(5000, new QuestionBoxActivity.SingleTextTemplate("Quelle est votre couleur préférée ?")),
//                        new QuestionBoxActivity.Question(5000, new QuestionBoxActivity.SingleTextTemplate("Chat ou Chien ?")),
//                        new QuestionBoxActivity.Question(5000, new QuestionBoxActivity.SingleTextTemplate("Sinon il fait beau ?"))
//                ),
//                Optional.of(new QuestionBoxActivity.Bye(10000, new QuestionBoxActivity.SingleTextTemplate("Merci pour votre patience et à bientôt.")))
//        );
//
//        return ImmutableMap.of(
//                activity.getId(), activity,
//                activity2.getId(), activity2
//        );
        // fixme remove comment...

        try (Stream<Path> subPaths = Files.list(root)) {
            return ImmutableMap.copyOf(
                    subPaths.filter(Files::isDirectory)
                            .map(this::readPersistedActivity)
                            .flatMap(MoreStreams::reduceOptionals)
                            .collect(Collectors.toMap(Activity::getId, Function.identity()))
            );
        } catch (NoSuchFileException ignored) {
            logger.debug("store does not exists (yet): {}", root);
            return ImmutableMap.of();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Optional<Activity> readPersistedActivity(Path activityPath) {
        final Path activityFilePath = activityPath.resolve("activity.json");
        if (Files.exists(activityFilePath)) {
            try {
                return Optional.of(mapper.readValue(activityFilePath.toFile(), Activity.class));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public FileSystemActivityStore store(Activity activity, ImmutableSet<String> contents) {
        findByName(activity.getName()).ifPresent(this::throwDuplicateException);
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            findByName(activity.getName()).ifPresent(this::throwDuplicateException);
            Path activityPath = doStoreOnFS(activity);
            moveContents(contents, tmpContentPath, activityPath);
            setActivities(
                    ImmutableMap.<String, Activity>builder()
                            .putAll(getActivities())
                            .put(activity.getId(), activity)
                            .build()
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            lock.unlock();
        }
        return this;
    }

    @Override
    public FileSystemActivityStore update(Activity activity, ImmutableSet<String> contents) {
        if (findById(activity.getId()).isPresent()) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                if (findById(activity.getId()).isPresent()) {
                    remove(activity.getId(), false);
                    store(activity, contents);
                }
            } finally {
                lock.unlock();
            }
        }
        return this;
    }

    /*
        internal method for the 'store' method, this method is not guarded, and should never be called directly,
        it is separated from the 'store' method to improve readability, and nothing else!
     */
    private Path doStoreOnFS(Activity activity) throws IOException {
        final Path activityDir = Files.createDirectories(root.resolve(generateActivityDirName(activity)));
        try (Writer writer = Files.newBufferedWriter(root.resolve(activityDir).resolve("activity.json"), UTF_8)) {
            mapper.writeValue(writer, activity);
        }
        return activityDir;
    }

    @Override
    public FileSystemActivityStore remove(String id) {
        return remove(id, true);
    }

    public FileSystemActivityStore remove(String id, boolean removeOnDisk) {
        findById(id).ifPresent(activity -> {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                if (removeOnDisk) doRemoveOnFS(activity);
                setActivities(
                        ImmutableMap.copyOf(
                                activities()
                                        .filter(entry -> notEquals(entry.getId(), id))
                                        .collect(Collectors.toMap(Activity::getId, Function.identity()))
                        )
                );
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                lock.unlock();
            }
        });

        return this;
    }

    private void doRemoveOnFS(Activity activity) throws IOException {
        FileUtils.deleteDirectory(root.resolve(generateActivityDirName(activity)).toFile());
    }

    private void moveContents(ImmutableSet<String> contents, Path tmpContentPath, Path dest) {
        contents.stream()
                .map(tmpContentPath::resolve)
                .map(Path::toFile)
                .forEach(file -> {
                    try {
                        com.google.common.io.Files.move(file, dest.resolve(file.getName()).toFile());
                    } catch (IOException e) {
                        throw new UncheckedIOException("unable to move temporary file: " + file, e);
                    }
                });
    }

    @Override
    public Stream<Activity> activities() {
        final ImmutableMap<String, Activity> activities = getActivities();
        return activities.values().stream();
    }

    @Override
    public Optional<Activity> findById(String id) {
        final ImmutableMap<String, Activity> activities = getActivities();
        return Optional.ofNullable(activities.get(id));
    }

    @Override
    public Optional<Activity> findByName(String name) {
        return activities().filter(activity -> activity.getName().equals(name)).findAny();
    }

    private String generateActivityDirName(Activity activity) {
        return activity.getId();
    }

    private void throwDuplicateException(Activity activity) {
        throw new IllegalStateException(
                format(
                        "an activity with name '%s' already exist, duplicates are not allowed",
                        activity.getName()
                )
        );
    }
}
