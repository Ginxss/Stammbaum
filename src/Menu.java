import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Menu {
    private Main main;
    private JFrame frame;
    private ContentPanel contentPanel;
    private ActionStack actionStack;

    private JMenuBar menuBar;
    public JCheckBoxMenuItem checkboxAntialiasing;

    public Menu(Main main, JFrame frame, ContentPanel contentPanel, ActionStack actionStack) {
        this.main = main;
        this.frame = frame;
        this.contentPanel = contentPanel;
        this.actionStack = actionStack;

        menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createViewMenu());

        frame.setJMenuBar(menuBar);
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("Datei");

        JMenuItem menuItemSave = new JMenuItem("Speichern");
        menuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        menuItemSave.addActionListener((e) -> main.saveDialog());

        JMenuItem menuItemOpen = new JMenuItem("Öffnen");
        menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        menuItemOpen.addActionListener((e) -> main.openDialog());

        fileMenu.add(menuItemSave);
        fileMenu.add(menuItemOpen);

        return fileMenu;
    }

    private JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Bearbeiten");

        JMenuItem menuItemUndo = new JMenuItem("Rückgängig");
        menuItemUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        menuItemUndo.addActionListener((e) -> actionStack.undo());

        JMenuItem menuItemRedo = new JMenuItem("Wiederholen");
        menuItemRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
        menuItemRedo.addActionListener((e) -> actionStack.redo());

        JMenuItem menuItemNewPanel = new JMenuItem("Neue Person");
        menuItemNewPanel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        menuItemNewPanel.addActionListener((e) -> main.newPanelDialog(10, 10));

        JMenuItem menuItemNewRelation = new JMenuItem("Neue Beziehung");
        menuItemNewRelation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
        menuItemNewRelation.addActionListener((e) -> main.newRelationDialog());

        JMenuItem menuItemDeletePanel = new JMenuItem("Person löschen");
        menuItemDeletePanel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));
        menuItemDeletePanel.addActionListener((e) -> main.deletePanelDialog());

        JMenuItem menuItemDeleteRelation = new JMenuItem("Beziehung löschen");
        menuItemDeleteRelation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
        menuItemDeleteRelation.addActionListener((e) -> main.deleteRelationDialog());

        JMenuItem menuItemDeleteSelected = new JMenuItem("Auswahl löschen");
        menuItemDeleteSelected.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
        menuItemDeleteSelected.addActionListener((e) -> contentPanel.deleteSelected());

        JMenuItem menuItemClear = new JMenuItem("Alles löschen");
        menuItemClear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
        menuItemClear.addActionListener((e) -> contentPanel.clear());

        JMenuItem menuItemSearchPanel = new JMenuItem("Nach Person Suchen");
        menuItemSearchPanel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
        menuItemSearchPanel.addActionListener((e) -> main.searchDialog());

        JMenuItem menuItemNavMode = new JMenuItem("Navigationsmodus");
        menuItemNavMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
        menuItemNavMode.addActionListener((e) -> main.enterNavMode());

        editMenu.add(menuItemUndo);
        editMenu.add(menuItemRedo);
        editMenu.addSeparator();
        editMenu.add(menuItemNewPanel);
        editMenu.add(menuItemNewRelation);
        editMenu.addSeparator();
        editMenu.add(menuItemDeletePanel);
        editMenu.add(menuItemDeleteRelation);
        editMenu.addSeparator();
        editMenu.add(menuItemDeleteSelected);
        editMenu.add(menuItemClear);
        editMenu.addSeparator();
        editMenu.add(menuItemSearchPanel);
        editMenu.add(menuItemNavMode);

        return editMenu;
    }

    private JMenu createViewMenu() {
        JMenu viewMenu = new JMenu("Anzeige");

        checkboxAntialiasing = new JCheckBoxMenuItem("Glatte Linien");
        checkboxAntialiasing.addActionListener((e) -> {
            contentPanel.toggleAntialiasing();
            main.storeSettings();
        });

        JMenuItem menuItemColor = new JMenuItem("Farbe");
        menuItemColor.addActionListener((e) -> {
            Color color = JColorChooser.showDialog(frame, "Farbe auswählen", Panel.getColor());
            if (color != null) {
                Panel.setColor(color);
                for (Panel panel : contentPanel.getPanelList())
                    panel.updateColor();
                main.storeSettings();
            }
        });

        viewMenu.add(checkboxAntialiasing);
        viewMenu.add(menuItemColor);

        return viewMenu;
    }

}
