package org.teutinc.pi.camerabox.activity.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

/**
 * @author apeyrard
 */
public class Serializers {

    public static abstract class JsonTypedSerializer<O> extends JsonSerializer<O> {
        @Override
        public void serializeWithType(O value,
                                      JsonGenerator gen,
                                      SerializerProvider provider,
                                      TypeSerializer typeSer) throws IOException {

            typeSer.writeTypePrefixForObject(value, gen);
            serialize(value, gen, provider);
            typeSer.writeTypeSuffixForObject(value, gen);
        }
    }

    public static abstract class JsonTypedDeserializer<O> extends JsonDeserializer<O> {
        @Override
        public Object deserializeWithType(JsonParser jp,
                                          DeserializationContext ctxt,
                                          TypeDeserializer typeDeserializer) throws IOException {

            return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
        }
    }
}
