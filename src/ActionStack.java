import java.awt.*;
import java.util.LinkedList;

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

    public void sortPanelStart() {
        LinkedList<Action> panelActions = new LinkedList<>();
        LinkedList<Action> relationActions = new LinkedList<>();
        LinkedList<Action> renameActions = new LinkedList<>();

        for (Action action : data) {
            switch (action.getType()) {
            case "Panel":
                panelActions.add(action);
                break;
            case "Relation":
                relationActions.add(action);
                break;
            case "Rename":
                renameActions.add(action);
                break;
            }
        }

        data.clear();
        data.addAll(panelActions);
        data.addAll(relationActions);
        data.addAll(renameActions);
    }

    public void sortRelationStart() {
        LinkedList<Action> relationActions = new LinkedList<>();
        LinkedList<Action> panelActions = new LinkedList<>();
        LinkedList<Action> renameActions = new LinkedList<>();

        for (Action action : data) {
            switch (action.getType()) {
                case "Relation":
                    relationActions.add(action);
                    break;
                case "Panel":
                    panelActions.add(action);
                    break;
                case "Rename":
                    renameActions.add(action);
                    break;
            }
        }

        data.clear();
        data.addAll(relationActions);
        data.addAll(panelActions);
        data.addAll(renameActions);
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
    }

    public void undo() {
        if (head.below == null)
            return;

        head.sortPanelStart();

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

        head.sortRelationStart();

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
