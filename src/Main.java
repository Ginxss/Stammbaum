import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class Main {
    private JFrame frame;
    private JPanel backgroundPanel;

    private Menu menu;
    private TaskBarPanel taskBarPanel;

    private JPanel cardPanel;
    private ContentPanel contentPanel;
    private NavModePanel navModePanel;

    private StatusPanel statusPanel;

    private File openFile;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }

        initWindow();

        loadSettings();

        addComponents();

        frame.repaint();
        frame.revalidate();
    }

    private void initWindow() {
        frame = new JFrame();
        frame.setSize(800, 600);
        frame.setTitle("Stammbaum");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        backgroundPanel = new JPanel(new BorderLayout());

        taskBarPanel = new TaskBarPanel(this);
        taskBarPanel.init();

        statusPanel = new StatusPanel();
        statusPanel.init();

        cardPanel = new JPanel(new CardLayout());
        contentPanel = new ContentPanel(statusPanel.getLabel());
        contentPanel.setComponentPopupMenu(new ContentPanelRightClickMenu());
        navModePanel = new NavModePanel();

        ActionStack.init(contentPanel);

        menu = new Menu(this, frame, contentPanel);

        openFile = null;
    }

    private void addComponents() {
        cardPanel.add(contentPanel, "Content");
        cardPanel.add(navModePanel, "NavMode");
        backgroundPanel.add(cardPanel, BorderLayout.CENTER);
        backgroundPanel.add(taskBarPanel, BorderLayout.NORTH);
        backgroundPanel.add(statusPanel, BorderLayout.SOUTH);
        frame.getContentPane().add(backgroundPanel);
        frame.setVisible(true);
    }

    public void loadSettings() {
        File file = new File(".StbConfig.txt");
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;

                if ((line = br.readLine()) != null) {
                    String antialiasingValue = line.split(":")[1];
                    if (antialiasingValue.equals("true"))
                        menu.checkboxAntialiasing.doClick();
                }

                if ((line = br.readLine()) != null) {
                    String colorValue = line.split(":")[1];
                    Panel.setColor(Color.decode(colorValue));
                    for (Panel panel : contentPanel.getPanelList())
                        panel.updateColor();
                }

                if ((line = br.readLine()) != null) {
                    String filePath = line.split(":")[1];
                    loadFile(new File(filePath));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void storeSettings() {
        try (FileWriter fw = new FileWriter(".StbConfig.txt")) {
            String s = "Antialiasing:" + String.valueOf(contentPanel.getAntilasing()) + System.lineSeparator();
            s += "Color:" + toHexString(Panel.getColor()) + System.lineSeparator();
            if (openFile != null)
                s += "OpenFile:" + openFile.getPath() + System.lineSeparator();

            fw.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toHexString(Color color) {
        String hexColor = Integer.toHexString(color.getRGB() & 0xffffff);
        if (hexColor.length() < 6)
            hexColor = "000000".substring(0, 6 - hexColor.length()) + hexColor;
        return "#" + hexColor;
    }

    public Panel newPanelDialog() {
        Point mousePos = MouseInfo.getPointerInfo().getLocation();
        Point contentPanelPos = contentPanel.getLocationOnScreen();
        int diffX = mousePos.x - contentPanelPos.x;
        int diffY = mousePos.y - contentPanelPos.y;

        Panel panel = null;

        JTextField field = new JTextField();
        contentPanel.requestFocus(field);

        JComponent[] inputs = new JComponent[] {new JLabel("Name:"), field};

        int option = JOptionPane.showConfirmDialog(frame, inputs, "Neue Person", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = field.getText();
            if (!name.equals("")) {
                if (diffX < 0 || diffY < 0)
                    panel = contentPanel.newPanel(name, 10, 10);
                else
                    panel = contentPanel.newPanel(name, mousePos.x - contentPanelPos.x, mousePos.y - contentPanelPos.y);

                contentPanel.repaint();
                contentPanel.revalidate();
            }
        }

        return panel;
    }

    public void newRelationDialog() {
        JTextField srcField = new JTextField();
        contentPanel.requestFocus(srcField);
        String[] types = {"ist Kind von"};
        JComboBox relationTypes = new JComboBox(types);
        JTextField targetField = new JTextField();

        JComponent[] inputs = new JComponent[] {srcField, relationTypes, targetField};

        int option = JOptionPane.showConfirmDialog(frame, inputs, "Neue Beziehung", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            if (relationTypes.getSelectedItem() == "ist Kind von")
                contentPanel.newRelation(srcField.getText(), targetField.getText(), Relation.Type.CHILD);

            contentPanel.repaint();
            contentPanel.revalidate();
        }
    }

    public void deletePanelDialog() {
        JTextField field = new JTextField();
        contentPanel.requestFocus(field);

        JComponent[] inputs = new JComponent[] {new JLabel("Name:"), field};

        int option = JOptionPane.showConfirmDialog(frame, inputs, "Person löschen", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            contentPanel.deletePanel(field.getText());
            contentPanel.repaint();
            contentPanel.revalidate();
        }
    }

    public void deleteRelationDialog() {
        JTextField srcField = new JTextField();
        contentPanel.requestFocus(srcField);
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

    // Type-Check speichern
    public void saveDialog() {
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(".stb - Stammbaum Speicherdateien", "stb");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(fileFilter);
        if (openFile != null)
            fileChooser.setCurrentDirectory(openFile);

        System.out.println(openFile);

        int option = fileChooser.showSaveDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (!fileChooser.getSelectedFile().getName().endsWith(".stb"))
                fileChooser.setSelectedFile(new File(fileChooser.getSelectedFile().getPath()+".stb"));

            writeToFile(fileChooser.getSelectedFile());
        }
    }

    public void save() {
        if (openFile == null)
            saveDialog();
        else
            writeToFile(openFile);
    }

    private void writeToFile(File file) {
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try (FileWriter fw = new FileWriter(file)) {
            fw.write("P:" + System.lineSeparator());
            for (Panel panel : contentPanel.getPanelList()) {
                fw.append(panel.getPanelName()).append(System.lineSeparator());
                fw.append(String.valueOf(panel.getX())).append(System.lineSeparator());
                fw.append(String.valueOf(panel.getY())).append(System.lineSeparator());
            }

            fw.write("R:" + System.lineSeparator());
            for (Relation relation : contentPanel.getRelationList().getAllRelations()) {
                fw.append(relation.srcPanel.getPanelName()).append(System.lineSeparator());
                fw.append(relation.targetPanel.getPanelName()).append(System.lineSeparator());
            }

            fw.write("N:" + System.lineSeparator());
            for (Panel panel : contentPanel.getPanelList()) {
                if (panel.getNotes() != null) {
                    fw.append(panel.getPanelName()).append(System.lineSeparator());
                    fw.append(panel.getNotes()).append(System.lineSeparator());
                    fw.append("__ENDNOTES__").append(System.lineSeparator());
                }
            }

            openFile = file;
        } catch (IOException e) {
            e.printStackTrace();
        }

        storeSettings();

        statusPanel.getLabel().setText("Anzahl Personen: " + contentPanel.getPanelList().size() + "  |  gespeichert");

        frame.setCursor(Cursor.getDefaultCursor());
    }

    public void openDialog() {
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(".stb - Stammbaum Speicherdateien", "stb");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(fileFilter);
        if (openFile != null)
            fileChooser.setCurrentDirectory(openFile);

        int option = fileChooser.showOpenDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile().getName().endsWith(".stb")) {
                loadFile(fileChooser.getSelectedFile());
                storeSettings();
            }
        }
    }

    private void loadFile(File file) {
        contentPanel.clear();

        if (file == null || !file.exists()) {
            openFile = null;
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            int state = -1; // 0 = panels, 1 = relations, 2 = notes

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
                else if (line.equals("N:")) {
                    state = 2;
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

                    case 2: {
                        String panelName = line;

                        Panel panel = contentPanel.getPanel(panelName);
                        if (panel != null) {
                            StringBuilder notes = new StringBuilder();
                            String noteLine = br.readLine();
                            while ((noteLine != null) && !(noteLine.equals("__ENDNOTES__"))) {
                                notes.append(noteLine).append(System.lineSeparator());
                                noteLine = br.readLine();
                            }

                            panel.setNotes(notes.toString());
                        }
                    }
                }
            }

            openFile = file;
            ActionStack.clear();

        } catch (IOException e) {
            e.printStackTrace();
        }

        contentPanel.repaint();
        contentPanel.revalidate();
    }

    public void searchDialog() {
        JTextField field = new JTextField();
        contentPanel.requestFocus(field);

        JComponent[] inputs = new JComponent[] {new JLabel("Nach Name suchen:"), field};

        int option = JOptionPane.showConfirmDialog(frame, inputs, "Suchen", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            contentPanel.searchFor(field.getText());
            contentPanel.repaint();
        }
    }

    public void enterNavMode() {
        BufferedImage img = contentPanel.takeSnapShot();
        CardLayout cl = (CardLayout)cardPanel.getLayout();
        navModePanel.init(img, contentPanel.getPointOnCanvas(), contentPanel.getPanelList(), cl);

        cl.show(cardPanel, "NavMode");
    }

    class ContentPanelRightClickMenu extends JPopupMenu {
        private JMenuItem newPanelItem, newRelationItem;

        public ContentPanelRightClickMenu() {
            newPanelItem = new JMenuItem("Neue Person");
            newPanelItem.addActionListener((e) -> newPanelDialog());

            newRelationItem = new JMenuItem("Neue Beziehung");
            newRelationItem.addActionListener((e) -> newRelationDialog());

            add(newPanelItem);
            add(newRelationItem);
        }
    }

}
