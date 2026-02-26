package insta_montage;

import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.tool.PlugInTool;
import java.awt.event.MouseEvent;

public class MontageDialogTool extends PlugInTool {

    private MontageDialog dialog;

    @Override
    public String getToolName() {
        return "Insta Montage";
    }

    @Override
    public String getToolIcon() {
        // Draws a simple 2x2 grid icon in the toolbar
        return "C00fR0033R3300R0330R3033C00fL4488L4848L8448L8884";
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
            int[] imageIDs = WindowManager.getIDList();
            ImagePlus[] images = new ImagePlus[0];
            if (imageIDs != null) {
                images = new ImagePlus[imageIDs.length];
                for (int i = 0; i < imageIDs.length; i++) {
                    images[i] = WindowManager.getImage(imageIDs[i]);
                }
            }
            dialog = new MontageDialog("Insta Montage", "0.0.1", images);
            dialog.display();
        } else {
            dialog.toFront();
        }
    }
}