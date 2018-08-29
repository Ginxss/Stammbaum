import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;

public class ChildParentGroup {
    private Collection<Panel> children;
    private Collection<Panel> parents;

    private LinkedList<Point> childNodes;
    private LinkedList<Point> parentNodes;
    private Point childMiddle;
    private Point parentMiddle;

    private LinkedList<Point> orgChildNodes;
    private LinkedList<Point> orgParentNodes;
    private Point orgChildMiddle;
    private Point orgParentMiddle;

    public ChildParentGroup(Panel child, Collection<Panel> parents) {
        this.children = new LinkedList<>();
        children.add(child);
        this.parents = parents;

        childNodes = new LinkedList<>();
        int childX = child.getX() + child.getWidth() / 2;
        int childY = child.getY();
        childNodes.add(new Point(childX, childY));
        childMiddle = new Point(childX, childY);

        parentNodes = new LinkedList<>();
        parentMiddle = new Point();
        updateParentNodes();

        orgChildNodes = new LinkedList<>();
        orgParentNodes = new LinkedList<>();
        orgChildMiddle = new Point();
        orgParentMiddle = new Point();
    }

    public Collection<Panel> getChildren() {
        return children;
    }

    public Collection<Panel> getParents() {
        return parents;
    }

    public LinkedList<Point> getChildNodes() {
        return childNodes;
    }

    public LinkedList<Point> getParentNodes() {
        return parentNodes;
    }

    public Point getChildMiddle() {
        return childMiddle;
    }

    public Point getParentMiddle() {
        return parentMiddle;
    }

    public boolean hasSameParents(ChildParentGroup other) {
        if (parents.size() == other.getParents().size()) {
            if (parents.containsAll(other.getParents()) && other.getParents().containsAll(parents)) {
                return true;
            }
        }
        return false;
    }

    public void mergeChildren(ChildParentGroup other) {
        for (Panel child : other.getChildren()) {
            if (!children.contains(child)) {
                children.add(child);
            }
        }

        update();
    }

    public void update() {
        updateParentNodes();
        updateChildNodes();
    }

    private void updateChildNodes() {
        childNodes.clear();
        childMiddle.x = 0;
        childMiddle.y = Integer.MAX_VALUE;
        int i = 0;
        for (Panel panel : children) {
            int childX = panel.getX() + panel.getWidth() / 2;
            int childY = panel.getY();
            childNodes.add(new Point(childX, childY));

            childMiddle.x += childX;
            i++;

            if (childY < childMiddle.y)
                childMiddle.y = childY;
        }
        childMiddle.x /= i;

        if (childMiddle.y < parentMiddle.y + 40)
            childMiddle.y = parentMiddle.y + 40;

        if (children.size() > 1)
            childMiddle.y -= 20;
    }

    private void updateParentNodes() {
        parentNodes.clear();
        parentMiddle.x = 0;
        parentMiddle.y = Integer.MIN_VALUE;
        int i = 0;
        for (Panel panel : parents) {
            int parentX = panel.getX() + panel.getWidth() / 2;
            int parentY = panel.getY() + panel.getHeight();
            parentNodes.add(new Point(parentX, parentY));

            parentMiddle.x += parentX;
            i++;

            if (parentY > parentMiddle.y)
                parentMiddle.y = parentY;
        }
        parentMiddle.x /= i;

        if (parents.size() > 1)
            parentMiddle.y += 20;
    }


    public void setOrgPositions() {
        orgChildNodes.clear();
        for (int i = 0; i < childNodes.size(); i++)
            orgChildNodes.add(new Point(childNodes.get(i).x, childNodes.get(i).y));

        orgParentNodes.clear();
        for (int i = 0; i < parentNodes.size(); i++)
            orgParentNodes.add(new Point(parentNodes.get(i).x, parentNodes.get(i).y));

        orgChildMiddle = new Point(childMiddle);
        orgParentMiddle = new Point(parentMiddle);
    }

    public void applyDiff(int diffX, int diffY) {
        for (int i = 0; i < childNodes.size(); i++) {
            Point orgPos = orgChildNodes.get(i);
            childNodes.get(i).setLocation(orgPos.x + diffX, orgPos.y  + diffY);
        }

        for (int i = 0; i < parentNodes.size(); i++) {
            Point orgPos = orgParentNodes.get(i);
            parentNodes.get(i).setLocation(orgPos.x + diffX, orgPos.y + diffY);
        }

        childMiddle.x = orgChildMiddle.x + diffX;
        childMiddle.y = orgChildMiddle.y + diffY;

        parentMiddle.x = orgParentMiddle.x + diffX;
        parentMiddle.y = orgParentMiddle.y + diffY;
    }

    @Override
    public String toString() {
        String s = "Kinder: ";
        for (Panel panel : children)
            s += panel.getPanelName() + " ";
        s += "| von Eltern: ";
        for (Panel panel : parents)
            s += panel.getPanelName() + " ";

        return s;
    }
}