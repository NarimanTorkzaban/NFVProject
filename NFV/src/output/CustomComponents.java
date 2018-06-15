package output;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JLabel;

public class CustomComponents extends JLabel {

    private static final long serialVersionUID = 1L;

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(200, 100);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 200);
    }

    @Override
    public void paintComponent(Graphics g) {
        int x=10;
        int y=10;
        super.paintComponent(g);
        g.setColor(Color.red);
        g.drawLine(x, y, x+10, y+10);
    }
}
