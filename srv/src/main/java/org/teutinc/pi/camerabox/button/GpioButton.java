package org.teutinc.pi.camerabox.button;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * @author apeyrard
 */
public class GpioButton {
    public static GpioButton pullUp(Pin gpioPin) {
        return new GpioButton(gpioPin, PinPullResistance.PULL_UP);
    }

    private final Pin gpioPin;
    private final PinPullResistance pullResistance;

    private GpioButton(Pin gpioPin, PinPullResistance pullResistance) {
        this.gpioPin = gpioPin;
        this.pullResistance = pullResistance;
    }

    public void onPressed(Listener listener) {
        build((GpioPinListenerDigital) event -> {
            if (event.getState().isLow()) {
                listener.listen();
            }
        });
    }

    private void build(GpioPinListener listener) {
        final GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalInput pin = gpio.provisionDigitalInputPin(gpioPin, pullResistance);
        pin.addListener(listener);
    }

    @FunctionalInterface
    public interface Listener {
        void listen();
    }
}
