import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class NavModePanel extends JPanel {
    private BufferedImage contentImg;
    private double ratio = 0.0;

    public void setContentImg(BufferedImage img) {
        contentImg = img;
        ratio = img.getWidth() / img.getHeight();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Irgendwie auf der Linken Seite platzieren, Ratio beibehalten.
        int width = getWidth();
        int height = (int)(ratio * width);
        g2.drawImage(contentImg, 0, 0, width, height, 0, 0, contentImg.getWidth(), contentImg.getHeight(), this);
    }
}
