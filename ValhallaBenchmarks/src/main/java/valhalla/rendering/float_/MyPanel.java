package valhalla.rendering.float_;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import javax.swing.JPanel;
import javax.swing.Timer;
import kotlin.ranges.RangesKt;
import shared.AbstractGraphics;
import shared.CommonKt;

import static shared.ConfigKt.OffsetInSeconds;

final class MyPanel extends JPanel implements KeyListener, MouseListener {
    private long startTime = System.nanoTime();
    private int frameCount;
    private double fps = 100.0;
    private double minFps = 100.0;
    private final long veryStartTime = System.nanoTime();
    private Point2 pos = new Point2(22.0f, 12.0f);
    private Vector2 dir = new Vector2(-1.0f, 0.0f);
    private Vector2 plane = new Vector2(0.0f, 0.66f);

    Point2 getPos() {
        return pos;
    }

    void setPos(Point2 var1) {
        pos = var1;
    }

    Vector2 getDir() {
        return dir;
    }

    void setDir(Vector2 var1) {
        dir = var1;
    }

    Vector2 getPlane() {
        return plane;
    }

    void setPlane(Vector2 var1) {
        plane = var1;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        mainLoop(CommonKt.asAbstract((Graphics2D)g));
        g.setColor(Color.BLACK);
        g.drawString("Current FPS: " + new DecimalFormat("#.#").format(fps) + ", Min FPS: " + new DecimalFormat("#.#").format(minFps), 10, 20);
        frameCount++;
    }

    private void mainLoop(AbstractGraphics g) {
        for(int i = 0; i < 100; ++i) {
            Runner.access$drawScene(pos, dir, plane, g);
        }
    }

    public void keyPressed(KeyEvent e) {
        float frameTime = 1 / (float)fps;
        float moveSpeed = frameTime * 0.5f;
        float rotSpeed = frameTime * 0.3f;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> {
                dir = dir.rotate(rotSpeed);
                plane = plane.rotate(rotSpeed);
            }
            case KeyEvent.VK_UP -> {
                if (Runner.canMove(pos.plus(dir.times(moveSpeed).xProjection()))) {
                    pos = pos.plus(dir.times(moveSpeed).xProjection());
                }
                if (Runner.canMove(pos.plus(dir.times(moveSpeed).yProjection()))) {
                    pos = pos.plus(dir.times(moveSpeed).yProjection());
                }
            }
            case KeyEvent.VK_RIGHT -> {
                dir = dir.rotate(-rotSpeed);
                plane = plane.rotate(-rotSpeed);
            }
            case KeyEvent.VK_DOWN -> {
                if (Runner.canMove(pos.minus(dir.times(moveSpeed).xProjection()))) {
                    pos = pos.minus(dir.times(moveSpeed).xProjection());
                }
                if (Runner.canMove(pos.minus(dir.times(moveSpeed).yProjection()))) {
                    pos = pos.minus(dir.times(moveSpeed).yProjection());
                }
            }
        }

        repaint();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    MyPanel() {
        addKeyListener(this);
        addMouseListener(this);
        setFocusable(true);
        new Timer(1000, it -> {
            long currentTime = System.nanoTime();
            float elapsedTime = (float)(currentTime - startTime) / 1.0E9f;
            fps = (float)frameCount / elapsedTime;
            if ((float)(currentTime - veryStartTime) / 1.0E9 > (float)OffsetInSeconds) {
                minFps = RangesKt.coerceAtMost(minFps, fps);
            }

            startTime = currentTime;
            frameCount = 0;
        }).start();
        startTime = System.nanoTime();
    }
}
