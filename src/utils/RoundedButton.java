package utils;

import java.awt.*;
import javax.swing.*;

public class RoundedButton extends JButton {

    private int radius = 20;

    public RoundedButton(String text) {
        super(text);
        setOpaque(false);
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        super.paintComponent(g);
        g2.dispose();
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
