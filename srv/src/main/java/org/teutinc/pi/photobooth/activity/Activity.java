package org.teutinc.pi.photobooth.activity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.teutinc.pi.photobooth.activity.runner.State;
import org.teutinc.pi.photobooth.activity.runner.StateDispatcher;
import org.teutinc.pi.photobooth.event.ActivityEvent;

import java.util.UUID;

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
