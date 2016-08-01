package org.teutinc.pi.camerabox;

import restx.config.Settings;
import restx.config.SettingsKey;

/**
 * @author apeyrard
 */
@Settings
public interface AppSettings {

    @SettingsKey(key = "camerabox.activity.storePath")
    String activityStorePath();

    @SettingsKey(key = "camerabox.upload.tempPath")
    String uploadTempPath();

    @SettingsKey(key = "camerabox.upload.allowedTypes", defaultValue = "image/jpeg, image/png, image/bmp, image/x-png")
    String uploadAllowedTypes();

    @SettingsKey(key = "camerabox.button.doubleClickDelay", defaultValue = "500")
    long doubleClickDelay();


}
