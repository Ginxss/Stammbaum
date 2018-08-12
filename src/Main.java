import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

// TODO: Zoom -> in "Map" Format umschalten, mit Viereck als Cursor, wenn man klickt, kommt man genau da hin.
// TODO: Automatisches Sortieren
public class Main {
    private JFrame frame;
    private JPanel backgroundPanel;

    private JPanel cardPanel;
    private ContentPanel contentPanel;
    private NavModePanel navModePanel;

    private JPanel taskBarPanel;
    private JButton newPanelButton, newRelationButton, deletePanelButton, deleteRelationButton, navModeButton;

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
        JMenuItem menuItemOpen = new JMenuItem("Öffnen");
        menuItemSave.addActionListener((e) -> save());
        menuItemOpen.addActionListener((e) -> open());
        fileMenu.add(menuItemSave);
        fileMenu.add(menuItemOpen);

        JMenu editMenu = new JMenu("Bearbeiten");

        JMenuItem menuItemDeleteSelected = new JMenuItem("Auswahl löschen");
        menuItemDeleteSelected.addActionListener((e) -> {
            contentPanel.updateSelectedPanels();
            for (int i = contentPanel.getPanelList().size() - 1; i >= 0; i--) {
                if (contentPanel.getSelectedPanels().contains(i))
                    contentPanel.deletePanel(i);
            }
            contentPanel.repaint();
            contentPanel.revalidate();
        });

        JMenuItem menuItemSearchPanel = new JMenuItem("Nach Person Suchen");
        menuItemSearchPanel.addActionListener((e) -> search());

        JMenuItem menuItemClear = new JMenuItem("Alles löschen");
        menuItemClear.addActionListener((e) -> {
            contentPanel.clear();
            contentPanel.repaint();
        });

        editMenu.add(menuItemDeleteSelected);
        editMenu.add(menuItemSearchPanel);
        editMenu.add(menuItemClear);

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

        newPanelButton = new JButton(new ImageIcon("newPanel.png"));
        newPanelButton.addActionListener((e) -> newPanelDialog());

        newRelationButton = new JButton(new ImageIcon("newRelation.png"));
        newRelationButton.addActionListener((e) -> newRelationDialog());

        deletePanelButton = new JButton(new ImageIcon("deletePanel.png"));
        deletePanelButton.addActionListener((e) -> deletePanelDialog());

        deleteRelationButton = new JButton(new ImageIcon("deleteRelation.png"));
        deleteRelationButton.addActionListener((e) -> deleteRelationDialog());

        navModeButton = new JButton("NavMode");
        navModeButton.addActionListener((e) -> {
            BufferedImage img = contentPanel.takeSnapShot();
            navModePanel.setContentImg(img);
            CardLayout cl = (CardLayout)cardPanel.getLayout();
            cl.next(cardPanel);
        });

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
        cardPanel.add(contentPanel);
        cardPanel.add(navModePanel);
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
                    fw.append(panel.getName() + System.lineSeparator());
                    fw.append(panel.getX() + System.lineSeparator());
                    fw.append(panel.getY() + System.lineSeparator());
                }

                fw.write("R:" + System.lineSeparator());
                for (Relation relation : contentPanel.getRelationList().getAllRelations()) {
                    fw.append(relation.srcPanel.getName() + System.lineSeparator());
                    fw.append(relation.targetPanel.getName() + System.lineSeparator());
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
