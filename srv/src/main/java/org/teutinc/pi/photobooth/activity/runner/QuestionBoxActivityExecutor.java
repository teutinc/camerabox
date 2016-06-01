package org.teutinc.pi.photobooth.activity.runner;

import org.slf4j.Logger;
import org.teutinc.pi.photobooth.activity.QuestionBoxActivity;
import org.teutinc.pi.photobooth.activity.QuestionBoxActivity.Bye;
import org.teutinc.pi.photobooth.activity.QuestionBoxActivity.DisplayTemplate;
import org.teutinc.pi.photobooth.activity.QuestionBoxActivity.Question;
import org.teutinc.pi.photobooth.activity.QuestionBoxActivity.Welcome;
import org.teutinc.pi.photobooth.device.Camera;
import org.teutinc.pi.photobooth.event.ActivityEvent;
import restx.factory.Component;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class QuestionBoxActivityExecutor extends ActivityExecutor.ForType<QuestionBoxActivity> {
    private static final Logger logger = getLogger(QuestionBoxActivityExecutor.class);

    private final Camera camera;
    private volatile boolean interrupted = false;
    private volatile State state = State.noop();
    private volatile boolean stopForced = false;
    private final ReentrantLock stepGuard = new ReentrantLock();
    private final Condition endOfStep = stepGuard.newCondition();

    QuestionBoxActivityExecutor(Camera camera) {
        super(QuestionBoxActivity.class);
        this.camera = camera;
    }

    @Override
    public void run(QuestionBoxActivity activity, StateDispatcher dispatcher) {
        logger.info("starting question box: {}", activity.getName());
        int questionIndex = 0;
        stopForced = false;
        interrupted = false;

        welcomeStep(activity.getId(), activity.getWelcome(), dispatcher).run();

        Camera.Recording recording = camera.record();
        while (!interrupted && !stopForced && questionIndex < activity.getQuestions().size()) {
            questionStep(
                    activity.getId(),
                    activity.getQuestions().get(questionIndex),
                    questionIndex,
                    activity.getQuestions().size(),
                    dispatcher
            ).run();

            questionIndex++;
        }
        recording.stop()
                 .moveTo(Paths.get("/tmp")); // fixme move to a storage for this activity

        if (!interrupted) {
            activity.getBye().ifPresent(bye -> byeStep(activity.getId(), bye, dispatcher).run());
        }
    }

    private Step welcomeStep(String id, Welcome welcome, StateDispatcher dispatcher) {
        logger.debug("display welcome...");
        state = dispatcher.dispatch(QuestionBoxState.welcome(id, welcome));
        return this::waitIndefinitely;
    }

    private Step questionStep(String id, Question question, int number, int total, StateDispatcher dispatcher) {
        logger.debug("start question number: {}", number);
        state = dispatcher.dispatch(QuestionBoxState.question(id, question, number, total));
        return () -> waitFor(Duration.ofMillis(question.getDuration()));
    }

    private Step byeStep(String id, Bye bye, StateDispatcher dispatcher) {
        logger.debug("display bye...");
        state = dispatcher.dispatch(QuestionBoxState.bye(id, bye));
        return () -> waitFor(Duration.ofMillis(bye.getDuration()));
    }

    private void waitIndefinitely() {
        waitFor(Duration.ZERO);
    }

    private void waitFor(Duration duration) {
        final ReentrantLock stepGuard = this.stepGuard;
        stepGuard.lock();
        try {
            if (duration.isZero()) {
                endOfStep.await();
            } else {
                endOfStep.await(duration.toMillis(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            stepGuard.unlock();
        }
    }

    private void wakeUp() {
        final ReentrantLock stepGuard = this.stepGuard;
        stepGuard.lock();
        try {
            endOfStep.signal();
        } finally {
            stepGuard.unlock();
        }
    }

    @Override
    public State state() {
        return state.fresh();
    }

    @Override
    public void stop() {
        interrupted = true;
        wakeUp();
    }

    @Override
    public void handleEvent(ActivityEvent event) {
        if (event == ActivityEvent.pressed) {
            logger.debug("next step...");
            wakeUp();
        } else if (event == ActivityEvent.doubleClicked) {
            logger.debug("force current run to end...");
            if (state instanceof QuestionBoxState && "welcome".equals(((QuestionBoxState) state).getStep())) {
                logger.debug("skip, end of current run, we are still in welcome step... Just treat it as a single click...");
                wakeUp();
            } else {
                stopForced = true;
                wakeUp();
            }
        }
    }


    @FunctionalInterface
    private interface Step {
        void run();
    }

    public static class QuestionBoxState implements State {
        static QuestionBoxState welcome(String id, Welcome welcome) {
            return new QuestionBoxState(id, "welcome", welcome.getTemplate(), null, null, null);
        }

        static QuestionBoxState question(String id, Question question, int number, int total) {
            return new QuestionBoxState(id, "question", question.getTemplate(), question.getDuration(), number, total);
        }

        static QuestionBoxState bye(String id, Bye bye) {
            return new QuestionBoxState(id, "bye", bye.getTemplate(), bye.getDuration(), null, null);
        }

        private final String id;
        private final Instant creationDate = Instant.now();
        private final String step;
        private final DisplayTemplate template;
        private final Long delay;
        private final Integer number;
        private final Integer total;

        public QuestionBoxState(String id, String step, DisplayTemplate template, Long delay, Integer number, Integer total) {
            this.id = id;
            this.step = step;
            this.template = template;
            this.delay = delay;
            this.number = number;
            this.total = total;
        }

        @Override
        public String getActivityId() {
            return id;
        }

        @Override
        public String getActivity() {
            return "questionBox";
        }

        public String getStep() {
            return step;
        }

        public DisplayTemplate getTemplate() {
            return template;
        }

        public Long getDelay() {
            return delay;
        }

        public Integer getNumber() {
            return number;
        }

        public Integer getTotal() {
            return total;
        }

        @Override
        public State fresh() {
            return new QuestionBoxState(
                    id,
                    step,
                    template,
                    delay != null ? Math.max(0, delay - (creationDate.until(Instant.now(), ChronoUnit.MILLIS))) : null,
                    number,
                    total
            );
        }
    }
}