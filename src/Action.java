import java.awt.*;

interface Action {
    String getType();
}

class PanelAction implements Action {
    public boolean add;
    public String object;
    public Point position;

    public PanelAction(boolean add, String object, Point position) {
        this.add = add;
        this.object = object;
        this.position = position;
    }

    public String getType() {
        return "Panel";
    }
}

class RelationAction implements Action {
    public boolean add;
    public String srcName, targetName;
    public Relation.Type relationType;

    public RelationAction(boolean add, String srcName, String targetName, Relation.Type relationType) {
        this.add = add;
        this.srcName = srcName;
        this.targetName = targetName;
        this.relationType = relationType;
    }

    public String getType() {
        return "Relation";
    }
}

class RenameAction implements Action {
    public String oldName, newName;

    public RenameAction(String oldName, String newName) {
        this.oldName = oldName;
        this.newName = newName;
    }

    public String getType() {
        return "Rename";
    }
}