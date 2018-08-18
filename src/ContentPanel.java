import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedList;

public class ContentPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private Content content;

    private LinkedList<ChildParentGroup> groups;

    private Point initialMousePos;
    private LinkedList<Point> panelPositions;

    private Rectangle selectionRectangle;
    private Rectangle normedSelectionRectangle;
    private boolean drawSelectionRectangle;

    private Panel createRelationSrcPanel;
    private Point createRelationTarget;
    public static boolean creatingRelation = false;

    private boolean drawBorder;

    public ContentPanel() {
        super(null);

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        content = new Content();

        groups = new LinkedList<>();

        initialMousePos = new Point();
        panelPositions = new LinkedList<>();

        selectionRectangle = new Rectangle();
        normedSelectionRectangle = new Rectangle();
        drawSelectionRectangle = false;

        createRelationTarget = new Point();

        drawBorder = false;
    }

    public LinkedList<Panel> getPanelList() {
        return content.getPanelList();
    }

    public RelationList getRelationList() {
        return content.getRelationList();
    }

    public LinkedList<Integer> getSelectedPanels() {
        return content.getSelectedPanels();
    }

    public Panel getPanel(int i) {
        return content.getPanel(i);
    }

    public Panel getPanel(String name) {
        return content.getPanel(name);
    }

    public Panel newPanel(String name, int x, int y) {
        Panel panel = content.newPanel(name, x, y);
        if (panel != null) {
            panel.setComponentPopupMenu(new PanelRightClickMenu(panel));
            add(panel);
        }
        return panel;
    }

    public Relation newRelation(Panel srcPanel, Panel targetPanel, Relation.Type type) {
        Relation relation = content.newRelation(srcPanel, targetPanel, type);
        updateChildParentGroups();
        return relation;
    }

    public boolean deletePanel(int i) {
        remove(content.getPanel(i));
        boolean result = content.deletePanel(i);
        updateChildParentGroups();
        return result;
    }

    public boolean deletePanel(String name) {
        remove(content.getPanel(name));
        boolean result = content.deletePanel(name);
        updateChildParentGroups();
        return result;
    }

    public boolean deleteRelation(String srcName, String targetName, Relation.Type type) {
        boolean result = content.deleteRelation(srcName, targetName, type);
        updateChildParentGroups();
        return result;
    }

    public void updateSelectedPanels() {
        content.updateSelectedPanels();
    }

    public void clear() {
        for (Panel panel : content.getPanelList())
            remove(panel);
        content.clear();
        updateChildParentGroups();
    }

    public void searchFor(String name) {
        Panel panel = content.getPanel(name);
        if (panel != null) {
            Point oldLocation = panel.getLocation();
            panel.setLocation(getWidth()/2 - panel.getWidth()/2, getHeight()/5);
            Point diff = new Point(panel.getX() - oldLocation.x, panel.getY() - oldLocation.y);
            for (Panel panel1 : content.getPanelList()) {
                if (panel1 != panel)
                    panel1.setLocation(panel1.getX() + diff.x, panel1.getY() + diff.y);
            }
        }
    }

    public BufferedImage takeSnapShot() {
        drawBorder = false;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        LinkedList<Point> orgPos = new LinkedList<>();

        for (Panel panel : content.getPanelList()) {
            orgPos.add(panel.getLocation());

            if (panel.getX() < minX)
                minX = panel.getX();
            if (panel.getY() < minY)
                minY = panel.getY();
        }

        for (Panel panel : content.getPanelList())
            panel.setLocation(panel.getX() - minX, panel.getY() - minY);

        int orgWidth = getWidth();
        int orgHeight = getHeight();
        int maxWidth = orgWidth;
        int maxHeight = orgHeight;
        for (Panel panel : content.getPanelList()) {
            if (panel.getX() + panel.getWidth() > maxWidth)
                maxWidth = panel.getX() + panel.getWidth();
            if (panel.getY() + panel.getHeight() > maxHeight)
                maxHeight = panel.getY() + panel.getHeight();
        }

        setSize(maxWidth, maxHeight);
        BufferedImage img = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB);

        paint(img.createGraphics());

        for (int i = 0; i < content.getPanelList().size(); i++)
            content.getPanel(i).setLocation(orgPos.get(i));

        setSize(orgWidth, orgHeight);

        return img;
    }

    public Point getPointOnCanvas() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (Panel panel : content.getPanelList()) {
            if (panel.getX() < minX)
                minX = panel.getX();
            if (panel.getY() < minY)
                minY = panel.getY();
        }

        int x = getWidth() / 2 - minX;
        int y = getHeight() / 2 - minY;
        return new Point(x, y);
    }

    // evtl. performanter machen?
    private void updateChildParentGroups() {
        groups.clear();

        LinkedList<Relation> usedChildRelations = new LinkedList<>();

        for (Relation relation : content.getRelationList().getChildRelations()) {
            if (!usedChildRelations.contains(relation)) {
                Panel c = relation.srcPanel;
                Collection<Panel> parents = new LinkedList<>();
                parents.add(relation.targetPanel);

                for (Relation relation1 : content.getRelationList().getChildRelations()) {
                    if (relation != relation1) {
                        if (relation.srcPanel == relation1.srcPanel) {
                            parents.add(relation1.targetPanel);
                            usedChildRelations.add(relation1);
                        }
                    }
                }

                ChildParentGroup group = new ChildParentGroup(c, parents);
                groups.add(group);
            }
        }

        for (int i = 0; i < groups.size(); i++) {
            ChildParentGroup group = groups.get(i);

            for (int j = i + 1; j < groups.size(); j++) {
                ChildParentGroup group1 = groups.get(j);

                if (group.hasSameParents(group1)) {
                    group.mergeChildren(group1);
                    groups.remove(j);
                    j--;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawRelations(g2);

        drawSelectionRectangle(g2);

        drawCreatingRelation(g2);

        if (drawBorder) {
            g2.setColor(Color.black);
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
    }

    // performanter machen. Ist potentiell immer noch ein Problem...
    private void drawRelations(Graphics2D g2) {
        for (ChildParentGroup group : groups) {
            g2.setColor(Color.blue);
            for (Point point : group.getParentNodes())
                g2.drawLine(point.x, point.y, group.getParentMiddle().x, group.getParentMiddle().y);

            if (group.getParentMiddle().y > group.getChildMiddle().y) {
                g2.setColor(Color.red);
                g2.setStroke(new BasicStroke(2));
            }
            else {
                g2.setColor(Color.blue);
                g2.setStroke(new BasicStroke(1));
            }

            g2.drawLine(group.getParentMiddle().x, group.getParentMiddle().y, group.getChildMiddle().x, group.getChildMiddle().y);

            g2.setColor(Color.black);
            g2.setStroke(new BasicStroke(1));
            for (Point point : group.getChildNodes())
                g2.drawLine(point.x, point.y, group.getChildMiddle().x, group.getChildMiddle().y);
        }
    }

    private void drawSelectionRectangle(Graphics2D g2) {
        if (drawSelectionRectangle) {
            int x = selectionRectangle.x;
            int y = selectionRectangle.y;
            int w = selectionRectangle.width;
            int h = selectionRectangle.height;
            int drawX = x;
            int drawY = y;
            int drawW = w;
            int drawH = h;

            if (w < 0 && h < 0) {
                drawX = x + w;
                drawY = y + h;
                drawW = -w;
                drawH = -h;
            }
            else if (h < 0) {
                drawY = y + h;
                drawH = -h;
            }
            else if (w < 0) {
                drawX = x + w;
                drawW = -w;
            }

            normedSelectionRectangle.setBounds(drawX, drawY, drawW, drawH);

            g2.setColor(Color.black);
            g2.drawRect(drawX, drawY, drawW, drawH);
            g2.setColor(new Color(50, 50, 50, 50));
            g2.fillRect(drawX, drawY, drawW, drawH);
        }
    }

    // Creating Relation Type-Check?
    private void drawCreatingRelation(Graphics2D g2) {
        if (creatingRelation) {
            int srcX = createRelationSrcPanel.getX() + createRelationSrcPanel.getWidth() / 2;
            int srcY = createRelationSrcPanel.getY() + createRelationSrcPanel.getHeight() / 2;
            int targetX = createRelationTarget.x = MouseInfo.getPointerInfo().getLocation().x - getLocationOnScreen().x;
            int targetY = createRelationTarget.y = MouseInfo.getPointerInfo().getLocation().y - getLocationOnScreen().y;

            g2.setColor(Color.gray);
            g2.drawLine(srcX, srcY, targetX, targetY);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (creatingRelation) {
                creatingRelation = false;
                if (e.getSource() instanceof Panel) {
                    Panel targetPanel = (Panel)e.getSource();
                    if (createRelationSrcPanel != targetPanel)
                        content.newRelation(createRelationSrcPanel, targetPanel, Relation.Type.CHILD);
                }
                repaint();
            }
            else {
                for (Panel panel : content.getPanelList())
                    panel.unselect();
                content.getSelectedPanels().clear();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e)) {
            initialMousePos.x = e.getXOnScreen();
            initialMousePos.y = e.getYOnScreen();

            // Aus Performancegünden werden die bereits vorhandenen Positionen nur bearbeitet.
            for (int i = 0; i < content.getPanelList().size(); i++) {
                if (i < panelPositions.size()) {
                    panelPositions.get(i).x = content.getPanel(i).getX();
                    panelPositions.get(i).y = content.getPanel(i).getY();
                }
                else {
                    panelPositions.add(content.getPanel(i).getLocation());
                }
            }

            content.updateSelectedPanels();
        }
        else if (SwingUtilities.isLeftMouseButton(e)) {
            if (!creatingRelation) {
                drawSelectionRectangle = true;
                selectionRectangle.x = e.getX();
                selectionRectangle.y = e.getY();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            for (Panel panel : content.getPanelList()) {
                if (!panel.isSelected()) {
                    int middleX = panel.getX() + panel.getWidth() / 2;
                    int middleY = panel.getY() + panel.getHeight() / 2;

                    if (middleX > normedSelectionRectangle.x && middleX < normedSelectionRectangle.x + normedSelectionRectangle.width &&
                        middleY > normedSelectionRectangle.y && middleY < normedSelectionRectangle.y + normedSelectionRectangle.height) {
                        panel.select();
                    }
                }
            }

            drawSelectionRectangle = false;
            repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO: Hier nur zu Point-Lists der Groups diffX und diffY dazurechnen.
        if (SwingUtilities.isMiddleMouseButton(e)) {
            int diffX = e.getXOnScreen() - initialMousePos.x;
            int diffY = e.getYOnScreen() - initialMousePos.y;

            // Schöner machen
            if (content.getSelectedPanels().isEmpty()) {
                for (int i = 0; i < content.getPanelList().size(); i++) {
                    Point panelPos = panelPositions.get(i);
                    content.getPanel(i).setLocation(panelPos.x + diffX, panelPos.y + diffY);
                }
            }
            else {
                for (int i : content.getSelectedPanels()) {
                    Point panelPos = panelPositions.get(i);
                    content.getPanel(i).setLocation(panelPos.x + diffX, panelPos.y + diffY);
                }
            }

            for (ChildParentGroup group : groups)
                group.update();
        }
        else if (SwingUtilities.isLeftMouseButton(e)) {
            if (!creatingRelation) {
                selectionRectangle.width = e.getX() - selectionRectangle.x;
                selectionRectangle.height = e.getY() - selectionRectangle.y;
            }
        }

        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (creatingRelation)
            repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {}

    private boolean renamePanelDialog(String oldName) {
        JTextField field = new JTextField();
        field.addAncestorListener(new RequestFocusListener());

        JComponent[] inputs = new JComponent[] {new JLabel("Neuer Name:"), field};

        int option = JOptionPane.showConfirmDialog(getTopLevelAncestor(), inputs, "Umbennen", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = field.getText();
            if (!name.equals("")) {
                content.renamePanel(oldName, name);
                repaint();
                return true;
            }
        }

        return false;
    }

    class PanelRightClickMenu extends JPopupMenu {
        private JMenuItem deletePanel, childRelationTo, renamePanel, displayAncestors, displayDescendants;

        public PanelRightClickMenu(Panel panel) {
            deletePanel = new JMenuItem("Löschen");
            deletePanel.addActionListener((e) -> {
                deletePanel(panel.getName());
                ContentPanel.this.repaint();
            });

            renamePanel = new JMenuItem("Umbenennen");
            renamePanel.addActionListener((e) -> renamePanelDialog(panel.getName()));

            displayAncestors = new JMenuItem("Vorfahren anzeigen");
            displayAncestors.addActionListener((e) -> {
                Container p = ContentPanel.this;
                while (!(p instanceof JFrame))
                    p = p.getParent();

                new FamilyDialog((JFrame)p, panel, content.getRelationList(), FamilyDialog.Type.ANCESTOR);
            });

            displayDescendants = new JMenuItem("Nachfahren anzeigen");
            displayDescendants.addActionListener((e) -> {
                Container p = ContentPanel.this;
                while (!(p instanceof JFrame))
                    p = p.getParent();

                new FamilyDialog((JFrame)p, panel, content.getRelationList(), FamilyDialog.Type.DESCENDANTS);
            });

            childRelationTo = new JMenuItem("ist Kind von ...");
            childRelationTo.addActionListener((e) -> {
                createRelationSrcPanel = panel;
                creatingRelation = true;
            });

            add(deletePanel);
            add(renamePanel);
            add(displayAncestors);
            add(displayDescendants);
            add(childRelationTo);
        }
    }
}
