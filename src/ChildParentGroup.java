import java.util.Collection;
import java.util.LinkedList;

public class ChildParentGroup {
    private Collection<Panel> children;
    private Collection<Panel> parents;

    public ChildParentGroup(Panel child, Collection<Panel> parents) {
        Collection<Panel> children = new LinkedList<>();
        children.add(child);

        this.children = children;
        this.parents = parents;
    }

    public Collection<Panel> getChildren() {
        return children;
    }

    public Collection<Panel> getParents() {
        return parents;
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
            if (!children.contains(child))
                children.add(child);
        }
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