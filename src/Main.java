import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.*;

// TODO: Automatisches Sortieren
// TODO: Listentypen anpassen
public class Main {
    private JFrame frame;
    private JPanel backgroundPanel;

    private Menu menu;
    private TaskBarPanel taskBarPanel;

    private JPanel cardPanel;
    private ContentPanel contentPanel;
    private NavModePanel navModePanel;

    private StatusPanel statusPanel;

    private ActionStack actionStack;

    private File openFile;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
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

        actionStack = new ActionStack(contentPanel);

        menu = new Menu(this, frame, contentPanel, actionStack);

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
        File file = new File("config.txt");
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
        try (FileWriter fw = new FileWriter("config.txt")) {
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

    private void requestFocus(JTextField field) {
        field.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent ancestorEvent) {
                field.requestFocusInWindow();
            }

            @Override
            public void ancestorRemoved(AncestorEvent ancestorEvent) {}
            @Override
            public void ancestorMoved(AncestorEvent ancestorEvent) {}
        });

        field.addFocusListener(new FocusAdapter() {
            private boolean firstTime = true;
            @Override
            public void focusLost(FocusEvent focusEvent) {
                if (firstTime) {
                    field.requestFocusInWindow();
                    firstTime = false;
                }
            }
        });
    }

    public Panel newPanelDialog(int x, int y) {
        Panel panel = null;

        JTextField field = new JTextField();
        requestFocus(field);

        JComponent[] inputs = new JComponent[] {new JLabel("Name:"), field};

        int option = JOptionPane.showConfirmDialog(frame, inputs, "Neue Person", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = field.getText();
            if (!name.equals("")) {
                panel = contentPanel.newPanel(name, x, y);

                contentPanel.repaint();
                contentPanel.revalidate();
            }
        }

        return panel;
    }

    public void newRelationDialog() {
        JTextField srcField = new JTextField();
        requestFocus(srcField);
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

    public void deletePanelDialog() {
        JTextField field = new JTextField();
        requestFocus(field);

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
        requestFocus(srcField);
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

    public void openDialog() {
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(".stb - Stammbaum Speicherdateien", "stb");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(fileFilter);

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

        int state = -1; // 0 = panels, 1 = relations

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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

            openFile = file;
            actionStack.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

        contentPanel.repaint();
        contentPanel.revalidate();
    }

    public void searchDialog() {
        JTextField field = new JTextField();
        requestFocus(field);

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
            newPanelItem.addActionListener((e) -> {
                Point mouseOnScreen = MouseInfo.getPointerInfo().getLocation();
                Point contentPanelPos = contentPanel.getLocationOnScreen();

                newPanelDialog(mouseOnScreen.x - contentPanelPos.x, mouseOnScreen.y - contentPanelPos.y);
            });

            newRelationItem = new JMenuItem("Neue Beziehung");
            newRelationItem.addActionListener((e) -> newRelationDialog());

            add(newPanelItem);
            add(newRelationItem);
        }
    }

}
