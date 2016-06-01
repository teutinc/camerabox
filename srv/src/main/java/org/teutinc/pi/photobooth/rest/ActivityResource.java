package org.teutinc.pi.photobooth.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import org.teutinc.pi.photobooth.CameraBox;
import org.teutinc.pi.photobooth.CameraBox.RunningActivity;
import org.teutinc.pi.photobooth.activity.Activity;
import restx.annotations.*;
import restx.factory.Component;
import restx.security.PermitAll;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author apeyrard
 */
@RestxResource("/activities")
@Component
@PermitAll
public class ActivityResource {
    private final CameraBox cameraBox;

    public ActivityResource(CameraBox cameraBox) {
        this.cameraBox = cameraBox;
    }

    @GET("")
    public List<RunningActivity> activities() {
        return cameraBox.activities().collect(Collectors.toList());
    }

    @GET("/:id")
    public Optional<Activity> activity(String id) {
        return cameraBox.activity(id);
    }

    @POST("")
    public Activity create(ActivityWithContent activityWithContent) {
        return cameraBox.add(activityWithContent.activity, activityWithContent.contents);
    }

    @PUT("/:id")
    public Activity update(String id, ActivityWithContent activityWithContent) {
        return cameraBox.update(activityWithContent.activity, activityWithContent.contents);
    }

    @DELETE("/:id")
    public void delete(String id) {
        cameraBox.delete(id);
    }

    @POST("/start/:id")
    public Optional<Activity> startActivity(String id) {
        return cameraBox.start(id);
    }

    @POST("/stop/:id")
    public Optional<Activity> stopActivity(String id) {
        return cameraBox.stop(id);
    }

    public static class ActivityWithContent {
        private final Activity activity;
        private final ImmutableSet<String> contents;

        @JsonCreator
        public ActivityWithContent(@JsonProperty("activity") Activity activity,
                                   @JsonProperty("contents") ImmutableSet<String> contents) {
            this.activity = activity;
            this.contents = contents != null ? contents : ImmutableSet.of();
        }

        public Activity getActivity() {
            return activity;
        }

        public ImmutableSet<String> getContents() {
            return contents;
        }
    }
}
