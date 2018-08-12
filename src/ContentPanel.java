import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedList;

public class ContentPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private LinkedList<Panel> panelList;
    private RelationList relationList;

    private Point initialMousePos;
    private LinkedList<Point> panelPositions;

    private LinkedList<Integer> selectedPanels;

    private Rectangle selectionRectangle;
    private Rectangle normedSelectionRectangle;
    private boolean drawSelectionRectangle;

    private Panel createRelationSrcPanel;
    private Point createRelationTarget;
    public static boolean creatingRelation = false;

    public ContentPanel() {
        super(null);

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        panelList = new LinkedList<>();
        relationList = new RelationList();

        initialMousePos = new Point();
        panelPositions = new LinkedList<>();

        selectedPanels = new LinkedList<>();

        selectionRectangle = new Rectangle();
        normedSelectionRectangle = new Rectangle();
        drawSelectionRectangle = false;

        createRelationTarget = new Point();
    }

    public Panel getPanel(int i) {
        return panelList.get(i);
    }

    public Panel getPanel(String name) {
        for (Panel panel : panelList) {
            if (panel.getName().equals(name)) {
                return panel;
            }
        }
        return null;
    }

    public LinkedList<Panel> getPanelList() {
        return panelList;
    }

    public RelationList getRelationList() {
        return relationList;
    }

    public LinkedList<Integer> getSelectedPanels() {
        return selectedPanels;
    }

    public Panel newPanel(String name, int x, int y) {
        for (Panel panel : panelList) {
            if (panel.getName().equals(name))
                return null;
        }

        Panel panel = new Panel(name, x, y);
        panel.setComponentPopupMenu(new PanelRightClickMenu(panel));
        panelList.add(panel);
        add(panel);
        return panel;
    }

    public Relation newRelation(Panel srcPanel, Panel targetPanel, Relation.Type type) {
        if (relationList.hasRelation(srcPanel, targetPanel, type))
            return null;

        Relation relation = new Relation(srcPanel, targetPanel, Relation.Type.CHILD);
        relationList.add(relation);
        return relation;
    }

    public boolean deletePanel(int j) {
        Panel panel = getPanel(j);
        return delete_panel(panel);
    }

    public boolean deletePanel(String name) {
        Panel panel = getPanel(name);
        return delete_panel(panel);
    }

    private boolean delete_panel(Panel panel) {
        if (panel != null) {
            relationList.removeRelationsWith(panel);
            panelList.remove(panel);
            remove(panel);
            return true;
        }
        return false;
    }

    public boolean deleteRelation(String srcName, String targetName, Relation.Type type) {
        return relationList.remove(srcName, targetName, type);
    }

    public boolean renamePanel(String oldName, String newName) {
        Panel panel = getPanel(oldName);
        if (panel != null) {
            if (!nameExists(newName))
                panel.setName(newName);
        }
        return false;
    }

    public void clear() {
        relationList.clear();

        for (Panel panel : panelList)
            remove(panel);
        panelList.clear();
    }

    public void updateSelectedPanels() {
        // Was ist performanter???
        /*selectedPanels.clear();
        for (int i = 0; i < panelList.size(); i++) {
            Panel panel = panelList.get(i);
            if (panel.isSelected())
                selectedPanels.add(i);
        }*/

        for (int i = 0; i < selectedPanels.size(); i++) {
            int pos = selectedPanels.get(i);
            if (!panelList.get(pos).isSelected()) {
                selectedPanels.remove(pos);
            }
        }

        for (int i = 0; i < panelList.size(); i++) {
            Panel panel = panelList.get(i);
            if (panel.isSelected() && !panelPositions.contains(i)) {
                selectedPanels.add(i);
            }
        }
    }

    public boolean nameExists(String name) {
        for (Panel panel : panelList) {
            if (panel.getName().equals(name))
                return true;
        }
        return false;
    }

    public void searchFor(String name) {
        Panel panel = getPanel(name);
        if (panel != null) {
            Point oldLocation = panel.getLocation();
            panel.setLocation(getWidth()/2 - panel.getWidth()/2, getHeight()/5);
            Point diff = new Point(panel.getX() - oldLocation.x, panel.getY() - oldLocation.y);
            for (Panel panel1 : panelList) {
                if (panel1 != panel)
                    panel1.setLocation(panel1.getX() + diff.x, panel1.getY() + diff.y);
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

        g2.setColor(Color.black);
        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

    // Performanter machen.
    private void drawRelations(Graphics2D g2) {
        LinkedList<Relation> usedChildRelations = new LinkedList<>();
        LinkedList<ChildParentGroup> groups = new LinkedList<>();

        for (Relation relation : relationList.getChildRelations()) {
            if (!usedChildRelations.contains(relation)) {
                Panel c = relation.srcPanel;
                Collection<Panel> parents = new LinkedList<>();
                parents.add(relation.targetPanel);

                for (Relation relation1 : relationList.getChildRelations()) {
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

        for (ChildParentGroup group : groups) {
            LinkedList<Point> childNodes = new LinkedList<>();
            for (Panel panel : group.getChildren()) {
                int childX = panel.getX() + panel.getWidth() / 2;
                int childY = panel.getY();
                childNodes.add(new Point(childX, childY));
            }

            Point childMiddle = new Point();
            childMiddle.y = Integer.MAX_VALUE;
            int j = 0;
            for (Point point : childNodes) {
                if (point.y < childMiddle.y)
                    childMiddle.y = point.y;
                childMiddle.x += point.x;
                j++;
            }
            childMiddle.x /= j;
            childMiddle.y -= 0;

            LinkedList<Point> parentNodes = new LinkedList<>();
            for (Panel panel : group.getParents()) {
                int parentX = panel.getX() + panel.getWidth() / 2;
                int parentY = panel.getY() + panel.getHeight();
                parentNodes.add(new Point(parentX, parentY - 1));
            }

            Point parentMiddle = new Point();
            parentMiddle.y = Integer.MIN_VALUE;
            int i = 0;
            for (Point point : parentNodes) {
                if (point.y > parentMiddle.y)
                    parentMiddle.y = point.y;
                parentMiddle.x += point.x;
                i++;
            }
            parentMiddle.x /= i;
            parentMiddle.y += (childMiddle.y - parentMiddle.y) / 5;

            g2.setColor(Color.blue);
            for (Point point : parentNodes)
                g2.drawLine(point.x, point.y, parentMiddle.x, parentMiddle.y);

            if (parentMiddle.y > childMiddle.y) {
                g2.setColor(Color.red);
                g2.setStroke(new BasicStroke(2));
            }
            else {
                g2.setColor(Color.blue);
                g2.setStroke(new BasicStroke(1));
            }

            g2.drawLine(parentMiddle.x, parentMiddle.y, childMiddle.x, childMiddle.y);

            g2.setColor(Color.black);
            g2.setStroke(new BasicStroke(1));
            for (Point point : childNodes)
                g2.drawLine(point.x, point.y, childMiddle.x, childMiddle.y);
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
                drawX = x;
                drawY = y + h;
                drawW = w;
                drawH = -h;
            }
            else if (w < 0) {
                drawX = x + w;
                drawY = y;
                drawW = -w;
                drawH = h;
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
                        newRelation(createRelationSrcPanel, targetPanel, Relation.Type.CHILD);
                }
                repaint();
            }
            else {
                for (Panel panel : panelList)
                    panel.unselect();
                selectedPanels.clear();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e)) {
            initialMousePos.x = e.getXOnScreen();
            initialMousePos.y = e.getYOnScreen();

            // Aus Performancegünden werden die bereits vorhandenen Positionen nur bearbeitet.
            for (int i = 0; i < panelList.size(); i++) {
                if (i < panelPositions.size()) {
                    panelPositions.get(i).x = panelList.get(i).getX();
                    panelPositions.get(i).y = panelList.get(i).getY();
                }
                else {
                    panelPositions.add(panelList.get(i).getLocation());
                }
            }

            updateSelectedPanels();
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
            for (Panel panel : panelList) {
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

            if (selectedPanels.isEmpty()) {
                for (int i = 0; i < panelList.size(); i++) {
                    Point panelPos = panelPositions.get(i);
                    panelList.get(i).setLocation(panelPos.x + diffX, panelPos.y + diffY);
                }
            }
            else {
                for (int i : selectedPanels) {
                    Point panelPos = panelPositions.get(i);
                    panelList.get(i).setLocation(panelPositions.get(i).x + diffX, panelPositions.get(i).y + diffY);
                }
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
    public void mouseWheelMoved(MouseWheelEvent e) {
        // TODO: Zoom ContentPanel
    }

    private boolean renamePanelDialog(String oldName) {
        JTextField field = new JTextField();
        field.addAncestorListener(new RequestFocusListener());

        JComponent[] inputs = new JComponent[] {new JLabel("Neuer Name:"), field};

        int option = JOptionPane.showConfirmDialog(getTopLevelAncestor(), inputs, "Umbennen", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = field.getText();
            if (!name.equals("")) {
                renamePanel(oldName, name);
                repaint();
                return true;
            }
        }

        return false;
    }

    public BufferedImage takeSnapShot(){
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        LinkedList<Point> orgPos = new LinkedList<>();

        for (Panel panel : panelList) {
            orgPos.add(panel.getLocation());

            if (panel.getX() < minX)
                minX = panel.getX();
            if (panel.getY() < minY)
                minY = panel.getY();
        }

        for (Panel panel : panelList)
            panel.setLocation(panel.getX() - minX, panel.getY() - minY);

        int orgWidth = getWidth();
        int orgHeight = getHeight();
        int maxWidth = orgWidth;
        int maxHeight = orgHeight;
        for (Panel panel : panelList) {
            if (panel.getX() + panel.getWidth() > maxWidth)
                maxWidth = panel.getX() + panel.getWidth();
            if (panel.getY() + panel.getHeight() > maxHeight)
                maxHeight = panel.getY() + panel.getHeight();
        }

        setSize(maxWidth, maxHeight);
        BufferedImage img = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB);

        paint(img.createGraphics());

        for (int i = 0; i < panelList.size(); i++)
            panelList.get(i).setLocation(orgPos.get(i));

        setSize(orgWidth, orgHeight);

        return img;
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

                FamilyDialog fd = new FamilyDialog((JFrame)p, panel, relationList, FamilyDialog.Type.ANCESTOR);
            });

            displayDescendants = new JMenuItem("Nachfahren anzeigen");
            displayDescendants.addActionListener((e) -> {
                Container p = ContentPanel.this;
                while (!(p instanceof JFrame))
                    p = p.getParent();

                FamilyDialog fd = new FamilyDialog((JFrame)p, panel, relationList, FamilyDialog.Type.DESCENDANTS);
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
