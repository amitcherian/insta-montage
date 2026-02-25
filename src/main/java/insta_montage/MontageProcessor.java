package insta_montage;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ColorProcessor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

public class MontageProcessor {

    private final ImagePlus[] images;
    private final MontageSettings s;

    private int rows;
    private int cols;
    private int tileW;
    private int tileH;
    private int canvasW;
    private int canvasH;

    public MontageProcessor(ImagePlus[] images, MontageSettings settings) {
        this.images = images;
        this.s = settings;
    }

    public void run() {
        // Auto-expand grid if needed
        rows = s.rows;
        cols = s.cols;
        while (rows * cols < images.length) {
            cols++;
        }

        // Determine tile size from the first image
        tileW = images[0].getWidth();
        tileH = images[0].getHeight();

        // Scale images down if that mode is selected
        if (!s.expandCanvas) {
            tileW = tileW - s.borderThickness;
            tileH = tileH - s.borderThickness;
            if (tileW < 1) tileW = 1;
            if (tileH < 1) tileH = 1;
        }

        // Total canvas size — border only between tiles, not on outer edges
        canvasW = cols * tileW + (cols - 1) * s.borderThickness;
        canvasH = rows * tileH + (rows - 1) * s.borderThickness;

        // Create blank canvas (RGB color image)
        ColorProcessor canvas = new ColorProcessor(canvasW, canvasH);
        canvas.setColor(s.borderColor);
        canvas.fill();

        // Place each image tile onto the canvas
        for (int i = 0; i < images.length; i++) {
            int row = i / cols;
            int col = i % cols;

            // No outer border — tiles start at 0,0
            int x = col * (tileW + s.borderThickness);
            int y = row * (tileH + s.borderThickness);

            // Convert and resize the tile image
            ImageProcessor tile = images[i].getProcessor().convertToRGB();
            if (tile.getWidth() != tileW || tile.getHeight() != tileH) {
                tile = tile.resize(tileW, tileH, true);
            }

            // Draw tile onto canvas
            canvas.insert(tile, x, y);

            // Draw label if enabled
            if (s.showLabels) {
                String title = images[i].getTitle();
                String label = title.contains(".") ? title.substring(0, title.lastIndexOf('.')) : title;
                drawLabel(canvas, label, x, y, tileW, tileH, s.labelPosition);
            }

            // Draw scale bar on the designated image only
            if (s.showScaleBar && i == s.scaleBarImageIndex) {
                drawScaleBar(canvas, images[i], x, y, tileW, tileH, s.scaleBarPosition);
            }
        }

        // Show result as a new image window
        ImagePlus result = new ImagePlus("Insta Montage", canvas);
        result.show();
    }

    private void drawLabel(ColorProcessor canvas, String text,
                           int tileX, int tileY, int tileW, int tileH,
                           String position) {
        int fontSize = Math.max(10, tileH / 20);
        Font font = new Font("SansSerif", Font.BOLD, fontSize);
        int padding = fontSize / 2;

        canvas.setFont(font);
        canvas.setColor(Color.WHITE);

        FontMetrics fm = canvas.getFontMetrics();
        int textW = fm.stringWidth(text);
        int textH = fontSize;

        int x, y;
        switch (position) {
            case "Top Right":
                x = tileX + tileW - textW - padding;
                y = tileY + padding + textH;
                break;
            case "Bottom Left":
                x = tileX + padding;
                y = tileY + tileH - padding;
                break;
            case "Bottom Right":
                x = tileX + tileW - textW - padding;
                y = tileY + tileH - padding;
                break;
            default: // Top Left
                x = tileX + padding;
                y = tileY + padding + textH;
                break;
        }

        // Clamp so label never spills outside tile
        if (x < tileX) x = tileX;
        if (x + textW > tileX + tileW) x = tileX + tileW - textW;

        canvas.drawString(text, x, y);
    }

    private void drawScaleBar(ColorProcessor canvas, ImagePlus imp,
                              int tileX, int tileY, int tileW, int tileH,
                              String position) {
        // Get pixel size from image calibration
        double pixelSize = imp.getCalibration().pixelWidth;
        if (pixelSize <= 0) pixelSize = 1.0;

        int barLengthPx = (int) Math.round(s.scaleBarLength / pixelSize);
        if (barLengthPx < 1) barLengthPx = 1;
        if (barLengthPx > tileW / 2) barLengthPx = tileW / 2;

        int barHeight = Math.max(3, tileH / 40);
        int padding = tileH / 20;

        int x, y;
        switch (position) {
            case "Top Left":
                x = tileX + padding;
                y = tileY + padding;
                break;
            case "Top Right":
                x = tileX + tileW - barLengthPx - padding;
                y = tileY + padding;
                break;
            case "Bottom Left":
                x = tileX + padding;
                y = tileY + tileH - barHeight - padding;
                break;
            default: // Bottom Right
                x = tileX + tileW - barLengthPx - padding;
                y = tileY + tileH - barHeight - padding;
                break;
        }

        // Draw the scale bar rectangle
        canvas.setColor(s.scaleBarColor);
        canvas.fillRect(x, y, barLengthPx, barHeight);

        // Draw scale bar label centered over the bar
        String unit = imp.getCalibration().getUnit();
        String label = (int) s.scaleBarLength + " " + unit;
        int fontSize = Math.max(8, tileH / 25);
        Font font = new Font("SansSerif", Font.PLAIN, fontSize);
        canvas.setFont(font);
        canvas.setColor(s.scaleBarColor);

        FontMetrics fm = canvas.getFontMetrics();
        int labelWidth = fm.stringWidth(label);

        // Center label over scale bar
        int labelX = x + (barLengthPx / 2) - (labelWidth / 2);

        // Clamp so it never spills outside the tile
        if (labelX < tileX) labelX = tileX;
        if (labelX + labelWidth > tileX + tileW) labelX = tileX + tileW - labelWidth;

        int labelY = (position.startsWith("Top"))
            ? y + barHeight + fontSize + 2
            : y - 4;
        canvas.drawString(label, labelX, labelY);
    }
}