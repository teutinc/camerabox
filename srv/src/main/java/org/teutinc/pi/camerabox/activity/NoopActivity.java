package org.teutinc.pi.camerabox.activity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.teutinc.pi.camerabox.activity.json.Serializers;

import java.io.IOException;

/**
 * @author apeyrard
 */
@JsonSerialize(using = NoopActivity.Serializer.class)
@JsonDeserialize(using = NoopActivity.Deserializer.class)
public class NoopActivity extends AbstractActivity {

    public NoopActivity(String id, String name) {
        super(id, name);
    }

    public NoopActivity(String name) {
        super(name);
    }

    static class Serializer extends Serializers.JsonTypedSerializer<NoopActivity> {
        @Override
        public void serialize(NoopActivity value,
                              JsonGenerator gen,
                              SerializerProvider provider) throws IOException {
            gen.writeStringField("id", value.getId().toString());
            gen.writeStringField("name", value.getName());
        }
    }

    static class Deserializer extends Serializers.JsonTypedDeserializer<NoopActivity> {
        @Override
        public NoopActivity deserialize(JsonParser jp,
                                           DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            final String id = node.get("id").asText();
            final String name = node.get("name").asText();
            return new NoopActivity(id, name);
        }
    }

    @Override
    public String toString() {
        return "NoopActivity{" +
                "name='" + getName() + '\'' +
                '}';
    }
}
