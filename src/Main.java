import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;

// TODO: Automatisches Sortieren
// Je mehr auf dem BILDSCHIRM ist, desto laggier, egal wieviel auf dem Panel ist!
public class Main {
    private JFrame frame;
    private JPanel backgroundPanel;

    private JPanel cardPanel;
    private ContentPanel contentPanel;
    private NavModePanel navModePanel;

    private JPanel taskBarPanel;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        createWindow();

        createMenu();

        createTaskBar();

        addComponents();

        contentPanel.newPanel("Vater", 10, 10);
        contentPanel.newPanel("Mutter", 100, 10);
        contentPanel.newPanel("Kind 1", 10, 200);
        contentPanel.newPanel("Kind 2", 100, 200);
        contentPanel.newRelation(contentPanel.getPanel("Kind 1"), contentPanel.getPanel("Vater"), Relation.Type.CHILD);
        contentPanel.newRelation(contentPanel.getPanel("Kind 1"), contentPanel.getPanel("Mutter"), Relation.Type.CHILD);
        contentPanel.newRelation(contentPanel.getPanel("Kind 2"), contentPanel.getPanel("Vater"), Relation.Type.CHILD);
        contentPanel.newRelation(contentPanel.getPanel("Kind 2"), contentPanel.getPanel("Mutter"), Relation.Type.CHILD);

