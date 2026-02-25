package insta_montage;

import java.awt.Color;

public class MontageSettings {

    // Grid
    public int rows;
    public int cols;

    // Border
    public int borderThickness;
    public Color borderColor;

    // Canvas mode
    public boolean expandCanvas; // true = expand canvas, false = scale images down

    // Labels
    public boolean showLabels;
    public String labelPosition; // "Top Left", "Top Right", "Bottom Left", "Bottom Right"
    public String[] labelTexts;  // one per image, defaults to image title

    // Scale bar
    public boolean showScaleBar;
    public String scaleBarPosition; // "Top Left", "Top Right", "Bottom Left", "Bottom Right"
    public int scaleBarImageIndex;  // which image tile gets the scale bar
    public double scaleBarLength;   // in physical units (e.g. microns)
    public Color scaleBarColor;

    public MontageSettings() {
        // Sensible defaults
        rows = 2;
        cols = 2;
        borderThickness = 5;
        borderColor = Color.WHITE;
        expandCanvas = true;
        showLabels = true;
        labelPosition = "Bottom Left";
        showScaleBar = true;
        scaleBarPosition = "Bottom Right";
        scaleBarImageIndex = 0;
        scaleBarLength = 10.0;
        scaleBarColor = Color.WHITE;
    }
}