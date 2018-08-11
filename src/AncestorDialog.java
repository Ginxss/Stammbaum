import javax.swing.*;
import java.awt.*;

public class AncestorDialog extends JDialog {
    private JPanel backgroundPanel;
    private AncestorTree ancestorTree;

    public AncestorDialog(JFrame window, Panel panel, RelationList relationList) {
        super(window);

        setModal(false);
        setLocationRelativeTo(window);
        setSize(800, 400);
        setTitle("Vorfahren von " + panel.getName());

        backgroundPanel = new JPanel(null)  {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                resizeLabels(ancestorTree.root, 0, backgroundPanel.getWidth(), 1);
                drawLines(ancestorTree.root, g2);
            }
        };

        getContentPane().add(backgroundPanel);
        setVisible(true);

        JLabel newLabel = new JLabel(panel.getName());
        newLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        ancestorTree = new AncestorTree(newLabel);
        fillTree(ancestorTree.root, relationList);

        addLabels(ancestorTree.root, 0, backgroundPanel.getWidth(), 1);
    }

    private void fillTree(Node startNode, RelationList relationList) {
        for (Relation relation : relationList.getChildRelations()) {
            if (relation.srcPanel.getName().equals(startNode.data.getText())) {
                JLabel newLabel = new JLabel(relation.targetPanel.getName());
                newLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
                startNode.parents.add(new Node(newLabel));
            }
        }

        for (Node node : startNode.parents) {
            fillTree(node, relationList);
        }
    }

    private void addLabels(Node startNode, int startX, int width, int heightLevel) {
        int height = 20;
        startNode.data.setBounds(startX, backgroundPanel.getHeight() - (heightLevel * height * 4), width, height);
        startNode.data.setHorizontalAlignment(JLabel.CENTER);
        startNode.data.setVerticalAlignment(JLabel.CENTER);

        backgroundPanel.add(startNode.data);

        for (int i = 0; i < startNode.parents.size(); i++) {
            Node node = startNode.parents.get(i);
            int areaWidth = width / startNode.parents.size();
            addLabels(node, startX + i * areaWidth, areaWidth, heightLevel + 1);
        }
    }

    private void resizeLabels(Node startNode, int startX, int width, int heightLevel) {
        int height = 20;
        startNode.data.setBounds(startX, backgroundPanel.getHeight() - (heightLevel * height * 4), width, height);

        for (int i = 0; i < startNode.parents.size(); i++) {
            Node node = startNode.parents.get(i);
            int areaWidth = width / startNode.parents.size();
            resizeLabels(node, startX + i * areaWidth, areaWidth, heightLevel + 1);
        }
    }

    private void drawLines(Node startNode, Graphics2D g2) {
        for (Node node : startNode.parents) {
            int myX = startNode.data.getX() + startNode.data.getWidth() / 2;
            int myY = startNode.data.getY();
            int parentX = node.data.getX() + node.data.getWidth() / 2;
            int parentY = node.data.getY() + node.data.getHeight();

            g2.drawLine(myX, myY, parentX, parentY);

            drawLines(node, g2);
        }
    }

}