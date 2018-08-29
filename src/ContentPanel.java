import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class ContentPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private Content content;

    private LinkedList<ChildParentGroup> groups;

    private Point initialMousePos;
    private ArrayList<Point> panelPositions;

    private Rectangle selectionRectangle;
    private Rectangle normedSelectionRectangle;
    private boolean drawSelectionRectangle;

    private Panel createRelationSrcPanel;
    private Point createRelationTarget;
    public static boolean creatingRelation = false;

    private boolean antialiasing;
    private boolean takingSnapshot;

    private JLabel statusLabel;
    private String statusText;

    public ContentPanel(JLabel statusLabel) {
        super(null);

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        content = new Content();

        groups = new LinkedList<>();

        initialMousePos = new Point();
        panelPositions = new ArrayList<>();

        selectionRectangle = new Rectangle();
        normedSelectionRectangle = new Rectangle();
        drawSelectionRectangle = false;

        createRelationTarget = new Point();

        antialiasing = false;
        takingSnapshot = false;

        this.statusLabel = statusLabel;

        statusText = "Anzahl Personen: " + content.getPanelList().size();
    }

    public void updateStatus() {
        statusLabel.setText(statusText);
    }

    public void updateStatusText() {
        statusText = "Anzahl Personen: " + content.getPanelList().size();
    }

    public Content getContent() {
        return content;
    }

    public LinkedList<Panel> getPanelList() {
        return content.getPanelList();
    }

    public RelationList getRelationList() {
        return content.getRelationList();
    }

    public Panel getPanel(String name) {
        return content.getPanel(name);
    }

    public boolean getAntilasing() {
        return antialiasing;
    }

    public Panel newPanel(String name, int x, int y) {
        Panel panel = content.newPanel(name, x, y);
        if (panel != null) {
            addMenu(panel);
            add(panel);

            ActionStack.addPanelAction(true, name, new Point(x, y));

            updateStatusText();
        }
        return panel;
    }

    public void addMenu(Panel panel) {
        panel.setComponentPopupMenu(new PanelRightClickMenu(panel));
    }

    public Relation newRelation(Panel srcPanel, Panel targetPanel, Relation.Type type) {
        Relation relation = content.newRelation(srcPanel, targetPanel, type);
        updateChildParentGroups();

        ActionStack.addRelationAction(true, srcPanel.getPanelName(), targetPanel.getPanelName(), type);
        return relation;
    }

    public boolean deletePanel(int i, boolean appendAction) {
        if (appendAction)
            ActionStack.appendPanelAction(false, content.getPanel(i).getPanelName(), content.getPanel(i).getLocation());
        else
            ActionStack.addPanelAction(false, content.getPanel(i).getPanelName(), content.getPanel(i).getLocation());

        remove(content.getPanel(i));
        boolean result = content.deletePanel(i);
        updateChildParentGroups();

        updateStatusText();

        return result;
    }

    public boolean deletePanel(String name) {
        return deletePanel(name, false);
    }

    public boolean deletePanel(String name, boolean appendAction) {
        if (appendAction)
            ActionStack.appendPanelAction(false, name, content.getPanel(name).getLocation());
        else
            ActionStack.addPanelAction(false, name, content.getPanel(name).getLocation());

        remove(content.getPanel(name));
        boolean result = content.deletePanel(name);
        updateChildParentGroups();

        updateStatusText();

        return result;
    }

    public boolean deleteRelation(String srcName, String targetName, Relation.Type type) {
        boolean result = content.deleteRelation(srcName, targetName, type);
        updateChildParentGroups();

        ActionStack.addRelationAction(false, srcName, targetName, type);
        return result;
    }

    public void deleteSelected() {
        content.updateSelectedPanels();
        for (int i = content.getSelectedPanels().size() - 1; i >= 0; i--) {
            int pos = content.getSelectedPanels().get(i);

            if (i == content.getSelectedPanels().size() - 1)
                deletePanel(pos, false);
            else
                deletePanel(pos, true);
        }

        repaint();
        revalidate();
    }

    public void clear() {
        for (Panel panel : content.getPanelList())
            panel.select();
        deleteSelected();

        content.getSelectedPanels().clear();
        updateChildParentGroups();

        repaint();
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
            updateChildParentGroups();
        }
    }

    public BufferedImage takeSnapShot() {
        takingSnapshot = true;
        boolean orgAntialiasing = antialiasing;
        antialiasing = true;

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
        for (ChildParentGroup group : groups)
            group.update();

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

        antialiasing = orgAntialiasing;
        takingSnapshot = false;

        for (int i = 0; i < content.getPanelList().size(); i++)
            content.getPanel(i).setLocation(orgPos.get(i));
        for (ChildParentGroup group : groups)
            group.update();

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

    public void updateChildParentGroups() {
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
                    groups.remove(j--);
                }
            }
        }

        for (Panel panel : content.getPanelList())
            panel.clearGroups();

        for (ChildParentGroup group : groups) {
            for (Panel panel : group.getChildren())
                panel.addGroup(group);
            for (Panel panel : group.getParents())
                panel.addGroup(group);
        }
    }

    public void toggleAntialiasing() {
        antialiasing = !antialiasing;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        Object ant = (antialiasing) ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, ant);

        if (takingSnapshot) {
            g2.setStroke(new BasicStroke(2));
            drawRelations(g2);
        }
        else {
            g2.setStroke(new BasicStroke(1));
            drawRelations(g2);
            drawSelectionRectangle(g2);
            drawCreatingRelation(g2);

            g2.setColor(Color.black);
            g2.drawLine(0, 0, 0, getHeight());
            g2.drawLine(getWidth() - 1,  0, getWidth() - 1, getHeight());

            updateStatus();
        }

    }

    private void drawRelations(Graphics2D g2) {
        for (ChildParentGroup group : groups) {
            g2.setColor(Color.green.darker());
            for (Point point : group.getParentNodes()) {
                g2.drawLine(point.x, point.y, point.x, group.getParentMiddle().y);
                g2.drawLine(point.x, group.getParentMiddle().y, group.getParentMiddle().x, group.getParentMiddle().y);
            }

            g2.drawLine(group.getParentMiddle().x, group.getParentMiddle().y, group.getChildMiddle().x, group.getChildMiddle().y);

            g2.setColor(Color.black);
            for (Point point : group.getChildNodes()) {
                if (point.y < group.getChildMiddle().y) {
                    g2.setColor(Color.red);
                    g2.setStroke(new BasicStroke(2));
                }
                g2.drawLine(point.x, point.y + 1, point.x, group.getChildMiddle().y);

                if (g2.getColor() != Color.black) {
                    g2.setColor(Color.black);
                    g2.setStroke(new BasicStroke(1));
                }
                g2.drawLine(point.x, group.getChildMiddle().y, group.getChildMiddle().x, group.getChildMiddle().y);
            }
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

            g2.setColor(Panel.getColor().darker().darker());
            g2.drawRect(drawX, drawY, drawW, drawH);
            g2.setColor(new Color(Panel.getColor().getRed(), Panel.getColor().getGreen(), Panel.getColor().getBlue(), 50));
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
                if (e.getSource() instanceof Panel) {
                    Panel targetPanel = (Panel)e.getSource();
                    if (createRelationSrcPanel != targetPanel)
                        newRelation(createRelationSrcPanel, targetPanel, Relation.Type.CHILD);
                }
                else {
                    creatingRelation = false;
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

            for (ChildParentGroup group : groups)
                group.setOrgPositions();
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
        if (SwingUtilities.isMiddleMouseButton(e)) {
            int diffX = e.getXOnScreen() - initialMousePos.x;
            int diffY = e.getYOnScreen() - initialMousePos.y;

            if (content.getSelectedPanels().isEmpty()) {
                for (int i = 0; i < content.getPanelList().size(); i++) {
                    Point panelPos = panelPositions.get(i);
                    content.getPanel(i).setLocation(panelPos.x + diffX, panelPos.y + diffY);
                }

                for (ChildParentGroup group : groups)
                    group.applyDiff(diffX, diffY);
            }
            else {
                LinkedList<ChildParentGroup> groupsToUpdate = new LinkedList<>();

                for (int i : content.getSelectedPanels()) {
                    Point panelPos = panelPositions.get(i);
                    Panel panel = content.getPanel(i);
                    panel.setLocation(panelPos.x + diffX, panelPos.y + diffY);

                    for (ChildParentGroup group : panel.getGroups()) {
                        if (!groupsToUpdate.contains(group))
                            groupsToUpdate.add(group);
                    }
                }

                for (ChildParentGroup group : groupsToUpdate)
                    group.update();
            }
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

    public void requestFocus(JTextField field) {
        field.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent ancestorEvent) {
                field.requestFocusInWindow();
            }

            @Override
            public void ancestorRemoved(AncestorEvent ancestorEvent) {}
            @Override
            public void ancestorMoved(AncestorEvent ancestorEvent) {}
        });

        field.addFocusListener(new FocusAdapter() {
            private boolean firstTime = true;
            @Override
            public void focusLost(FocusEvent focusEvent) {
                if (firstTime) {
                    field.requestFocusInWindow();
                    firstTime = false;
                }
            }
        });
    }

    private boolean renamePanelDialog(String oldName) {
        JTextField field = new JTextField();
        requestFocus(field);

        JComponent[] inputs = new JComponent[] {new JLabel("Neuer Name:"), field};

        int option = JOptionPane.showConfirmDialog(getTopLevelAncestor(), inputs, "Umbennen", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = field.getText();
            if (!name.equals("")) {
                content.renamePanel(oldName, name);

                ActionStack.addRenameAction(oldName, name);
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
                deletePanel(panel.getPanelName());
                ContentPanel.this.repaint();
            });

            renamePanel = new JMenuItem("Umbenennen");
            renamePanel.addActionListener((e) -> renamePanelDialog(panel.getPanelName()));

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