        frame.repaint();
        frame.revalidate();
    }

    private void createWindow() {
        frame = new JFrame();
        frame.setSize(800, 600);
        frame.setTitle("Stammbaum");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        backgroundPanel = new JPanel(new BorderLayout());

        cardPanel = new JPanel(new CardLayout());

        contentPanel = new ContentPanel();
        contentPanel.setComponentPopupMenu(new ContentPanelRightClickMenu());

        navModePanel = new NavModePanel();
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Datei");

        JMenuItem menuItemSave = new JMenuItem("Speichern");
        menuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        menuItemSave.addActionListener((e) -> save());

        JMenuItem menuItemOpen = new JMenuItem("Öffnen");
        menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        menuItemOpen.addActionListener((e) -> open());

        fileMenu.add(menuItemSave);
        fileMenu.add(menuItemOpen);

        JMenu editMenu = new JMenu("Bearbeiten");

        JMenuItem menuItemNewPanel = new JMenuItem("Neue Person");
        menuItemNewPanel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        menuItemNewPanel.addActionListener((e) -> newPanelDialog());

        JMenuItem menuItemNewRelation = new JMenuItem("Neue Beziehung");
        menuItemNewRelation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
        menuItemNewRelation.addActionListener((e) -> newRelationDialog());

        JMenuItem menuItemDeletePanel = new JMenuItem("Person löschen");
        menuItemDeletePanel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));
        menuItemDeletePanel.addActionListener((e) -> deletePanelDialog());

        JMenuItem menuItemDeleteRelation = new JMenuItem("Beziehung löschen");
        menuItemDeleteRelation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
        menuItemDeleteRelation.addActionListener((e) -> deleteRelationDialog());

        JMenuItem menuItemNavMode = new JMenuItem("Navigationsmodus");
        menuItemNavMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
        menuItemNavMode.addActionListener((e) -> enterNavMode());

        JMenuItem menuItemDeleteSelected = new JMenuItem("Auswahl löschen");
        menuItemDeleteSelected.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
        menuItemDeleteSelected.addActionListener((e) -> {
            contentPanel.updateSelectedPanels();
            for (int i = contentPanel.getPanelList().size() - 1; i >= 0; i--) {
                if (contentPanel.getSelectedPanels().contains(i))
                    contentPanel.deletePanel(i);
            }
            contentPanel.repaint();
            contentPanel.revalidate();
        });

        JMenuItem menuItemClear = new JMenuItem("Alles löschen");
        menuItemClear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
        menuItemClear.addActionListener((e) -> {
            contentPanel.clear();
            contentPanel.repaint();
        });

        JMenuItem menuItemSearchPanel = new JMenuItem("Nach Person Suchen");
        menuItemSearchPanel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
        menuItemSearchPanel.addActionListener((e) -> search());

        editMenu.add(menuItemNewPanel);
        editMenu.add(menuItemNewRelation);
        editMenu.addSeparator();
        editMenu.add(menuItemDeletePanel);
        editMenu.add(menuItemDeleteRelation);
        editMenu.addSeparator();
        editMenu.add(menuItemDeleteSelected);
        editMenu.add(menuItemClear);
        editMenu.add(menuItemSearchPanel);
        editMenu.addSeparator();
        editMenu.add(menuItemNavMode);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);

        frame.setJMenuBar(menuBar);
    }

    private void createTaskBar() {
        taskBarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D)g;
                GradientPaint gp = new GradientPaint(0, 0, getBackground(), getWidth(), 0, getBackground().brighter());
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(Color.black);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        };

        taskBarPanel.setLayout(new BoxLayout(taskBarPanel, BoxLayout.X_AXIS));
        taskBarPanel.setBackground(Color.decode("#bac7d8"));
        taskBarPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JButton newPanelButton = new JButton(new ImageIcon("newPanel.png"));
        newPanelButton.addActionListener((e) -> newPanelDialog());

        JButton newRelationButton = new JButton(new ImageIcon("newRelation.png"));
        newRelationButton.addActionListener((e) -> newRelationDialog());

        JButton deletePanelButton = new JButton(new ImageIcon("deletePanel.png"));
        deletePanelButton.addActionListener((e) -> deletePanelDialog());

        JButton deleteRelationButton = new JButton(new ImageIcon("deleteRelation.png"));
        deleteRelationButton.addActionListener((e) -> deleteRelationDialog());

        JButton navModeButton = new JButton(new ImageIcon("navmode.png"));
        navModeButton.addActionListener((e) -> enterNavMode());

        Dimension space = new Dimension(5, 5);
        taskBarPanel.add(newPanelButton);
        taskBarPanel.add(Box.createRigidArea(space));
        taskBarPanel.add(newRelationButton);
        taskBarPanel.add(Box.createRigidArea(space));
        taskBarPanel.add(deletePanelButton);
        taskBarPanel.add(Box.createRigidArea(space));
        taskBarPanel.add(deleteRelationButton);
        taskBarPanel.add(Box.createRigidArea(space));
        taskBarPanel.add(navModeButton);
    }

    private void addComponents() {
        cardPanel.add(contentPanel, "Content");
        cardPanel.add(navModePanel, "NavMode");
        backgroundPanel.add(cardPanel, BorderLayout.CENTER);
        backgroundPanel.add(taskBarPanel, BorderLayout.NORTH);
        frame.getContentPane().add(backgroundPanel);
        frame.setVisible(true);
    }

    private Panel newPanelDialog() {
        Panel panel = null;

        JTextField field = new JTextField();
        field.addAncestorListener(new RequestFocusListener());

        JComponent[] inputs = new JComponent[] {new JLabel("Name:"), field};

        int option = JOptionPane.showConfirmDialog(frame, inputs, "Neue Person", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = field.getText();
            if (!name.equals("")) {
                panel = contentPanel.newPanel(name, 10, 10);
                contentPanel.repaint();
                contentPanel.revalidate();
            }
        }

        return panel;
    }

    private void newRelationDialog() {
        JTextField srcField = new JTextField();
        srcField.addAncestorListener(new RequestFocusListener());
        String[] types = {"ist Kind von"};
        JComboBox relationTypes = new JComboBox(types);
        JTextField targetField = new JTextField();

        JComponent[] inputs = new JComponent[] {srcField, relationTypes, targetField};

        int option = JOptionPane.showConfirmDialog(frame, inputs, "Neue Beziehung", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            Panel srcPanel = contentPanel.getPanel(srcField.getText());
            Panel targetPanel = contentPanel.getPanel(targetField.getText());
            if (srcPanel != null && targetPanel != null) {
                if (relationTypes.getSelectedItem() == "ist Kind von") {
                    contentPanel.newRelation(srcPanel, targetPanel, Relation.Type.CHILD);
                    contentPanel.repaint();
                    contentPanel.revalidate();
                }
            }
        }
    }

    private void deletePanelDialog() {
        JTextField field = new JTextField();
        field.addAncestorListener(new RequestFocusListener());

        JComponent[] inputs = new JComponent[] {new JLabel("Name:"), field};

        int option = JOptionPane.showConfirmDialog(frame, inputs, "Person löschen", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            contentPanel.deletePanel(field.getText());
            contentPanel.repaint();
            contentPanel.revalidate();
        }
    }

    private void deleteRelationDialog() {
        JTextField srcField = new JTextField();
        srcField.addAncestorListener(new RequestFocusListener());
        String[] types = {"ist Kind von"};
        JComboBox relationTypes = new JComboBox(types);
        JTextField targetField = new JTextField();

        JComponent[] inputs = new JComponent[] {srcField, relationTypes, targetField};

        int option = JOptionPane.showConfirmDialog(frame, inputs, "Beziehung löschen", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            if (relationTypes.getSelectedItem() == "ist Kind von") {
                contentPanel.deleteRelation(srcField.getText(), targetField.getText(), Relation.Type.CHILD);
                contentPanel.repaint();
                contentPanel.revalidate();
            }
        }
    }

    // Ist System.lineSeperator() gut???
    // Type-Check speichern
    private void save() {
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(".stb - Stammbaum Speicherdateien", "stb");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(fileFilter);

        int option = fileChooser.showSaveDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (!fileChooser.getSelectedFile().getName().endsWith(".stb"))
                fileChooser.setSelectedFile(new File(fileChooser.getSelectedFile().getPath()+".stb"));

            try (FileWriter fw = new FileWriter(fileChooser.getSelectedFile())) {
                fw.write("P:" + System.lineSeparator());
                for (Panel panel : contentPanel.getPanelList()) {
                    fw.append(panel.getName()).append(System.lineSeparator());
                    fw.append(String.valueOf(panel.getX())).append(System.lineSeparator());
                    fw.append(String.valueOf(panel.getY())).append(System.lineSeparator());
                }

                fw.write("R:" + System.lineSeparator());
                for (Relation relation : contentPanel.getRelationList().getAllRelations()) {
                    fw.append(relation.srcPanel.getName()).append(System.lineSeparator());
                    fw.append(relation.targetPanel.getName()).append(System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void open() {
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(".stb - Stammbaum Speicherdateien", "stb");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(fileFilter);

        int option = fileChooser.showOpenDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile().getName().endsWith(".stb")) {
                contentPanel.clear();

                int state = -1; // 0 = panels, 1 = relations

                try (BufferedReader br = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                    String line;
                    while (true) {
                        line = br.readLine();
                        if (line == null)
                            break;

                        if (line.equals("P:")) {
                            state = 0;
                            continue;
                        }
                        else if (line.equals("R:")) {
                            state = 1;
                            continue;
                        }

                        switch (state) {
                            case 0: {
                                String name = line;
                                int x = Integer.parseInt(br.readLine());
                                int y = Integer.parseInt(br.readLine());

                                contentPanel.newPanel(name, x, y);
                            } break;

                            case 1: {
                                String srcName = line;
                                String targetName = br.readLine();

                                contentPanel.newRelation(contentPanel.getPanel(srcName), contentPanel.getPanel(targetName), Relation.Type.CHILD);
                            } break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            contentPanel.repaint();
            contentPanel.revalidate();
        }
    }

    private void search() {
        JTextField field = new JTextField();
        field.addAncestorListener(new RequestFocusListener());

        JComponent[] inputs = new JComponent[] {new JLabel("Nach Name suchen:"), field};

        int option = JOptionPane.showConfirmDialog(frame, inputs, "Suchen", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            contentPanel.searchFor(field.getText());
        }
    }

    private void enterNavMode() {
        BufferedImage img = contentPanel.takeSnapShot();
        CardLayout cl = (CardLayout)cardPanel.getLayout();
        navModePanel.init(img, contentPanel.getPointOnCanvas(), contentPanel.getPanelList(), cl);

        cl.show(cardPanel, "NavMode");
    }

    class ContentPanelRightClickMenu extends JPopupMenu {
        private JMenuItem newPanelItem, newRelationItem;

        public ContentPanelRightClickMenu() {
            newPanelItem = new JMenuItem("Neue Person");
            newPanelItem.addActionListener((e) -> {
                Point mouseOnScreen = MouseInfo.getPointerInfo().getLocation();
                Point contentPanelPos = contentPanel.getLocationOnScreen();

                Panel panel = newPanelDialog();

                if (panel != null)
                    panel.setLocation(mouseOnScreen.x - contentPanelPos.x, mouseOnScreen.y - contentPanelPos.y);
            });

            newRelationItem = new JMenuItem("Neue Beziehung");
            newRelationItem.addActionListener((e) -> newRelationDialog());

            add(newPanelItem);
            add(newRelationItem);
        }
    }

}
