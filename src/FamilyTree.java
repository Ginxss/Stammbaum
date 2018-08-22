import javax.swing.*;
import java.util.LinkedList;

class TreeNode {
    public JLabel data;
    public LinkedList<TreeNode> next;

    public TreeNode(JLabel label) {
        this.data = label;
        next = new LinkedList<>();
    }
}

class FamilyTree {
    public TreeNode root;

    public FamilyTree(JLabel rootLabel) {
        root = new TreeNode(rootLabel);
    }
}