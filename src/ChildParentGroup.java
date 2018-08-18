import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;

public class ChildParentGroup {
    private Collection<Panel> children;
    private Collection<Panel> parents;

    // TODO: Hiermit performanter machen?
    private LinkedList<Point> childNodes;
    private LinkedList<Point> parentNodes;
    private Point childMiddle;
    private Point parentMiddle;

    public ChildParentGroup(Panel child, Collection<Panel> parents) {
        this.children = new LinkedList<>();
        children.add(child);
        this.parents = parents;

        for (Panel panel : parents)
            panel.setGroup(this);
        child.setGroup(this);

        childNodes = new LinkedList<>();
        int childX = child.getX() + child.getWidth() / 2;
        int childY = child.getY();
        childNodes.add(new Point(childX, childY));
        childMiddle = new Point(childX, childY);

        parentNodes = new LinkedList<>();
        parentMiddle = new Point();
        updateParentNodes();
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
                child.setGroup(this);
            }
        }

        update();
    }

    public void update() {
        updateChildNodes();
        updateParentNodes();
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

            if (childY < childMiddle.y)
                childMiddle.y = childY;

            i++;
        }
        childMiddle.x /= i;
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

            if (parentY > parentMiddle.y)
                parentMiddle.y = parentY;

            i++;
        }
        parentMiddle.x /= i;
        //parentMiddle.y += (childMiddle.y - parentMiddle.y) / 5;
    }

    public void applyDiff(int diffX, int diffY) {
        /*for (Point point : childNodes) {
            point.x += diffX;
            point.y += diffY;
        }

        for (Point point : parentNodes) {
            point.x += diffX;
            point.y += diffY;
        }*/

        childMiddle.x += diffX;
        childMiddle.y += diffY;

        parentMiddle.x += diffX;
        parentMiddle.y += diffY;
    }

    @Override
    public String toString() {
        String s = "Kinder: ";
        for (Panel panel : children)
            s += panel.getName() + " ";
        s += "| von Eltern: ";
        for (Panel panel : parents)
            s += panel.getName() + " ";

        return s;
    }
}