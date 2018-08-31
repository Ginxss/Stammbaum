import java.util.LinkedList;

public class RelationList {
    private LinkedList<Relation> allRelations;
    private LinkedList<Relation> childRelations;

    public RelationList() {
        allRelations = new LinkedList<>();
        childRelations = new LinkedList<>();
    }

    public LinkedList<Relation> getAllRelations() {
        return allRelations;
    }

    public LinkedList<Relation> getChildRelations() {
        return childRelations;
    }

    public boolean add(Relation relation) {
        if (!hasRelation(relation.srcPanel, relation.targetPanel, relation.type)) {
            allRelations.add(relation);
            if (relation.type == Relation.Type.CHILD)
                childRelations.add(relation);

            return true;
        }
        return false;
    }

    public void remove(Relation relation) {
        if (relation.type == Relation.Type.CHILD) {
            if (childRelations.contains(relation))
                childRelations.remove(relation);
        }

        if (allRelations.contains(relation))
            allRelations.remove(relation);
    }

    public boolean remove(String srcName, String targetName, Relation.Type type) {
        for (Relation relation : allRelations) {
            if (relation.srcPanel.getPanelName().equals(srcName) && relation.targetPanel.getPanelName().equals(targetName) && relation.type == type) {
                remove(relation);
                return true;
            }
        }
        return false;
    }

    public boolean remove(Panel srcPanel, Panel targetPanel, Relation.Type type) {
        for (Relation relation : allRelations) {
            if (relation.srcPanel == srcPanel && relation.targetPanel == targetPanel && relation.type == type) {
                remove(relation);
                return true;
            }
        }
        return false;
    }

    public boolean hasRelation(Panel srcPanel, Panel targetPanel, Relation.Type type) {
        if (type == Relation.Type.CHILD) {
            for (Relation relation : childRelations) {
                if (relation.srcPanel == srcPanel && relation.targetPanel == targetPanel ||
                    relation.srcPanel == targetPanel && relation.targetPanel == srcPanel) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeRelationsWith(Panel panel) {
        for (int i = allRelations.size() - 1; i >= 0; i--) {
            Relation relation = allRelations.get(i);
            if (relation.srcPanel == panel || relation.targetPanel == panel) {
                remove(relation);

                ActionStack.appendRelationAction(false, relation.srcPanel.getPanelName(), relation.targetPanel.getPanelName(), relation.type);
            }
        }
    }
}
