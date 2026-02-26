package insta_montage;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MontageDialog extends JFrame {

    private final String pluginName;
    private final String version;

    // Thumbnail reorder panel
    private ThumbnailPanel thumbnailPanel;

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
        setResizable(true);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Thumbnail reorder panel ---
        thumbnailPanel = new ThumbnailPanel();
        thumbnailPanel.setOnOrderChanged(this::updateScaleBarCombo);
        JScrollPane thumbScroll = new JScrollPane(thumbnailPanel,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        thumbScroll.setPreferredSize(new Dimension(400, 120));
        mainPanel.add(thumbScroll);
        mainPanel.add(Box.createVerticalStrut(5));

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
            List<ImagePlus> orderedImages = thumbnailPanel.getOrderedImages();
            if (orderedImages.size() < 2) {
                IJ.error(pluginName, "Please open at least 2 images before making a montage.");
                return;
            }
            MontageSettings settings = getSettings(orderedImages);
            MontageProcessor processor = new MontageProcessor(
                orderedImages.toArray(new ImagePlus[0]), settings);
            processor.run();
        });

        add(mainPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }
    private void updateScaleBarCombo() {
        List<ImagePlus> ordered = thumbnailPanel.getOrderedImages();
        String currentSelection = (String) scaleBarImageCombo.getSelectedItem();
        scaleBarImageCombo.removeAllItems();
        for (int i = 0; i < ordered.size(); i++) {
            String title = ordered.get(i).getTitle();
            if (title.contains(".")) title = title.substring(0, title.lastIndexOf('.'));
            scaleBarImageCombo.addItem((i + 1) + ": " + title);
        }
        // Try to reselect the same image by name if possible
        if (currentSelection != null) {
            String currentName = currentSelection.contains(": ") 
                ? currentSelection.substring(currentSelection.indexOf(": ") + 2) 
                : currentSelection;
            for (int i = 0; i < scaleBarImageCombo.getItemCount(); i++) {
                String item = scaleBarImageCombo.getItemAt(i);
                if (item.endsWith(currentName)) {
                    scaleBarImageCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    public void refreshImageList() {
        int[] imageIDs = WindowManager.getIDList();
        List<ImagePlus> images = new ArrayList<>();
        if (imageIDs != null) {
            for (int id : imageIDs) {
                ImagePlus imp = WindowManager.getImage(id);
                if (imp != null) images.add(imp);
            }
        }

        // Update thumbnail panel
        thumbnailPanel.setImages(images);
        thumbnailPanel.revalidate();
        thumbnailPanel.repaint();

        // Update scale bar image combo
        scaleBarImageCombo.removeAllItems();
        if (images.isEmpty()) {
            scaleBarImageCombo.addItem("No images open");
        } else {
            for (int i = 0; i < images.size(); i++) {
                String title = images.get(i).getTitle();
                if (title.contains(".")) title = title.substring(0, title.lastIndexOf('.'));
                scaleBarImageCombo.addItem((i + 1) + ": " + title);
            }
        }

        pack();
    }

    private MontageSettings getSettings(List<ImagePlus> orderedImages) {
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