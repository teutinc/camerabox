package org.teutinc.pi.camerabox.activity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author apeyrard
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NoopActivity.class),
        @JsonSubTypes.Type(value = QuestionBoxActivity.class)
})
public interface Activity {
    String getId();

    String getName();
}
