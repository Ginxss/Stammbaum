import javax.swing.*;
import java.util.LinkedList;

class Node {
    public JLabel data;
    public LinkedList<Node> next;

    public Node(JLabel label) {
        this.data = label;
        next = new LinkedList<>();
    }
}

class FamilyTree {
    public Node root;

    public FamilyTree(JLabel rootLabel) {
        root = new Node(rootLabel);
    }
}