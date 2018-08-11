import javax.swing.*;
import java.util.LinkedList;

class Node {
    public JLabel data;
    public LinkedList<Node> parents;

    public Node(JLabel name) {
        this.data = name;
        parents = new LinkedList<>();
    }
}

class AncestorTree {
    public Node root;
    private LinkedList<Node> levelList;

    public AncestorTree(JLabel rootName) {
        root = new Node(rootName);
    }

    private void traverse(Node start, int level, int wantedLevel) {
        if (level == wantedLevel) {
            levelList.add(start);
            return;
        }

        for (Node node : start.parents)
            traverse(node, level + 1, wantedLevel);
    }
}