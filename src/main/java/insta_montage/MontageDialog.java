package insta_montage;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

public class MontageDialog extends JFrame {

    private final String pluginName;
    private final String version;

    // Grid
    private JSpinner rowsSpinner;
    private JSpinner colsSpinner;

    // Border
    private JSpinner borderThicknessSpinner;
    private JComboBox<String> borderColorCombo;

    // Canvas mode
    private JRadioButton expandCanvasRadio;
    private JRadioButton scaleDownRadio;

    // Labels
    private JCheckBox showLabelsCheck;
    private JComboBox<String> labelPositionCombo;

    // Scale bar
    private JCheckBox showScaleBarCheck;
    private JComboBox<String> scaleBarPositionCombo;
    private JComboBox<String> scaleBarImageCombo;
    private JSpinner scaleBarLengthSpinner;
    private JComboBox<String> scaleBarColorCombo;

    private static final String[] POSITIONS = {
        "Top Left", "Top Right", "Bottom Left", "Bottom Right"
    };

    private static final String[] COLORS = {
        "White", "Black", "Gray"
    };

    public MontageDialog(String pluginName, String version, ImagePlus[] images) {
        super(pluginName + " v" + version);
        this.pluginName = pluginName;
        this.version = version;
        buildUI();
        refreshImageList();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Grid Settings ---
        JPanel gridPanel = createTitledPanel("Grid Settings");
        gridPanel.setLayout(new GridLayout(2, 2, 5, 5));
        gridPanel.add(new JLabel("Rows:"));
        rowsSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
        gridPanel.add(rowsSpinner);
        gridPanel.add(new JLabel("Columns:"));
        colsSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
        gridPanel.add(colsSpinner);
        mainPanel.add(gridPanel);
        mainPanel.add(Box.createVerticalStrut(5));

        // --- Border Settings ---
        JPanel borderPanel = createTitledPanel("Border Settings");
        borderPanel.setLayout(new GridLayout(2, 2, 5, 5));
        borderPanel.add(new JLabel("Thickness (px):"));
        borderThicknessSpinner = new JSpinner(new SpinnerNumberModel(5, 0, 200, 1));
        borderPanel.add(borderThicknessSpinner);
        borderPanel.add(new JLabel("Color:"));
        borderColorCombo = new JComboBox<>(COLORS);
        borderPanel.add(borderColorCombo);
        mainPanel.add(borderPanel);
        mainPanel.add(Box.createVerticalStrut(5));

        // --- Canvas Mode ---
        JPanel canvasPanel = createTitledPanel("Border Mode");
        canvasPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        expandCanvasRadio = new JRadioButton("Expand canvas", true);
        scaleDownRadio = new JRadioButton("Scale images down");
        ButtonGroup canvasGroup = new ButtonGroup();
        canvasGroup.add(expandCanvasRadio);
        canvasGroup.add(scaleDownRadio);
        canvasPanel.add(expandCanvasRadio);
        canvasPanel.add(scaleDownRadio);
        mainPanel.add(canvasPanel);
        mainPanel.add(Box.createVerticalStrut(5));

        // --- Label Settings ---
        JPanel labelPanel = createTitledPanel("Label Settings");
        labelPanel.setLayout(new GridLayout(2, 2, 5, 5));
        labelPanel.add(new JLabel("Show labels:"));
        showLabelsCheck = new JCheckBox("", true);
        labelPanel.add(showLabelsCheck);
        labelPanel.add(new JLabel("Position:"));
        labelPositionCombo = new JComboBox<>(POSITIONS);
        labelPositionCombo.setSelectedItem("Bottom Left");
        labelPanel.add(labelPositionCombo);
        mainPanel.add(labelPanel);
        mainPanel.add(Box.createVerticalStrut(5));

        // --- Scale Bar Settings ---
        JPanel scaleBarPanel = createTitledPanel("Scale Bar Settings");
        scaleBarPanel.setLayout(new GridLayout(5, 2, 5, 5));
        scaleBarPanel.add(new JLabel("Show scale bar:"));
        showScaleBarCheck = new JCheckBox("", true);
        scaleBarPanel.add(showScaleBarCheck);
        scaleBarPanel.add(new JLabel("Position:"));
        scaleBarPositionCombo = new JComboBox<>(POSITIONS);
        scaleBarPositionCombo.setSelectedItem("Bottom Right");
        scaleBarPanel.add(scaleBarPositionCombo);
        scaleBarPanel.add(new JLabel("On image:"));
        scaleBarImageCombo = new JComboBox<>();
        scaleBarPanel.add(scaleBarImageCombo);
        scaleBarPanel.add(new JLabel("Length (image units):"));
        scaleBarLengthSpinner = new JSpinner(new SpinnerNumberModel(10.0, 0.1, 10000.0, 0.5));
        scaleBarPanel.add(scaleBarLengthSpinner);
        scaleBarPanel.add(new JLabel("Color:"));
        scaleBarColorCombo = new JComboBox<>(COLORS);
        scaleBarPanel.add(scaleBarColorCombo);
        mainPanel.add(scaleBarPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // --- Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton refreshButton = new JButton("Refresh Images");
        JButton makeMontageButton = new JButton("Make Montage");
        makeMontageButton.setBackground(new Color(70, 130, 180));
        makeMontageButton.setForeground(Color.WHITE);
        makeMontageButton.setFont(makeMontageButton.getFont().deriveFont(Font.BOLD));
        buttonPanel.add(refreshButton);
        buttonPanel.add(makeMontageButton);
        mainPanel.add(buttonPanel);

        // --- Button Actions ---
        refreshButton.addActionListener(e -> refreshImageList());

        makeMontageButton.addActionListener(e -> {
            // Check images are open
            int[] imageIDs = WindowManager.getIDList();
            if (imageIDs == null || imageIDs.length < 2) {
                IJ.error(pluginName, "Please open at least 2 images before making a montage.");
                return;
            }

            // Collect open images
            ImagePlus[] images = new ImagePlus[imageIDs.length];
            for (int i = 0; i < imageIDs.length; i++) {
                images[i] = WindowManager.getImage(imageIDs[i]);
            }

            // Build settings from UI
            MontageSettings settings = getSettings();

            // Run processor
            MontageProcessor processor = new MontageProcessor(images, settings);
            processor.run();
        });

        add(mainPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null); // center on screen
    }

    public void refreshImageList() {
        scaleBarImageCombo.removeAllItems();
        int[] imageIDs = WindowManager.getIDList();
        if (imageIDs == null || imageIDs.length == 0) {
            scaleBarImageCombo.addItem("No images open");
        } else {
            for (int i = 0; i < imageIDs.length; i++) {
                ImagePlus imp = WindowManager.getImage(imageIDs[i]);
                if (imp != null) {
                    scaleBarImageCombo.addItem((i + 1) + ": " + imp.getTitle());
                }
            }
        }
    }

    private MontageSettings getSettings() {
        MontageSettings settings = new MontageSettings();
        settings.rows = (int) rowsSpinner.getValue();
        settings.cols = (int) colsSpinner.getValue();
        settings.borderThickness = (int) borderThicknessSpinner.getValue();
        settings.borderColor = parseColor((String) borderColorCombo.getSelectedItem());
        settings.expandCanvas = expandCanvasRadio.isSelected();
        settings.showLabels = showLabelsCheck.isSelected();
        settings.labelPosition = (String) labelPositionCombo.getSelectedItem();
        settings.showScaleBar = showScaleBarCheck.isSelected();
        settings.scaleBarPosition = (String) scaleBarPositionCombo.getSelectedItem();
        settings.scaleBarImageIndex = Math.max(0, scaleBarImageCombo.getSelectedIndex());
        settings.scaleBarLength = (double) scaleBarLengthSpinner.getValue();
        settings.scaleBarColor = parseColor((String) scaleBarColorCombo.getSelectedItem());
        return settings;
    }

    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), title,
            TitledBorder.LEFT, TitledBorder.TOP));
        return panel;
    }

    private Color parseColor(String colorName) {
        if (colorName == null) return Color.WHITE;
        switch (colorName) {
            case "Black": return Color.BLACK;
            case "Gray":  return Color.GRAY;
            default:      return Color.WHITE;
        }
    }

    public void display() {
    setVisible(true);
}
}