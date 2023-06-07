package valhalla.rendering.double_;

import javax.swing.*;

import static shared.CommonKt.screenHeight;
import static shared.CommonKt.screenWidth;

final class MyFrame extends JFrame {
    MyFrame() {
        this.add(new MyPanel());
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(screenWidth, screenHeight);
        this.setVisible(true);
        new Thread(() -> {
            try {
                while (true) {
                    SwingUtilities.invokeLater(() -> getContentPane().repaint());
                    Thread.sleep(10L);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
