import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class NavModePanel extends JPanel implements MouseListener, MouseMotionListener {
    private BufferedImage contentImg;

    private double ratio;
    private double scalingFactor;
    private Point mousePos;

    private Point beginPoint;
    private LinkedList<Panel> panelList;
    private CardLayout cl;
    private LinkedList<ChildParentGroup> groups;

    public NavModePanel() {
        addMouseListener(this);
        addMouseMotionListener(this);

        ratio = 0.0;
        scalingFactor = 0.0;
        mousePos = new Point();
    }

    public void init(BufferedImage img, Point middle, LinkedList<Panel> panelList, LinkedList<ChildParentGroup> groups, CardLayout cl) {
        contentImg = img;
        ratio = (double)img.getHeight() / img.getWidth();
        beginPoint = new Point(middle.x, middle.y);
        this.panelList = panelList;
        this.cl = cl;
        this.groups = groups;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int imgWidth = contentImg.getWidth();
        int imgHeight = contentImg.getHeight();

        int previewX = panelWidth / 2;
        int previewY = 0;
        int previewWidth = panelWidth / 2;
        int previewHeight = panelHeight / 2;

        int scaleWidth = panelWidth - previewWidth;
        int scaleHeight = (int)(scaleWidth * ratio);
        if (scaleHeight > panelHeight) {
            scaleHeight = panelHeight;
            scaleWidth = (int)(scaleHeight / ratio);
        }
        scalingFactor = (double)imgWidth / scaleWidth;

        double widthScaleRatio = (double)scaleWidth / imgWidth;
        double heightScaleRatio = (double)scaleHeight / imgHeight;

        int cursorWidth = (int)(panelWidth * widthScaleRatio);
        int cursorHeight = (int)(panelHeight * heightScaleRatio);
        int cursorX = mousePos.x- cursorWidth / 2;
        int cursorY = mousePos.y - cursorHeight / 2;

        int previewImgX = (int)(cursorX / widthScaleRatio);
        int previewImgY = (int)(cursorY / heightScaleRatio);

        g2.setStroke(new BasicStroke(3));

        // Draw Image
        g2.drawImage(contentImg, 0, 0, scaleWidth, scaleHeight, 0, 0, imgWidth, imgHeight, this);

        // Draw Preview Image
        g2.drawImage(contentImg, previewX, previewY, previewWidth + previewX, previewHeight + previewY,
                                 previewImgX, previewImgY, panelWidth + previewImgX, panelHeight + previewImgY, this);

        // Draw Preview Frame
        g2.drawRect(previewX, previewY, previewWidth, previewHeight);

        // Draw Cursor
        g2.drawRect(cursorX, cursorY, cursorWidth, cursorHeight);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int actualX = (int)(e.getX() * scalingFactor);
        int actualY = (int)(e.getY() * scalingFactor);

        int diffX = beginPoint.x - actualX;
        int diffY = beginPoint.y - actualY;

        for (Panel panel : panelList)
            panel.setLocation(panel.getX() + diffX, panel.getY() + diffY);

        for (ChildParentGroup group : groups)
            group.update();

        cl.show(getParent(), "Content");
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos.x = e.getX();
        mousePos.y = e.getY();
        repaint();
    }
}
