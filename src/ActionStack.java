import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
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
*/

class Action {
    public int actionID; // 0 = addPanel, 1 = removePanel, 2 = addRelation, 3 = removeRelation
    public ArrayList<String> objects;
    public ArrayList<Point> positions;

    public Action(int id, ArrayList<String> objects, ArrayList<Point> positions) {
        this.actionID = id;
        this.objects = objects;
        this.positions = positions;
    }
}

class StackElement {
    public LinkedList<Action> data;
    public StackElement below;
    public StackElement above;

    public StackElement(LinkedList<Action> actions, StackElement below, StackElement above) {
        this.data = actions;
        this.below = below;
        this.above = above;
    }
}

public class ActionStack {
    private ContentPanel contentPanel;
    private StackElement head;

    public ActionStack(ContentPanel contentPanel) {
        this.contentPanel = contentPanel;
    }

    public void addAction(Action... actions) {
        LinkedList<Action> list = new LinkedList<>(Arrays.asList(actions));

        if (head == null) {
            head = new StackElement(list, null, null);
        }
        else {
            head.above = new StackElement(list, head, null);
            head = head.above;
        }
    }

    public void undo() {
        if (head != null) {
            if (head.below != null)
                head = head.below;
            // do the opposite of head action...

            for (Action action : head.data) {
                switch (action.actionID) {
                case 0:
                    for (int i = 0; i < action.objects.size(); i++)
                        contentPanel.deletePanel(action.objects.get(i));
                    break;
                case 1:
                    for (int i = 0; i < action.objects.size(); i++) {
                        Point pos = action.positions.get(i);
                        contentPanel.newPanel(action.objects.get(i), pos.x, pos.y);
                    }
                    break;
                case 2:
                    for (int i = 0; i < action.objects.size(); i += 3) {
                        String srcName = action.objects.get(i);
                        String targetName = action.objects.get(i+1);
                        String typeName = action.objects.get(i+2);
                        if (typeName.equals("CHILD"))
                            contentPanel.deleteRelation(srcName, targetName, Relation.Type.CHILD);
                    }
                    break;
                case 3:
                    for (int i = 0; i < action.objects.size(); i += 3) {
                        Panel srcPanel = contentPanel.getPanel(action.objects.get(i));
                        Panel targetPanel = contentPanel.getPanel(action.objects.get(i+1));
                        String typeName = action.objects.get(i+2);
                        if (typeName.equals("CHILD"))
                            contentPanel.newRelation(srcPanel, targetPanel, Relation.Type.CHILD);
                    }
                }
            }

            contentPanel.repaint();
        }
    }

    public void redo() {
        if (head != null)  {
            if (head.above != null)
                head = head.above;
            // do head action...

            for (Action action : head.data) {
                switch(action.actionID) {
                case 0:
                    for (int i = 0; i < action.objects.size(); i++) {
                        Point pos = action.positions.get(i);
                        contentPanel.newPanel(action.objects.get(i), pos.x, pos.y);
                    }
                    break;
                case 1:
                    for (int i = 0; i < action.objects.size(); i++)
                        contentPanel.deletePanel(action.objects.get(i));
                    break;
                case 2:
                    for (int i = 0; i < action.objects.size(); i += 3) {
                        Panel srcPanel = contentPanel.getPanel(action.objects.get(i));
                        Panel targetPanel = contentPanel.getPanel(action.objects.get(i+1));
                        String typeName = action.objects.get(i+2);
                        if (typeName.equals("CHILD"))
                            contentPanel.newRelation(srcPanel, targetPanel, Relation.Type.CHILD);
                    }
                    break;
                case 3:
                    for (int i = 0; i < action.objects.size(); i += 3) {
                        String srcName = action.objects.get(i);
                        String targetName = action.objects.get(i+1);
                        String typeName = action.objects.get(i+3);
                        if (typeName.equals("CHILD"))
                            contentPanel.deleteRelation(srcName, targetName, Relation.Type.CHILD);
                    }
                }
            }

            contentPanel.repaint();
        }
    }

    public void clear() {
        head = null;
    }
}
