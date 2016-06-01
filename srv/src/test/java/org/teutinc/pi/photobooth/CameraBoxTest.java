package org.teutinc.pi.photobooth;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test cases for {@link CameraBox}
 */
public class CameraBoxTest {

    @Test
    public void should_stream_no_activities_when_no_activities_is_available_in_storage() {
        // WHEN
//        PhotoBooth photoBooth =
//                PhotoBooth.builder()
//                          .storage(InMemoryActivityStore.create())
//                          .build();
//
//        // THEN
//        assertThat(
//                photoBooth.activities()
//                          .map(Activity::name)
//        ).isEmpty();
//    }
//
//    @Test
//    public void should_create_activities() {
//        // WHEN
//        PhotoBooth photoBooth =
//                PhotoBooth.builder()
//                          .storage(InMemoryActivityStore.create())
//                          .build()
//                          .addActivity(
//                                  QuestionBoxActivity.builder()
//                                                     .name("some question box")
//                                                     .addQuestion(
//                                                             BasicTextImageQuestion.builder()
//                                                                                   .question("Introduce yourself?")
//                                                                                   .maxAnswerDuration(30, ChronoUnit.SECONDS)
//                                                                                   .build()
//                                                     )
//                                                     .addQuestion(
//                                                             BasicTextImageQuestion.builder()
//                                                                                   .question("What is your favorite team?")
//                                                                                   .maxAnswerDuration(20, ChronoUnit.SECONDS)
//                                                                                   .build()
//                                                     )
//                                                     .addQuestion(
//                                                             BasicTextImageQuestion.builder()
//                                                                                   .question("What do you think about this player?")
//                                                                                   .image("pogba.png")
//                                                                                   .maxAnswerDuration(50, ChronoUnit.SECONDS)
//                                                                                   .build()
//                                                     )
//                                                     .nextBinding(ButtonEvent.PRESSED)
//                                                     .stopBinding(ButtonEvent.DOUBLE_CLICK)
//                                                     .build()
//                          )
//                          .addActivity(
//                                  PhotoBoothActivity.builder()
//                                                    .name("photobooth")
//                                                    .delayBeforePicture(3, ChronoUnit.SECONDS)
//                                                    .build()
//                          );
//
//        // THEN
//        assertThat(
//                photoBooth.activities()
//                          .map(Activity::name)
//        ).containsOnly("some question box", "photobooth");
    }
}