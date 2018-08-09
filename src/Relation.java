public class Relation {
    public enum Type {
        CHILD
    }

    public Panel srcPanel;
    public Panel targetPanel;
    public Type type;

    public Relation(Panel srcPanel, Panel targetPanel, Type type) {
        this.srcPanel = srcPanel;
        this.targetPanel = targetPanel;
        this.type = type;
    }
}
