import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

public class TaskBarPanel extends JPanel {
    private Main main;

    public TaskBarPanel(Main main) {
        this.main = main;
    }

    public void init() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(Color.decode("#bac7d8"));
        setBorder(new EmptyBorder(5, 5, 5, 5));

        JButton newPanelButton = new JButton(new ImageIcon(getClass().getResource("/newPanel.png")));
        newPanelButton.addActionListener((e) -> main.newPanelDialog(10, 10));

        JButton newRelationButton = new JButton(new ImageIcon(getClass().getResource("newRelation.png")));
        newRelationButton.addActionListener((e) -> main.newRelationDialog());

        JButton deletePanelButton = new JButton(new ImageIcon(getClass().getResource("deletePanel.png")));
        deletePanelButton.addActionListener((e) -> main.deletePanelDialog());

        JButton deleteRelationButton = new JButton(new ImageIcon(getClass().getResource("deleteRelation.png")));
        deleteRelationButton.addActionListener((e) -> main.deleteRelationDialog());

        JButton navModeButton = new JButton(new ImageIcon(getClass().getResource("navmode.png")));
        navModeButton.addActionListener((e) -> main.enterNavMode());

        Dimension space = new Dimension(5, 5);
        add(newPanelButton);
        add(Box.createRigidArea(space));
        add(newRelationButton);
        add(Box.createRigidArea(space));
        add(deletePanelButton);
        add(Box.createRigidArea(space));
        add(deleteRelationButton);
        add(Box.createRigidArea(space));
        add(navModeButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;
        GradientPaint gp = new GradientPaint(0, 0, getBackground(), getWidth(), 0, getBackground().brighter());
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(Color.black);
        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }
}
