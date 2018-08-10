import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Panel extends JPanel implements MouseListener, MouseMotionListener {
    private Point thisPos;
    private Point mousePos;

    private boolean entered;
    private boolean selected;

    private JLabel text;
    private static Font font = new Font("Tahoma", Font.PLAIN, 14);

    public Panel(String name, int x, int y) {
        setBackground(Color.decode("#efd667"));
        addMouseListener(this);
        addMouseMotionListener(this);

        thisPos = new Point();
        mousePos = new Point();

        entered = false;
        selected = false;

        text = new JLabel(name);
        text.setFont(font);

        FontMetrics fm = text.getFontMetrics(font);
        int width = fm.stringWidth(name);
        int height = fm.getHeight();
        setBounds(x, y, width + 10, height + 10);

        add(text);
    }

    public String getName() {
        return text.getText();
    }

    public void setName(String name) {
        text.setText(name);

        FontMetrics fm = text.getFontMetrics(font);
        int width = fm.stringWidth(name);
        int height = fm.getHeight();
        setBounds(getX(), getY(), width + 10, height + 10);
    }

    public void select() {
        selected = true;
    }

    public void unselect() {
        selected = false;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;
        GradientPaint gp = new GradientPaint(0, 0, getBackground(), 0, getHeight(), getBackground().brighter().brighter());
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (selected) {
            g2.setColor(Color.black);
            g2.setStroke(new BasicStroke(3));
        }
        else if (entered) {
            g2.setColor(Color.gray);
            g2.setStroke(new BasicStroke(3));
        }
        else {
            g2.setColor(Color.gray);
            g2.setStroke(new BasicStroke(1));
        }
        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (ContentPanel.creatingRelation) {
            Component source = (Component)e.getSource();
            source.getParent().dispatchEvent(e);
        }
        else {
            if (!selected)
                select();
            else
                unselect();
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            mousePos.x = e.getXOnScreen();
            mousePos.y = e.getYOnScreen();
            thisPos.x = getX();
            thisPos.y = getY();
        }
        else if (SwingUtilities.isMiddleMouseButton(e)) {
            Component source = (Component)e.getSource();
            source.getParent().dispatchEvent(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {
        entered = true;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        entered = false;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            int diffX = e.getXOnScreen() - mousePos.x;
            int diffY = e.getYOnScreen() - mousePos.y;

            setLocation(thisPos.x + diffX, thisPos.y + diffY);

            getParent().repaint();
        }
        else if (SwingUtilities.isMiddleMouseButton(e)) {
            Component source = (Component)e.getSource();
            source.getParent().dispatchEvent(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {}
}
