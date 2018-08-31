import java.util.LinkedList;

public class Content {
    private LinkedList<Panel> panelList;
    private RelationList relationList;
    private LinkedList<Integer> selectedPanels;

    public Content() {
        panelList = new LinkedList<>();
        relationList = new RelationList();
        selectedPanels = new LinkedList<>();
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

    public Panel getPanel(int i) {
        return panelList.get(i);
    }

    public Panel getPanel(String name) {
        for (Panel panel : panelList)
            if (panel.getPanelName().equals(name))
                return panel;
        return null;
    }

    public Panel newPanel(String name, int x, int y) {
        if (!nameExists(name)) {
            Panel panel = new Panel(name, x, y);
            panelList.add(panel);
            return panel;
        }
        return null;
    }

    public Relation newRelation(Panel srcPanel, Panel targetPanel, Relation.Type type) {
        if (srcPanel != null && targetPanel != null && !relationList.hasRelation(srcPanel, targetPanel, type)) {
            Relation relation = new Relation(srcPanel, targetPanel, type);
            relationList.add(relation);
            return relation;
        }
        return null;
    }

    public Relation newRelation(String srcName, String targetName, Relation.Type type) {
        Panel srcPanel = getPanel(srcName);
        Panel targetPanel = getPanel(targetName);
        return newRelation(srcPanel, targetPanel, type);
    }

    public boolean deletePanel(int i) {
        return deletePanel(getPanel(i));
    }

    public boolean deletePanel(String name) {
        return deletePanel(getPanel(name));
    }

    public boolean deletePanel(Panel panel) {
        if (panel != null) {
            relationList.removeRelationsWith(panel);
            panelList.remove(panel);
            return true;
        }
        return false;
    }

    public boolean deleteRelation(String srcName, String targetName, Relation.Type type) {
        return relationList.remove(srcName, targetName, type);
    }

    public boolean deleteRelation(Panel srcPanel, Panel targetPanel, Relation.Type type) {
        return relationList.remove(srcPanel, targetPanel, type);
    }

    public boolean renamePanel(String oldName, String newName) {
        Panel panel = getPanel(oldName);
        if (panel != null)
            if (!nameExists(newName)) {
                panel.setPanelName(newName);
                return true;
            }
        return false;
    }

    public void updateSelectedPanels() {
        selectedPanels.clear();
        for (int i = 0; i < panelList.size(); i++) {
            Panel panel = panelList.get(i);
            if (panel.isSelected())
                selectedPanels.add(i);
        }
    }

    public boolean nameExists(String name) {
        for (Panel panel : panelList)
            if (panel.getPanelName().equals(name))
                return true;
        return false;
    }

}
