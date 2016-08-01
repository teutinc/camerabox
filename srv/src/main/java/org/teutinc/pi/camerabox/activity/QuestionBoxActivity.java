package org.teutinc.pi.camerabox.activity;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.teutinc.pi.camerabox.activity.json.Serializers;

import java.io.IOException;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author apeyrard
 */
//@JsonSerialize(using = QuestionBoxActivity.Serializer.class)
//@JsonDeserialize(using = QuestionBoxActivity.Deserializer.class)
public class QuestionBoxActivity extends AbstractActivity {
    private static final Logger logger = getLogger(QuestionBoxActivity.class);

    private final Welcome welcome;
    private final ImmutableList<Question> questions;
    private final Optional<Bye> bye;

    @JsonCreator
    public QuestionBoxActivity(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("welcome") Welcome welcome,
            @JsonProperty("questions") ImmutableList<Question> questions,
            @JsonProperty("bye") Optional<Bye> bye) {

        super(id, name);
        this.welcome = welcome;
        this.questions = questions;
        this.bye = bye;
    }

    public QuestionBoxActivity(String name, Welcome welcome, ImmutableList<Question> questions, Optional<Bye> bye) {
        super(name);
        this.welcome = welcome;
        this.questions = questions;
        this.bye = bye;
    }

    public Welcome getWelcome() {
        return welcome;
    }

    public ImmutableList<Question> getQuestions() {
        return questions;
    }

    public Optional<Bye> getBye() {
        return bye;
    }

    public static class Welcome {
        private final DisplayTemplate template;

        @JsonCreator
        public Welcome(@JsonProperty("template") DisplayTemplate template) {
            this.template = template;
        }

        public DisplayTemplate getTemplate() {
            return template;
        }
    }

    public static class Question {
        private final long duration;
        private final DisplayTemplate template;

        @JsonCreator
        public Question(@JsonProperty("duration") long duration, @JsonProperty("template") DisplayTemplate template) {
            this.duration = duration;
            this.template = template;
        }

        public long getDuration() {
            return duration;
        }

        public DisplayTemplate getTemplate() {
            return template;
        }
    }

    public static class Bye {
        private final long duration;
        private final DisplayTemplate template;

        @JsonCreator
        public Bye(@JsonProperty("duration") long duration, @JsonProperty("template") DisplayTemplate template) {
            this.duration = duration;
            this.template = template;
        }

        public long getDuration() {
            return duration;
        }

        public DisplayTemplate getTemplate() {
            return template;
        }
    }

    @JsonTypeName("singleText")
    public static class SingleTextTemplate implements DisplayTemplate {
        private final String text;
        @JsonCreator
        public SingleTextTemplate(@JsonProperty("text") String text) {this.text = text;}

        public String getText() {
            return text;
        }
    }

    @JsonTypeName("singleImage")
    public static class SingleImageTemplate implements DisplayTemplate {
        private final String image;
        @JsonCreator
        public SingleImageTemplate(@JsonProperty("image") String image) {this.image = image;}

        public String getImage() {
            return image;
        }
    }

    @JsonTypeName("textAndImage")
    public static class TextAndImageTemplate implements DisplayTemplate {
        private final String text;
        private final String image;
        @JsonCreator
        public TextAndImageTemplate(@JsonProperty("text") String text, @JsonProperty("image") String image) {
            this.text = text;
            this.image = image;
        }

        public String getText() {
            return text;
        }

        public String getImage() {
            return image;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = SingleTextTemplate.class),
            @JsonSubTypes.Type(value = SingleImageTemplate.class),
            @JsonSubTypes.Type(value = TextAndImageTemplate.class)
    })
    public interface DisplayTemplate { }


    // ----------------- JSON SERIALIZATION PART

    static class Serializer extends Serializers.JsonTypedSerializer<QuestionBoxActivity> {
        @Override
        public void serialize(QuestionBoxActivity value,
                              JsonGenerator gen,
                              SerializerProvider provider) throws IOException {
            gen.writeStringField("id", value.getId());
            gen.writeStringField("name", value.getName());
        }
    }

    static class Deserializer extends Serializers.JsonTypedDeserializer<QuestionBoxActivity> {
        @Override
        public QuestionBoxActivity deserialize(JsonParser jp,
                                               DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            final String id = node.get("id").asText();
            final String name = node.get("name").asText();
            return null; // fixme ... new QuestionBoxActivity(UUID.fromString(id), name, welcome, ImmutableList.of(), bye);
        }
    }
}
