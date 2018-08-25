import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatusPanel extends JPanel {
    private JLabel statusLabel;

    public void init() {
        statusLabel = new JLabel("Anzahl Personen: 0");
        statusLabel.setBorder(new EmptyBorder(3, 3, 3, 3));
        statusLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(Color.gray.brighter());
        add(statusLabel);
    }

    public JLabel getLabel() {
        return statusLabel;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        g2.setColor(Color.black);
        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }
}
