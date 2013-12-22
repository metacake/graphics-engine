package io.metacake.enginetwo.input;

import io.metacake.core.process.ActionRecognizer;

import java.awt.event.KeyEvent;

public interface KeyboardActionRecognizer extends ActionRecognizer {
    public void keyPressed(long timestamp);
    public void keyReleased(long timestamp);
}