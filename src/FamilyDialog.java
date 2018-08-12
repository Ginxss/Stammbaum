import javax.swing.*;
import java.awt.*;

public class FamilyDialog extends JDialog {
    public enum Type {
        ANCESTOR, DESCENDANTS
    }

    private JPanel backgroundPanel;
    private FamilyTree familyTree;
    private Type type;

    public FamilyDialog(JFrame window, Panel panel, RelationList relationList, Type type) {
        super(window);

        this.type = type;

        setModal(false);
        setLocationRelativeTo(window);
        setSize(800, 400);
        switch (type) {
            case ANCESTOR: setTitle("Vorfahren von " + panel.getName()); break;
            case DESCENDANTS: setTitle("Nachfahren von " + panel.getName());
        }

        backgroundPanel = new JPanel(null)  {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // ??
                resizeLabels(familyTree.root, 0, backgroundPanel.getWidth(), 1);
                drawLines(familyTree.root, g2);
            }
        };
        getContentPane().add(backgroundPanel);
        setVisible(true);

        JLabel newLabel = new JLabel(panel.getName());
        newLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        familyTree = new FamilyTree(newLabel);
        fillTree(familyTree.root, relationList);

        addLabels(familyTree.root, 0, backgroundPanel.getWidth(), 1);
    }

    private void fillTree(Node startNode, RelationList relationList) {
        for (Relation relation : relationList.getChildRelations()) {
            switch (type) {
                case ANCESTOR:
                    if (relation.srcPanel.getName().equals(startNode.data.getText())) {
                        JLabel newLabel = new JLabel(relation.targetPanel.getName());
                        newLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
                        startNode.next.add(new Node(newLabel));
                    }
                    break;
                case DESCENDANTS:
                    if (relation.targetPanel.getName().equals(startNode.data.getText())) {
                        JLabel newLabel = new JLabel(relation.srcPanel.getName());
                        newLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
                        startNode.next.add(new Node(newLabel));
                    }
            }


        }

        for (Node node : startNode.next)
            fillTree(node, relationList);
    }

    private void addLabels(Node startNode, int startX, int width, int heightLevel) {
        int height = 20;
        int y = 0;
        switch (type) {
            case ANCESTOR:
                y = backgroundPanel.getHeight() - (heightLevel * height * 3);
                break;
            case DESCENDANTS:
                y = (heightLevel * height * 3);
        }
        startNode.data.setBounds(startX, y, width, height);
        startNode.data.setHorizontalAlignment(JLabel.CENTER);
        startNode.data.setVerticalAlignment(JLabel.CENTER);

        backgroundPanel.add(startNode.data);

        for (int i = 0; i < startNode.next.size(); i++) {
            Node node = startNode.next.get(i);
            int areaWidth = width / startNode.next.size();
            addLabels(node, startX + i * areaWidth, areaWidth, heightLevel + 1);
        }
    }

    private void resizeLabels(Node startNode, int startX, int width, int heightLevel) {
        int height = 20;
        int y = 0;
        switch (type) {
            case ANCESTOR:
                y = backgroundPanel.getHeight() - (heightLevel * height * 3);
                break;
            case DESCENDANTS:
                y = (heightLevel * height * 3);
        }
        startNode.data.setBounds(startX, y, width, height);

        for (int i = 0; i < startNode.next.size(); i++) {
            Node node = startNode.next.get(i);
            int areaWidth = width / startNode.next.size();
            resizeLabels(node, startX + i * areaWidth, areaWidth, heightLevel + 1);
        }
    }

    private void drawLines(Node startNode, Graphics2D g2) {
        for (Node node : startNode.next) {
            int myX = startNode.data.getX() + startNode.data.getWidth() / 2;
            int myY = 0;
            int nextX = node.data.getX() + node.data.getWidth() / 2;
            int nextY = 0;

            switch (type) {
                case ANCESTOR:
                    myY = startNode.data.getY();
                    nextY = node.data.getY() + node.data.getHeight();
                    break;
                case DESCENDANTS:
                    myY = startNode.data.getY() + startNode.data.getHeight();
                    nextY = node.data.getY();
            }

            g2.drawLine(myX, myY, nextX, nextY);

            drawLines(node, g2);
        }
    }

}