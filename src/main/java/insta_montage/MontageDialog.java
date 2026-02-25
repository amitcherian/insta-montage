package insta_montage;

import ij.gui.GenericDialog;
import java.awt.Color;

public class MontageDialog {

    private final String pluginName;
    private final String version;
    private final String[] imageNames;
    private final int imageCount;

    private GenericDialog gd;
    private MontageSettings settings;

    private static final String[] POSITIONS = {
        "Top Left", "Top Right", "Bottom Left", "Bottom Right"
    };

    private static final String[] COLORS = {
        "White", "Black", "Gray"
    };

    public MontageDialog(String pluginName, String version, ij.ImagePlus[] images) {
        this.pluginName = pluginName;
        this.version = version;
        this.imageCount = images.length;
        this.imageNames = new String[images.length];
        for (int i = 0; i < images.length; i++) {
            imageNames[i] = (i + 1) + ": " + images[i].getTitle();
        }
        settings = new MontageSettings();
    }

    public void show() {
        gd = new GenericDialog(pluginName + " v" + version);

        // --- Grid Settings ---
        gd.addMessage("=== Grid Settings ===");
        gd.addNumericField("Rows", settings.rows, 0);
        gd.addNumericField("Columns", settings.cols, 0);

        // --- Border Settings ---
        gd.addMessage("=== Border Settings ===");
        gd.addNumericField("Border thickness (px)", settings.borderThickness, 0);
        gd.addChoice("Border color", COLORS, "White");

        // --- Canvas Mode ---
        gd.addMessage("=== Canvas Mode ===");
        gd.addRadioButtonGroup("Border mode:", 
            new String[]{"Expand canvas", "Scale images down"}, 1, 2, "Expand canvas");

        // --- Label Settings ---
        gd.addMessage("=== Label Settings ===");
        gd.addCheckbox("Show labels on each image", settings.showLabels);
        gd.addChoice("Label position", POSITIONS, settings.labelPosition);

        // --- Scale Bar Settings ---
        gd.addMessage("=== Scale Bar Settings ===");
        gd.addCheckbox("Show scale bar", settings.showScaleBar);
        gd.addChoice("Scale bar position", POSITIONS, settings.scaleBarPosition);
        gd.addChoice("Show scale bar on image", imageNames, imageNames[0]);
        gd.addNumericField("Scale bar length (in image units)", settings.scaleBarLength, 1);
        gd.addChoice("Scale bar color", COLORS, "White");

        gd.showDialog();
    }

    public boolean wasCanceled() {
        return gd.wasCanceled();
    }

    public MontageSettings getSettings() {
        // --- Read Grid ---
        settings.rows = (int) gd.getNextNumber();
        settings.cols = (int) gd.getNextNumber();

        // --- Read Border ---
        settings.borderThickness = (int) gd.getNextNumber();
        settings.borderColor = parseColor(gd.getNextChoice());

        // --- Read Canvas Mode ---
        String mode = gd.getNextRadioButton();
        settings.expandCanvas = mode.equals("Expand canvas");

        // --- Read Labels ---
        settings.showLabels = gd.getNextBoolean();
        settings.labelPosition = gd.getNextChoice();

        // --- Read Scale Bar ---
        settings.showScaleBar = gd.getNextBoolean();
        settings.scaleBarPosition = gd.getNextChoice();
        settings.scaleBarImageIndex = indexOf(imageNames, gd.getNextChoice());
        settings.scaleBarLength = gd.getNextNumber();
        settings.scaleBarColor = parseColor(gd.getNextChoice());

        return settings;
    }

    private Color parseColor(String colorName) {
        switch (colorName) {
            case "Black": return Color.BLACK;
            case "Gray":  return Color.GRAY;
            default:      return Color.WHITE;
        }
    }

    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) return i;
        }
        return 0;
    }
}