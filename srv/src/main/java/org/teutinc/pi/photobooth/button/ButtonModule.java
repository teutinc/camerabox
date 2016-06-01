package org.teutinc.pi.photobooth.button;

import com.pi4j.io.gpio.RaspiPin;
import org.teutinc.pi.photobooth.AppSettings;
import org.teutinc.pi.photobooth.event.ActivityEvent;
import org.teutinc.pi.photobooth.event.EventBus;
import org.teutinc.pi.photobooth.util.lang.OSUtils;
import restx.factory.AutoPreparable;
import restx.factory.Module;
import restx.factory.Provides;

import java.util.concurrent.TimeUnit;

/**
 * @author apeyrard
 */
@Module
public class ButtonModule {

    /*
        Register the button listener, but only if we are on the raspberry...
     */
    @Provides
    public AutoPreparable manageButton(EventBus eventBus, AppSettings appSettings) {
        if (OSUtils.isArmArch()) {
            return () -> {
                ButtonEventCoalescor coalescor = new ButtonEventCoalescor(
                        () -> eventBus.post(ActivityEvent.pressed),
                        () -> eventBus.post(ActivityEvent.doubleClicked),
                        appSettings.doubleClickDelay(), TimeUnit.MILLISECONDS
                );
                GpioButton.pullUp(RaspiPin.GPIO_00)
                        .onPressed(coalescor::click);
            };
        } else {
            return () -> {};
        }
    }
}
