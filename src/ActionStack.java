import java.awt.*;
import java.util.LinkedList;

/*
Soll für Undo / Redo zuständig sein.

Speichert stackförmig alle aktionen die durchgeführt werden, also:
-> Panels dazugekommen oder gelöscht, Relationen dazugekommen oder gelöscht

Jedes mal wenn eine dieser Aktionen durchgeführt wird, wird sie auf diesen Stack gespeichert.

Bei Undo wird das oberste StackElement genommen und genau das Gegenteil ausgeführt, das StackElement wird nicht gelöscht für ein potentielles
Redo... nur die Head-Position im Stack verändert sich.

Bei Redo wird im Stack eins "hoch gegangen" und genau diese Aktion ausgeführt.
Wenn eine Aktion ausgeführt wird, wird alles über dem Head weggerworfen und das neue StackElement oben drauf gesetzt.

Umbennen muss mit rein
*/

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

class StackElement {
    public LinkedList<Action> data;
    public StackElement below;
    public StackElement above;

    public StackElement(Action action, StackElement below, StackElement above) {
        data = new LinkedList<>();
        data.add(action);
        this.below = below;
        this.above = above;
    }
}

public class ActionStack {
    private ContentPanel contentPanel;
    private static StackElement head;

    public ActionStack(ContentPanel contentPanel) {
        this.contentPanel = contentPanel;
        head = new StackElement(null, null, null);
    }

    public static void addPanelAction(boolean add, String name, Point pos) {
        Action action = new PanelAction(add, name, pos);
        addAction(action);
    }

    public static void appendPanelAction(boolean add, String name, Point pos) {
        Action action = new PanelAction(add, name, pos);
        head.data.add(action);
    }

    public static void addRelationAction(boolean add, String srcName, String targetName, Relation.Type type) {
        Action action = new RelationAction(add, srcName, targetName, type);
        addAction(action);
    }

    public static void appendRelationAction(boolean add, String srcName, String targetName, Relation.Type type) {
        Action action = new RelationAction(add, srcName, targetName, type);
        head.data.add(action);
    }

    public static void addRenameAction(String oldName, String newName) {
        Action action = new RenameAction(oldName, newName);
        addAction(action);
    }

    public static void appendRenameAction(String oldName, String newName) {
        Action action = new RenameAction(oldName, newName);
        head.data.add(action);
    }

    private static void addAction(Action action) {
        head.above = new StackElement(action, head, null);
        head = head.above;

        /*StackElement element = head;
        System.out.println("---------------------------------");
        while (element != null) {
            for (Action a : element.data) {
                System.out.print(a.getType() + ", ");
                if (a.getType().equals("Panel")) {
                    System.out.print(((PanelAction)a).add + " " + ((PanelAction)a).object + " " + ((PanelAction)a).position.x + " " + ((PanelAction)a).position.y);
                }
                else {
                    System.out.print(((RelationAction)a).add + " " + ((RelationAction)a).srcName + " " + ((RelationAction)a).targetName);
                }
                System.out.println();
            }

            System.out.println();
            element = element.below;
        }*/
    }

    // Problem bei head ist ganze liste...
    public void undo() {
        if (head.below == null)
            return;

        for (Action action : head.data) {
            if (action.getType().equals("Panel")) {
                PanelAction pAction = (PanelAction)action;
                if (pAction.add) {
                    Panel panel = contentPanel.getContent().getPanel(pAction.object);
                    pAction.position = panel.getLocation();
                    contentPanel.remove(panel);
                    contentPanel.getContent().deletePanel(pAction.object);
                    contentPanel.updateChildParentGroups();
                }
                else {
                    Panel panel = contentPanel.getContent().newPanel(pAction.object, pAction.position.x, pAction.position.y);
                    contentPanel.addMenu(panel);
                    contentPanel.add(panel);
                }
            }
            else if (action.getType().equals("Relation")) {
                RelationAction rAction = (RelationAction)action;
                if (rAction.add) {
                    contentPanel.getContent().deleteRelation(rAction.srcName, rAction.targetName, rAction.relationType);
                    contentPanel.updateChildParentGroups();
                }
                else {
                    contentPanel.getContent().newRelation(rAction.srcName, rAction.targetName, rAction.relationType);
                    contentPanel.updateChildParentGroups();
                }
            }
            else if (action.getType().equals("Rename")) {
                RenameAction rnAction = (RenameAction)action;
                contentPanel.getContent().renamePanel(rnAction.newName, rnAction.oldName);
            }
        }

        head = head.below;

        contentPanel.repaint();
        contentPanel.revalidate();
    }

    public void redo() {
        if (head.above == null)
            return;

        head = head.above;

        // Hier manchmal null???
        for (Action action : head.data) {
            if (action.getType().equals("Panel")) {
                PanelAction pAction = (PanelAction)action;
                if (pAction.add) {
                    Panel panel = contentPanel.getContent().newPanel(pAction.object, pAction.position.x, pAction.position.y);
                    contentPanel.addMenu(panel);
                    contentPanel.add(panel);
                }
                else {
                    Panel panel = contentPanel.getContent().getPanel(pAction.object);
                    pAction.position = panel.getLocation();
                    contentPanel.remove(panel);
                    contentPanel.getContent().deletePanel(pAction.object);
                    contentPanel.updateChildParentGroups();
                }
            }
            else if (action.getType().equals("Relation")) {
                RelationAction rAction = (RelationAction)action;
                if (rAction.add) {
                    contentPanel.getContent().newRelation(rAction.srcName, rAction.targetName, rAction.relationType);
                    contentPanel.updateChildParentGroups();
                }
                else {
                    contentPanel.getContent().deleteRelation(rAction.srcName, rAction.targetName, rAction.relationType);
                    contentPanel.updateChildParentGroups();
                }
            }
            else if (action.getType().equals("Rename")) {
                RenameAction rnAction = (RenameAction)action;
                contentPanel.getContent().renamePanel(rnAction.oldName, rnAction.newName);
            }
        }

        contentPanel.repaint();
        contentPanel.revalidate();
    }

    public void clear() {
        head = new StackElement(null, null, null);
    }
}
