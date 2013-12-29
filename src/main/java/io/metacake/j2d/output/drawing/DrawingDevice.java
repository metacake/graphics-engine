package io.metacake.j2d.output.drawing;

import io.metacake.core.common.TimedLoopThread;
import io.metacake.core.common.window.CakeWindow;
import io.metacake.core.output.OutputDeviceName;
import io.metacake.core.output.RenderingInstruction;
import io.metacake.core.output.system.OutputDevice;
import io.metacake.j2d.window.GraphicsWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.Collection;
import java.util.Collections;

/**
 * The {@code DrawingDevice} is an {@link io.metacake.core.output.system.OutputDevice} that uses the Java Graphics API
 * to draw to a {@link javax.swing.JFrame}.
 * <p>
 *     Notable Implementation Choices:
 *     This device will create a new {@link java.awt.Graphics2D} object for every {@link io.metacake.core.output.RenderingInstruction}
 *     that is sent to it. This is preserves the initial transformation to account for the {@link javax.swing.JFrame}'s
 *     {@link java.awt.Insets} and too remove any changes in state that are introduced by drawing various objects to
 *     the screen.
 *     A {@link java.awt.image.BufferStrategy} is also used to create multiple drawing buffers to eliminate any graphical
 *     flicker.
 * </p>
 * @author florence
 * @author rpless
 */
public class DrawingDevice implements OutputDevice {
    private static final int NUM_BUFFERS = 2;

    public static final OutputDeviceName NAME = new OutputDeviceName();
    private volatile Collection<RenderingInstruction> instructions = Collections.emptyList();
    private BufferStrategy bufferStrategy;
    private JFrame frame;
    private TimedLoopThread drawingThread;

    @Override
    public OutputDeviceName name() {
        return NAME;
    }

    @Override
    public void render(Collection<RenderingInstruction> instructions) {
        this.instructions = instructions;
    }

    @Override
    public void startOutputLoop() {
        drawingThread = new TimedLoopThread(() -> draw(instructions));
        drawingThread.start();
    }

    @Override
    public void shutdown() {
        drawingThread.requestStop();
    }

    @Override
    public void bind(CakeWindow w) {
        GraphicsWindow window = (GraphicsWindow) w;
        frame = window.getRawWindow();
        frame.createBufferStrategy(NUM_BUFFERS);
        bufferStrategy = frame.getBufferStrategy();
    }

    @SuppressWarnings("unchecked")
    private void draw(Collection<RenderingInstruction> instructions) {
        Graphics2D parentGraphics = (Graphics2D) this.bufferStrategy.getDrawGraphics();
        Insets insets = this.frame.getInsets();
        parentGraphics.translate(insets.right, insets.top);
        parentGraphics.setColor(Color.WHITE);
        parentGraphics.fillRect(0, 0, frame.getWidth(), frame.getHeight());
        for (RenderingInstruction<Graphics> instruction : instructions) {
            Graphics2D graphics = (Graphics2D) parentGraphics.create();
            instruction.render(graphics);
            graphics.dispose();
        }
        parentGraphics.dispose();
        this.bufferStrategy.show();
    }
}