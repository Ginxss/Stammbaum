import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

public class NavModePanel extends JPanel implements MouseMotionListener {
    private BufferedImage contentImg;
    private double ratio;
    private Point mousePos;

    public NavModePanel() {
        ratio = 0.0;
        mousePos = new Point();
        addMouseMotionListener(this);
    }

    public void setContentImg(BufferedImage img) {
        contentImg = img;
        ratio = (double)img.getHeight() / img.getWidth();
        System.out.println(ratio);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        ///////////////////////////////////// Ratios alle Korrekt, jetzt TODO: Im Preview Fenster Teil des imgs anzeigen

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int imgWidth = contentImg.getWidth();
        int imgHeight = contentImg.getHeight();

        int previewWidth = panelWidth / 2;
        int previewHeight = panelHeight / 2;

        int scaleWidth = panelWidth / 2;
        int scaleHeight = (int)(scaleWidth * ratio);
        if (scaleHeight > panelHeight) {
            scaleHeight = panelHeight;
            scaleWidth = (int)(scaleHeight / ratio);
        }

        double widthScaleRatio = (double)scaleWidth / imgWidth;
        double heightScaleRatio = (double)scaleHeight / imgHeight;
        int cursorWidth = (int)(panelWidth * widthScaleRatio);
        int cursorHeight = (int)(panelHeight * heightScaleRatio);

        g2.setStroke(new BasicStroke(3));

        // Draw Preview Window Frame
        g2.drawRect(panelWidth / 2, 0, previewWidth, previewHeight);

        // Draw Image
        g2.drawImage(contentImg, 0, 0, scaleWidth, scaleHeight, 0, 0, imgWidth, imgHeight, this);

        // Draw Cursor
        g2.drawRect(mousePos.x- cursorWidth / 2, mousePos.y - cursorHeight / 2, cursorWidth, cursorHeight);
    }


    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos.x = e.getX();
        mousePos.y = e.getY();
        repaint();
    }
}
