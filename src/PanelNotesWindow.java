import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PanelNotesWindow extends JFrame {
    private Panel panel;

    private JPanel backgroundPanel;
    private JPanel topPanel;
    private JPanel textPanel;
    private JPanel bottomPanel;

    private JLabel label;
    private JTextArea textArea;
    private JScrollPane scrollPane;

    private JButton okButton;
    private JButton cancelButton;

    public PanelNotesWindow(Panel panel) {
        setLocationRelativeTo(panel);
        setTitle("Notizen zu " + panel.getPanelName());

        this.panel = panel;

        createPanels();

        createContent();

        createButtons();

        addComponents();

        setSize(500, 300);
        setVisible(true);
    }

    private void createPanels() {
        backgroundPanel = new JPanel(new BorderLayout());

        topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(new EmptyBorder(0, 10, 0, 10));

        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
        bottomPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    private void createContent() {
        label = new JLabel(panel.getPanelName() + ":");
        label.setFont(new Font("Tahoma", Font.PLAIN, 14));

        textArea = new JTextArea();
        textArea.setFont(new Font("Tahoma", Font.PLAIN, 12));
        if (panel.getNotes() != null)
            textArea.setText(panel.getNotes());

        scrollPane = new JScrollPane(textArea);
    }

    private void createButtons() {
        okButton = new JButton(" OK ");
        okButton.addActionListener((e) -> {
            String text = textArea.getText();
            if (text.isEmpty())
                panel.setNotes(null);
            else
                panel.setNotes(textArea.getText());

            setVisible(false);
            dispose();
        });

        cancelButton = new JButton(" Abbrechen ");
        cancelButton.addActionListener((e) -> {
            setVisible(false);
            dispose();
        });
    }

    private void addComponents() {
        topPanel.add(label);

        textPanel.add(scrollPane);

        bottomPanel.add(cancelButton);
        bottomPanel.add(Box.createRigidArea(new Dimension(5, 5)));
        bottomPanel.add(okButton);

        backgroundPanel.add(topPanel, BorderLayout.NORTH);
        backgroundPanel.add(textPanel, BorderLayout.CENTER);
        backgroundPanel.add(bottomPanel, BorderLayout.SOUTH);
        getContentPane().add(backgroundPanel);
    }

}
