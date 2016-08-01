package org.teutinc.pi.camerabox.activity.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.ImmutableList;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.teutinc.pi.camerabox.activity.QuestionBoxActivity;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test json serialization and deserialization for {@link QuestionBoxActivity}.
 *
 * @author apeyrard
 */
public class QuestionBoxActivityJsonTest {
    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private final QuestionBoxActivity refActivity1 = new QuestionBoxActivity(
            UUID.fromString("8bd0db33-0cc2-4fd3-bb71-ae24512891eb").toString().substring(0, 8),
            "some questions",
            new QuestionBoxActivity.Welcome(new QuestionBoxActivity.SingleTextTemplate("Click when ready...")),
            ImmutableList.of(
                    new QuestionBoxActivity.Question(5000, new QuestionBoxActivity.SingleTextTemplate("Describe yourself...")),
                    new QuestionBoxActivity.Question(20000, new QuestionBoxActivity.SingleTextTemplate("what is your favorite player?")),
                    new QuestionBoxActivity.Question(30000, new QuestionBoxActivity.TextAndImageTemplate("what do you think about him?", "pogba.jpg"))
            ),
            Optional.of(new QuestionBoxActivity.Bye(10000, new QuestionBoxActivity.SingleTextTemplate("that's it. thanks. see you")))
    );

    private ObjectMapper buildMapper() {
        return new ObjectMapper().registerModule(new Jdk8Module())
                                 .registerModule(new GuavaModule());
    }

    @Test
    public void should_serialize_question_box_activity() throws JsonProcessingException {
        // GIVEN
        final ObjectMapper mapper = buildMapper();

        // WHEN
        final String serialized = mapper.writerWithDefaultPrettyPrinter()
                                        .writeValueAsString(refActivity1);

        // THEN
        softly.assertThat(serialized).isEqualTo(
                "{\n" +
                        "  \"type\" : \"QuestionBoxActivity\",\n" +
                        "  \"id\" : \"8bd0db33\",\n" +
                        "  \"name\" : \"some questions\",\n" +
                        "  \"welcome\" : {\n" +
                        "    \"template\" : {\n" +
                        "      \"type\" : \"singleText\",\n" +
                        "      \"text\" : \"Click when ready...\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"questions\" : [ {\n" +
                        "    \"duration\" : 5000,\n" +
                        "    \"template\" : {\n" +
                        "      \"type\" : \"singleText\",\n" +
                        "      \"text\" : \"Describe yourself...\"\n" +
                        "    }\n" +
                        "  }, {\n" +
                        "    \"duration\" : 20000,\n" +
                        "    \"template\" : {\n" +
                        "      \"type\" : \"singleText\",\n" +
                        "      \"text\" : \"what is your favorite player?\"\n" +
                        "    }\n" +
                        "  }, {\n" +
                        "    \"duration\" : 30000,\n" +
                        "    \"template\" : {\n" +
                        "      \"type\" : \"textAndImage\",\n" +
                        "      \"text\" : \"what do you think about him?\",\n" +
                        "      \"image\" : \"pogba.jpg\"\n" +
                        "    }\n" +
                        "  } ],\n" +
                        "  \"bye\" : {\n" +
                        "    \"duration\" : 10000,\n" +
                        "    \"template\" : {\n" +
                        "      \"type\" : \"singleText\",\n" +
                        "      \"text\" : \"that's it. thanks. see you\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Test
    public void should_deserialize_question_box_activity() throws IOException {
        // GIVEN
        final ObjectMapper mapper = buildMapper();
        final String serialized = mapper.writeValueAsString(refActivity1);

        // WHEN
        final QuestionBoxActivity questionBoxActivity = mapper.readValue(serialized, QuestionBoxActivity.class);

        // THEN
        softly.assertThat(questionBoxActivity.getId()).isEqualTo("8bd0db33");
        softly.assertThat(questionBoxActivity.getName()).isEqualTo("some questions");
        assertThat(questionBoxActivity.getWelcome().getTemplate()).isInstanceOf(QuestionBoxActivity.SingleTextTemplate.class);
        final QuestionBoxActivity.SingleTextTemplate template = (QuestionBoxActivity.SingleTextTemplate) questionBoxActivity.getWelcome().getTemplate();
        softly.assertThat(template.getText()).isEqualTo("Click when ready...");
        softly.assertThat(questionBoxActivity.getQuestions()).hasSize(3);
        assertThat(questionBoxActivity.getBye().isPresent()).isTrue();
        final QuestionBoxActivity.Bye bye = questionBoxActivity.getBye().get();
        softly.assertThat(bye.getDuration()).isEqualTo(10000);
    }
}
