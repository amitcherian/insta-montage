package insta_montage;

import ij.ImagePlus;
import ij.plugin.tool.PlugInTool;
import java.awt.event.MouseEvent;

public class LUTAdjusterTool extends PlugInTool {

    private LUTAdjusterDialog dialog;

    @Override
    public String getToolName() {
        return "LUT Adjuster";
    }

    @Override
    public String getToolIcon() {
        // Draws three stacked colored bars representing R/G/B LUT channels
        return "Cf00R2030B30Cf0R2050B50C00fR2070B70T2fa8L";
    }

    @Override
    public void mousePressed(ImagePlus imp, MouseEvent e) {
        showDialog();
    }

    @Override
    public void run(String arg) {
        showDialog();
    }

    private void showDialog() {
        if (dialog == null || !dialog.isVisible()) {
            dialog = new LUTAdjusterDialog();
            dialog.display();
        } else {
            dialog.refresh();
            dialog.toFront();
        }
    }
}